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

/**
 * CRUD methods for a user
 */
public class UserService {
    @Inject
    IMongoDB mongoDB;

    /**
     * Method creates a user.
     *
     * @param user
     * @return the created user
     */
    public CompletableFuture<User> create(User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
                        user.setPassword(Hash.createPassword(user.getPassword()));
                        collection.insertOne(user);
                        return user;
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Check again, internal server error!"));
                    }
                }
        );
    }

    /**
     * Method reads and shows all existing users.
     *
     * @param
     * @return the list of existing users
     */
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
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Check again, internal server error!"));
                    }
                }
        );
    }

    /**
     * Method updates a user based on its id.
     *
     * @param user
     * @param id
     * @return the updated user
     */
    public CompletableFuture<User> update(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
                        collection.replaceOne(Filters.eq("_id", new ObjectId(id)), user);
                        return user;
                    } catch (NullPointerException | CompletionException e) {
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Make sure your request is right!")));
                    } catch (IllegalArgumentException e) {
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Oops, not found!")));
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Check again, internal server error!")));
                    }
                }
        );
    }

    /**
     * Method deletes a user based on its id.
     * @param user
     * @param id
     * @return the deleted user
     * */
    public CompletableFuture<User> delete(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("user", User.class);
                        collection.deleteOne(Filters.eq("_id", new ObjectId(id)));
                        return user;
                    } catch (NullPointerException | CompletionException e) {
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Make sure your request is right!")));
                    } catch (IllegalArgumentException e) {
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("Oops, not found!")));
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, Json.toJson("Check again, internal server error!")));
                    }

                }
        );
    }

}
