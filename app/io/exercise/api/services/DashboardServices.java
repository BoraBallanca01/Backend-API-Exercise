package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.GraphLookupOptions;
import com.mongodb.client.model.UnwindOptions;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.mongo.IMongoDB;

import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.api.libs.functional.Monoid;
import play.libs.Json;
import play.mvc.Http;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

public class DashboardServices {
    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<Dashboard> create(Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> dashboardCollection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        dashboardCollection.insertOne(dashboard);
                        return dashboard;
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<List<Dashboard>> read(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        int limit = 100, skip = 0;
                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        List<Dashboard> dashboards = collection.find(Filters.or(
                                                Filters.in("readACL", user.getId()),
                                                Filters.in("readACL", user.getRoles()),
                                                Filters.in("writeACL", user.getId()),
                                                Filters.in("writeACL", user.getRoles()),
                                                Filters.and(
                                                        Filters.eq("readACL", new ArrayList<>()),
                                                        Filters.eq("writeACL", new ArrayList<>()))

                                        )
                                )
                                .skip(skip)
                                .limit(limit)
                                .into(new ArrayList<>());
                        List<String> dashId = dashboards.stream().map(x -> x.getId().toString()).collect(Collectors.toList());
                        List<ContentType> contents = mongoDB.getMongoDatabase()
                                .getCollection("content", ContentType.class)
                                .find(Filters.and(
                                        Filters.in("dashboardId", dashId),
                                        Filters.or(
                                                Filters.in("readACL", user.getId()),
                                                Filters.in("readACL", user.getRoles()),
                                                Filters.in("writeACL", user.getId()),
                                                Filters.in("writeACL", user.getRoles()),
                                                Filters.and(
                                                        Filters.eq("readACL", new ArrayList<>()),
                                                        Filters.eq("writeACL", new ArrayList<>()))

                                        )))
                                .into(new ArrayList<>());

                        dashboards.forEach(dashboard -> dashboard
                                .setContent(
                                        contents
                                                .stream().filter(x -> x.getDashboardId().equals(dashboard.getId())).collect(Collectors.toList())));
                        return dashboards;

                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<Dashboard> update(Dashboard dashboard, User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        collection.find(Filters.or(
                                        Filters.in("readACL", user.getId()),
                                        Filters.in("readACL", user.getRoles()),
                                        Filters.in("writeACL", user.getId()),
                                        Filters.in("writeACL", user.getRoles()),
                                        Filters.and(
                                                Filters.eq("readACL", new ArrayList<>()),
                                                Filters.eq("writeACL", new ArrayList<>()))
                                )
                        );
                        collection.replaceOne(Filters.eq("_id", new ObjectId(id)), dashboard);
                        return dashboard;
                    } catch (NullPointerException | CompletionException e) {
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Something went wrong.")));
                    } catch (IllegalArgumentException e) {
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Something went wrong.")));
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Something went wrong.")));
                    }
                }
        );
    }

    public CompletableFuture<Dashboard> delete(Dashboard dashboard, User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        collection.find(Filters.or(
                                        Filters.in("readACL", user.getId()),
                                        Filters.in("readACL", user.getRoles()),
                                        Filters.in("writeACL", user.getId()),
                                        Filters.in("writeACL", user.getRoles()),
                                        Filters.and(
                                                Filters.eq("readACL", new ArrayList<>()),
                                                Filters.eq("writeACL", new ArrayList<>()))

                                )
                        );
                        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
                        return dashboard;
                    } catch (NullPointerException | CompletionException e) {
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Something went wrong.")));
                    } catch (IllegalArgumentException e) {
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Something went wrong.")));
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Something went wrong.")));
                    }

                }
        );
    }

    public CompletableFuture<List<Dashboard>> hierarchy() {
        return CompletableFuture.supplyAsync(() -> {


//            List<Bson> pipeline = Arrays.asList(new Document("$match",
//                            new Document("parentId", new BsonNull())),
//                    new Document("$graphLookup", new Document("from", "dashboards")
//                            .append("startWith", "$_id")
//                            .append("connectFromField", "_id")
//                            .append("connectToField", "parentId")
//                            .append("depthField", "level")
//                            .append("as", "children")),
//                    new Document("$unwind", new Document("path", "$children")
//                            .append("preserveNullAndEmptyArrays", true)),
//                    new Document("$sort", new Document("children.level", -1L)),
//                    new Document("$group", new Document("_id", "$_id")
//                            .append("parentId",
//                                    new Document("$first", "$parentId"))
//                            .append("name",
//                                    new Document("$first", "$name"))
//                            .append("description",
//                                    new Document("$first", "$description"))
//                            .append("category",
//                                    new Document("$first", 1L))
//                            .append("children",
//                                    new Document("$push", "$children"))),
//                    new Document("$addFields",
//                            new Document("children",
//                                    new Document("$reduce",
//                                            new Document("input", "$children")
//                                                    .append("initialValue",
//                                                            new Document("level", -1L)
//                                                                    .append("presentChild", Arrays.asList())
//                                                                    .append("prevChild", Arrays.asList()))
//                                                    .append("in",
//                                                            new Document("$let",
//                                                                    new Document("vars",
//                                                                            new Document("prev",
//                                                                                    new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$$value.level", "$$this.level")), "$$value.prevChild", "$$value.presentChild")))
//                                                                                    .append("current",
//                                                                                            new Document("$cond", Arrays.asList(new Document("$eq", Arrays.asList("$$value.level", "$$this.level")), "$$value.presentChild", Arrays.asList()))))
//                                                                            .append("in",
//                                                                                    new Document("level", "$$this.level")
//                                                                                            .append("prevChild", "$$prev")
//                                                                                            .append("presentChild",
//                                                                                                    new Document("$concatArrays", Arrays.asList("$$current", Arrays.asList(new Document("$mergeObjects", Arrays.asList("$$this",
//                                                                                                            new Document("children",
//                                                                                                                    new Document("$filter",
//                                                                                                                            new Document("input", "$$prev")
//                                                                                                                                    .append("as", "e")
//                                                                                                                                    .append("cond",
//                                                                                                                                            new Document("$eq", Arrays.asList("$$e.parentId", "$$this._id"))))))))))))))))),
//                    new Document("$addFields",
//                            new Document("id", "$_id")
//                                    .append("children", "$children.presentChild")));
//            return mongoDB.getMongoDatabase()
//                    .getCollection("dashboards", Dashboard.class)
//                    .aggregate(pipeline, Dashboard.class)
//                    .into(new ArrayList<>());
//        });
            MongoCollection<Dashboard> collection = mongoDB.getMongoDatabase().getCollection("dashboards", Dashboard.class);
            List<Bson> pipeline = new ArrayList<>();
            pipeline.add(Aggregates
                    .match(new Document("parentId", new BsonNull())));
            pipeline.add(Aggregates
                    .graphLookup("dashboards", "$_id", "$_id", "parentId", "children", new GraphLookupOptions()));
            Document doc=new Document("path","$children").append("preserveNullAndEmptyArrays",true);
            pipeline.add(Aggregates
                    .unwind("$children",new UnwindOptions().preserveNullAndEmptyArrays(true)),
                    Aggregates.sort());

        }

    }
