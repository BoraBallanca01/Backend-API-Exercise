package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.Roles;
import io.exercise.api.models.User;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.mongo.IMongoDB;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class RolesService {
    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<Roles> create(Roles roles) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Roles> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("roles", Roles.class);
                        collection.insertOne(roles);
                        return roles;
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Something went wrong."));
                    }
                }
        );
    }

    public CompletableFuture<List<Roles>> read() {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Roles> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("roles", Roles.class);

//                        List<User> users = collection.find().into(new ArrayList<>());
//                        users.forEach(x -> x.setRoles(Arrays.asList(new Roles("62e929003edce75df8122000", "Admin"))));
//                        collection.drop();
//                        collection.insertMany(users);
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

    public CompletableFuture<Roles> update(Roles roles, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Roles> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("roles", Roles.class);
                        collection.replaceOne(Filters.eq("_id", new ObjectId(id)), roles);
                        return roles;
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

    public CompletableFuture<Roles> delete(Roles roles, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Roles> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("roles", Roles.class);
                        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
                        return roles;
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
