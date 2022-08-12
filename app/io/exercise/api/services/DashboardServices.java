package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.GraphLookupOptions;
import com.mongodb.client.model.UnwindOptions;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.BaseModel;
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

import static com.mongodb.client.model.Filters.all;
import static com.mongodb.client.model.Filters.eq;

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

    public CompletableFuture<List<Dashboard>> read(User user, int limit, int skip) {
        return CompletableFuture.supplyAsync(() -> {
                    try {

                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        List<Dashboard> dashboards = collection.find(Filters.or(
                                                Filters.in("readACL", user.getId()),
                                                Filters.in("readACL", user.getRoles()),
                                                Filters.in("writeACL", user.getId()),
                                                Filters.in("writeACL", user.getRoles()),
                                                Filters.and(
                                                        eq("readACL", new ArrayList<>()),
                                                        eq("writeACL", new ArrayList<>()))

                                        )
                                )
                                .skip(skip)
                                .limit(limit)
                                .into(new ArrayList<>());
                        List<String> dashId = dashboards.stream().map(x -> x.getId().toString()).collect(Collectors.toList());
                        List<ContentType> contents = mongoDB.getMongoDatabase()
                                .getCollection("content", ContentType.class)
                                .find(Filters.and(
                                        Filters.in("dashboardId", dashId.stream().map(ObjectId::new).collect(Collectors.toList())),
                                        Filters.or(
                                                Filters.in("readACL", user.getId()),
                                                Filters.in("readACL", user.getRoles()),
                                                Filters.in("writeACL", user.getId()),
                                                Filters.in("writeACL", user.getRoles()),
                                                Filters.and(
                                                        eq("readACL", new ArrayList<>()),
                                                        eq("writeACL", new ArrayList<>()))
                                        )))
                                .into(new ArrayList<>());

                        dashboards.forEach(dashboard -> dashboard
                                .setContent(contents.stream().filter(x -> x.getDashboardId().equals(dashboard.getId())).collect(Collectors.toList())));
                        return dashboards;

                    } catch (Exception e) {
                        e.printStackTrace();
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
                                                eq("readACL", new ArrayList<>()),
                                                eq("writeACL", new ArrayList<>()))
                                )
                        );
                        collection.replaceOne(eq("_id", new ObjectId(id)), dashboard);
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
                                                eq("readACL", new ArrayList<>()),
                                                eq("writeACL", new ArrayList<>()))

                                )
                        );
                        collection.deleteOne(eq("_id", new ObjectId(id)));
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

    public CompletableFuture<List<Dashboard>> hierarchy(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    MongoCollection<Dashboard> collection = mongoDB
                            .getMongoDatabase()
                            .getCollection("dashboards", Dashboard.class);

                    List<Bson> pipeline = new ArrayList<>();
                    pipeline.add(Aggregates.match(eq("parentId", new BsonNull())));
                    pipeline.add(Aggregates
                            .graphLookup("dashboards"
                                    , "$_id"
                                    , "_id"
                                    , "parentId"
                                    , "children"
                                    , new GraphLookupOptions().depthField("level")));

                    List<Dashboard> dashboards = collection
                            .aggregate(pipeline, Dashboard.class)
                            .into(new ArrayList<>());
                    List<Dashboard> allDashboards=new ArrayList<>();
                    dashboards.forEach(dashboard -> {
                        allDashboards.add(dashboard);
                        allDashboards.addAll(dashboard.getChildren());
                    });
            System.out.println("ALL DASHBOARDS: "+allDashboards);
            List<ObjectId> allDashboardId=allDashboards
                    .stream()
                    .map(BaseModel::getId)
                    .collect(Collectors.toList());
            List<ContentType> contents=mongoDB.getMongoDatabase().getCollection("content",ContentType.class)
                    .find(Filters.and(
                            Filters.in("dashboardId", allDashboardId),
                            Filters.or(
                                    Filters.in("readACL", user.getId()),
                                    Filters.in("readACL", user.getRoles()),
                                    Filters.in("writeACL", user.getId()),
                                    Filters.in("writeACL", user.getRoles()),
                                    Filters.and(
                                            eq("readACL", new ArrayList<>()),
                                            eq("writeACL", new ArrayList<>()))
                            )))
                    .into(new ArrayList<>());
            allDashboards.forEach(d->
                    d.setContent(contents
                            .stream()
                            .filter(x->
                                    x.getDashboardId()
                                            .equals(d.getId()))
                            .collect(Collectors.toList())));


            if (dashboards.size() == 0) {
                throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Dashboard not found.")));
            }

            dashboards.forEach(dash -> {
                List<Dashboard> list = dash.getChildren();
                List<Dashboard> parentLess = list.stream().filter(x -> x.getLevel() == 0).collect(Collectors.toList());
                parentLess.forEach(x -> addChildren(x, list));
                dash.setChildren(parentLess);
            });


            return dashboards;
                }
        );

    }

    public void addChildren(Dashboard parent, List<Dashboard> child) {
        for (Dashboard d : child) {
            if (parent.getId().equals(d.getParentId())) {
                parent.getChildren().add(d);
                addChildren(d, child);
            }
        }
    }
}