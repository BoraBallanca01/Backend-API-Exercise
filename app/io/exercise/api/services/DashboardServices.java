package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.DashboardUser;
import io.exercise.api.mongo.IMongoDB;

import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class DashboardServices {
    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<DashboardUser> save(DashboardUser user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<DashboardUser> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("users", DashboardUser.class);
                        collection.insertOne(user);
                        return user;
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<List<DashboardUser>> all() {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<DashboardUser> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("users", DashboardUser.class);
                        return collection
                                .find()
                                .into(new ArrayList<>());
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<DashboardUser> update(DashboardUser user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<DashboardUser> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("users", DashboardUser.class);
                        collection.replaceOne(Filters.eq("_id", new ObjectId(id)), user);
                        return user;
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

    public CompletableFuture<DashboardUser> delete(DashboardUser user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<DashboardUser> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("users", DashboardUser.class);
                        collection.deleteOne(Filters.eq("_id",  new ObjectId(id)));
                        return user;
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
