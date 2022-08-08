package io.exercise.api.actors;

import akka.actor.AbstractActorWithTimers;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import io.exercise.api.models.Dashboard;
import io.exercise.api.mongo.IMongoDB;
import org.bson.conversions.Bson;
import play.libs.Json;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Chat Actor - Representing a user in a room!
 */
public class MyMongoMonitoringActor extends AbstractActorWithTimers {
    protected final String SCHEDULE_KEY = "MONGO_WATCHER";
    /**
     * For logging purposes
     */
    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);
    /**
     * String messages as constants
     */
    private static final String PING = "PING";
    private static final String PONG = "PONG";
    /**
     * Mongo DB
     */
    private IMongoDB mongoDB;
    /**
     * Executor
     */
    private Executor executor;
    /**
     * Web socket represented from the front end
     */
    private ActorRef out;

    /**
     * Change Stream Watcher
     */
    private ChangeStreamIterable<Dashboard> watcher;

    /**
     * Props creator for this class of actors
     *
     * @param out
     * @param mongoDB
     * @return
     */
    public static Props props(ActorRef out, IMongoDB mongoDB, Executor executor) {
        return Props.create(MyMongoMonitoringActor.class, () -> new MyMongoMonitoringActor(out, mongoDB, executor));
    }

    private MyMongoMonitoringActor(ActorRef out, IMongoDB mongoDB, Executor executor) {
        this.mongoDB = mongoDB;
        this.out = out;
        this.executor = executor;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(String.class, this::onMessageReceived)
                .match(MongoMonitoringActorProtocol.WATCH.class, this::onHandleWatchSignal)
                .build();
    }

    @Override
    public void preStart() {
        this.watch();
    }

    @Override
    public void postStop() {
        this.close();
    }

    private void onHandleWatchSignal(MongoMonitoringActorProtocol.WATCH ignore) {
        this.watch();
    }

    private void watch() {
        this.close();
        log.info("Watch!");
        out.tell("Starting to Watch", getSelf());
        CompletableFuture.runAsync(() -> {
            getTimers().cancel(SCHEDULE_KEY);
            try {
                MongoCollection<Dashboard> userMongoCollection = mongoDB.getMongoDatabase()
                        .getCollection("dashboards", Dashboard.class);
                List<Bson> pipeline = new ArrayList<>();

                pipeline.add(Aggregates.match(
                                Filters.in("operationType", Arrays.asList("insert", "delete", "update"))
                ));

                watcher = userMongoCollection.watch(pipeline, Dashboard.class);
                out.tell("Registering", getSelf());
                MongoCursor<ChangeStreamDocument<Dashboard>> cursor = watcher.iterator();

                while (!getSelf().isTerminated()) {
                    ChangeStreamDocument<Dashboard> item = cursor.tryNext();
                    if (item == null) {
                        continue;
                    }
                    log.info("Got message back {}", item.toString());
                    out.tell(Json.toJson(item.getFullDocument()).toString(), getSelf());
                }
                log.info("Closing Cursor");
                cursor.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                // Couldn't do it, try again
                if (getSelf().isTerminated()) {
                    return;
                }
                getTimers().startSingleTimer(SCHEDULE_KEY, new MongoMonitoringActorProtocol.WATCH(), Duration.of(5, ChronoUnit.SECONDS));
                out.tell("Failed to register, trying again!", getSelf());
            }
        }, executor);

    }

    /**
     * Receiver of socket messages coming from the front end
     *
     * @param message
     */
    public void onMessageReceived(String message) {
        if (message.equals(PING)) {
            out.tell(PONG, getSelf());
        }
    }

    private void close() {
        if (watcher == null) {
            return;
        }
        try {
            watcher.iterator().close();
            watcher.cursor().close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
