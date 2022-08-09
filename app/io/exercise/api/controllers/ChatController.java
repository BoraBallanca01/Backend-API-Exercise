package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.mongodb.client.MongoCollection;
import io.exercise.api.actions.Authentication;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Chat;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.AuthenticationService;
import io.exercise.api.services.ChatService;
import io.exercise.api.services.DashboardServices;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.ServiceUtils;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletionException;


@Singleton
public class ChatController extends Controller {
    @Inject
    SerializationService serializationService;

    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;

    @Inject
    IMongoDB mongoDB;

    @Inject
    ChatService service;

//    public CompletableFuture<Result> chatCreate(Http.RequestHeader request,String roomId)
//    {
//        return service.chat(request,roomId)
//
//    }
    @Inject
    AuthenticationService auth;
//
//    @Authentication
//    public WebSocket getUser(Http.Request request, String room)
//    {
//        return service.chat(ServiceUtils.getUserFrom(request),room)
//
////        return chat(room);
//    }




    public WebSocket chat (String room, String token ){

        return WebSocket.Json.acceptOrResult(request->
        {
            User user = ServiceUtils
                    .decodeToken(token)
                    .thenCompose(ServiceUtils::getUserFromId)
                    .thenCompose(x -> ServiceUtils.verify(x,token))
                    .join();

            ActorFlow.actorRef((out)-> ChatActor.props(out,room),actorSystem,materializer);
            MongoCollection<Chat> collection= mongoDB
                    .getMongoDatabase()
                    .getCollection("chat",Chat.class);
            if(user==null){
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
            }



        }
        );

    }


}
