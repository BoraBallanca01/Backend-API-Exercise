package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Roles;
import io.exercise.api.models.User;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class UserService {
    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<User> create(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
                        user.setPassword(Hash.createPassword(user.getPassword()));
//                        user.setRolesId(new Roles().getId());
                        collection.insertOne(user);
                        return user;
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<List<User>> read() {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
                        return collection
                                .find()
                                .into(new ArrayList<>());
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<User> update(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
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

    public CompletableFuture<User> delete(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
                        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
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
