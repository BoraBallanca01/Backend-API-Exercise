package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.mongo.IMongoDB;

import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
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
                                                .stream().filter(x->x.getDashboardId().equals(dashboard.getId())).collect(Collectors.toList())));
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

}
