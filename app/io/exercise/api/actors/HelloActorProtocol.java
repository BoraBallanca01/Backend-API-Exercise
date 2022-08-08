package io.exercise.api.actors;

/**
 * Created by agonlohaj on 04 Sep, 2020
 */
public class HelloActorProtocol {

	public static class SayHello {
		public final String name;

		public SayHello(String name) {
			this.name = name;
		}
	}
}
