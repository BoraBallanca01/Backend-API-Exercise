package io.exercise.api.actors;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.event.Logging;
import akka.event.LoggingAdapter;

public class ChatActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    private static final String JOINED_ROOM = "Someone Joined the Room!";
    private static final String LEFT_ROOM = "Someone Left the Room!";
    private static final String PING = "PING";
    private static final String PONG = "PONG";

    private boolean write;
    private ActorRef mediator = DistributedPubSub.get(getContext().system()).mediator();
    private String roomId;
    private ActorRef out;

    public static Props props(ActorRef out, String roomId, boolean write) {
        return Props.create(ChatActor.class, () -> new ChatActor(out, roomId, write));
    }

    private ChatActor(ActorRef out, String roomId, boolean write) {
        this.roomId = roomId;
        this.out = out;
        this.write = write;
        mediator.tell(new DistributedPubSubMediator.Subscribe(roomId, getSelf()), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::onMessageReceived)
                .match(ChatActorProtocol.ChatMessage.class, this::onChatMessageReceived)
                .match(DistributedPubSubMediator.SubscribeAck.class, this::onSubscribe)
                .match(DistributedPubSubMediator.UnsubscribeAck.class, this::onUnsubscribe)
                .build();
    }

    public void onMessageReceived(String message) {
        if (message.equals(PING)) {
            out.tell(PONG, getSelf());
            return;
        }
        broadcast(message);
    }

    public void onChatMessageReceived(ChatActorProtocol.ChatMessage what) {
        if (getSender().equals(getSelf())) {
            return;
        }
        String message = what.getMessage();
        out.tell(message, getSelf());
    }

    public void onSubscribe(DistributedPubSubMediator.SubscribeAck message) {
        this.joinTheRoom();
    }

    public void onUnsubscribe(DistributedPubSubMediator.UnsubscribeAck message) {
        this.leaveTheRoom();
    }

    @Override
    public void postStop() {
        this.leaveTheRoom();
    }

    public void joinTheRoom() {
        this.broadcast(JOINED_ROOM);
    }

    public void leaveTheRoom() {
        this.broadcast(LEFT_ROOM
        );
    }

    private void broadcast(String message) {
        if (!write) {
            out.tell("You have no access to message here!", getSelf());
            return;
        }
        mediator.tell(new DistributedPubSubMediator
                .Publish(roomId, new ChatActorProtocol.ChatMessage(message)), getSelf());
    }
}