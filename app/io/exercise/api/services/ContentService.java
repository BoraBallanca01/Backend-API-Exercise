package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.ServiceUtils;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ContentService {
    @Inject
    IMongoDB mongoDB;

    public CompletableFuture<ContentType> create(ContentType content,String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<ContentType> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("content", ContentType.class);

                        content.setDashboardId(new ObjectId(id));
                        collection.insertOne(content);
                        return content;
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Check again, internal server error!"));
                    }
                }
        );
    }

    public CompletableFuture<List<ContentType>> read(User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<ContentType> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("content", ContentType.class);

                        collection.find(ServiceUtils.readACL(user));
                        return collection
                                .find(Filters.eq("dashboardId",id))
                                .into(new ArrayList<>());
                    } catch (Exception e) {
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Check again, internal server error!"));
                    }
                }
        );
    }

    public CompletableFuture<ContentType> update(ContentType content,String id, String contentId,User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<ContentType> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("content", ContentType.class);

                        collection.find(Filters.eq("dashboardId",id));
                        collection.find(ServiceUtils.writeACL(user));
                        collection.replaceOne(Filters.eq("_id", new ObjectId(contentId)), content);
                        return content;
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

    public CompletableFuture<ContentType> delete(ContentType content,String id, String contentId,User user) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<ContentType> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("content", ContentType.class);
                        collection.find(Filters.eq("dashboardId",id));
                        collection.find(ServiceUtils.writeACL(user));
                        collection.deleteOne(Filters.eq("_id",  new ObjectId(contentId)));
                        return content;
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
