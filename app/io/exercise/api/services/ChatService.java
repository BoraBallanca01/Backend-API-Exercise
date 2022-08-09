package io.exercise.api.services;

import akka.actor.ActorSystem;
import akka.stream.Materializer;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.models.User;
import play.libs.streams.ActorFlow;
import play.mvc.Http;
import play.mvc.WebSocket;

import javax.inject.Inject;

public class ChatService {
    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;



    public WebSocket chat (User user, String room){

        return WebSocket.Text.accept(request->
                ActorFlow.actorRef((out)-> ChatActor.props(out,room),actorSystem,materializer));

    }
}
