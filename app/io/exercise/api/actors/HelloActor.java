package io.exercise.api.actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;

/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class HelloActor extends AbstractActor {

	public static Props getProps() {
		return Props.create(HelloActor.class);
	}

	@Override
	public Receive createReceive() {
		return receiveBuilder()
				.match(HelloActorProtocol.SayHello.class, hello -> {
					String reply = "Hello, " + hello.name;
					ObjectNode node = Json.newObject();
					node.put("reply", reply);
					sender().tell(node, self());
				})
				.build();
	}
}
