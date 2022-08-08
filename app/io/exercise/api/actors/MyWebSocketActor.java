package io.exercise.api.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class MyWebSocketActor extends AbstractActor {

	public static Props props(ActorRef out) {
		return Props.create(MyWebSocketActor.class, out);
	}

	private final ActorRef out;

	public MyWebSocketActor(ActorRef out) {
		this.out = out;
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(String.class, message -> out.tell("I received your message: " + message, self()))
				.build();
	}
}
