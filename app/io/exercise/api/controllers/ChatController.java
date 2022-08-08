package io.exercise.api.controllers;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.stream.Materializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.exercise.api.actors.ChatActor;
import io.exercise.api.actors.ChatActorProtocol;
import play.libs.streams.ActorFlow;
import play.mvc.*;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ChatController extends Controller {
    @Inject
    private ActorSystem actorSystem;
    @Inject
    private Materializer materializer;

    public WebSocket chat (String room){
        return WebSocket.Text.accept(request->
                ActorFlow.actorRef((out)-> ChatActor.props(out,room),actorSystem,materializer));

    }
    @BodyParser.Of(BodyParser.Json.class)
    public Result publish(Http.Request request,String room){
        JsonNode node=request.body().asJson();
        Cluster cluster=Cluster.get(actorSystem);
        ActorRef mediator= DistributedPubSub.get(cluster.system()).mediator();
        mediator.tell(
                new DistributedPubSubMediator.Publish(room, new ChatActorProtocol.ChatMessage(node.asText())),
                ActorRef.noSender()
        );
                return ok(node);
    }

}
