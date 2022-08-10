package io.exercise.api.controllers;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import com.mongodb.client.MongoCollection;
import com.typesafe.config.Config;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Chat;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.AuthenticationService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.F;
import play.libs.Json;
import play.libs.streams.ActorFlow;
import play.mvc.*;
import com.mongodb.client.model.Filters;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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
    Config config;

    public WebSocket chat(String room, String token) {
        return WebSocket.Text.acceptOrResult(request ->
                {
                    try {
                        User user = ServiceUtils.decodeToken(token)
                                .thenCompose(x -> ServiceUtils.getUserFromId(mongoDB, x))
                                .thenCompose(x -> ServiceUtils.verify(x, token, config))
                                .join();
                        if (user == null) {
                            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
                        }

                        MongoCollection<Chat> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("chat", Chat.class);

                        Chat chat = collection.find(Filters.eq("_id", new ObjectId(room))).first();

                        if (chat == null) {
                            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Chat not found!")));
                        }

                        String userId = user.getId().toString();
                        List<String> roles = user.getRoles();
                        boolean read = false, write = false;

                        for (String id : roles) {
                            if (chat.getReadACL().contains(id) || chat.getReadACL().contains(userId)) {
                                read = true;
                            }
                            if (chat.getWriteACL().contains(id) || chat.getWriteACL().contains(userId)) {
                                read = true;
                                write = true;
                            }

                        }

                        if (!read) {
                            throw new CompletionException(new RequestException(Http.Status.FORBIDDEN, Json.toJson("Ku je nis o boss!!")));
                        }

                        boolean finalWrite = write;
                        return CompletableFuture.completedFuture(F.Either.Right(ActorFlow.actorRef((out) -> ChatActor.props(out, room, finalWrite), actorSystem, materializer)));
                    } catch (Exception e) {
                        e.printStackTrace();
                        return CompletableFuture.completedFuture(F.Either.Left(badRequest()));
                    }
                }
        );

    }


}
