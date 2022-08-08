package io.exercise.api.actors;

import lombok.Data;
import lombok.NoArgsConstructor;

public class MongoMonitoringActorProtocol  {

	@Data
	@NoArgsConstructor
	public static class WATCH implements ActorMessage {}
}
