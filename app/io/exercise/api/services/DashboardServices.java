package io.exercise.api.services;

import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.GraphLookupOptions;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.BaseModel;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.mongo.IMongoDB;

import io.exercise.api.utils.ServiceUtils;
import org.bson.BsonNull;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;
import com.mongodb.client.model.Filters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

public class DashboardServices {
    @Inject
    IMongoDB mongoDB;

    /**
     * Method creates a dashboard.
     *
     * @param dashboard
     * @return the created dashboard
     */
    public CompletableFuture<Dashboard> create(Dashboard dashboard) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> dashboardCollection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        dashboardCollection.insertOne(dashboard);
                        return dashboard;
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Check again, internal server error!"));
                    }
                }
        );
    }

    /**
     * Method reads and shows all existing dashboards with their contents, with parameters of limit and skip for pagination.
     *
     * @param user
     * @param limit
     * @param skip
     * @return a list of all existing dashboards
     */
    public CompletableFuture<List<Dashboard>> read(User user, int limit, int skip) {
        return CompletableFuture.supplyAsync(() -> {
                    try {

                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        List<Dashboard> dashboards = collection
                                .find(ServiceUtils.readACL(user))
                                .skip(skip)
                                .limit(limit)
                                .into(new ArrayList<>());

                        List<String> dashId = dashboards
                                .stream()
                                .map(x -> x.getId().toString())
                                .collect(Collectors.toList());

                        List<ContentType> contents = mongoDB.getMongoDatabase()
                                .getCollection("content", ContentType.class)
                                .find(Filters.and(
                                        in("dashboardId", dashId.stream().map(ObjectId::new).collect(Collectors.toList())),
                                        ServiceUtils.readACL(user)
                                ))
                                .into(new ArrayList<>());

                        dashboards.forEach(dashboard -> dashboard
                                .setContent(contents
                                        .stream()
                                        .filter(x -> x.getDashboardId().equals(dashboard.getId()))
                                        .collect(Collectors.toList())));
                        return dashboards;

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.INTERNAL_SERVER_ERROR, "Check again, internal server error!"));
                    }
                }
        );
    }

    /**
     * Method updates a given dashboard, based on its id and the user's access
     *
     * @param dashboard
     * @param user
     * @param id        the dashboard id
     * @return the updated dashboard
     */

    public CompletableFuture<Dashboard> update(Dashboard dashboard, User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        collection.find(ServiceUtils.writeACL(user));
                        collection.replaceOne(eq("_id", new ObjectId(id)), dashboard);
                        return dashboard;
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
     * Method deletes dashboard using its id and the user's access.
     *
     * @param dashboard
     * @param user
     * @param id
     * @return the deleted user
     */
    public CompletableFuture<Dashboard> delete(Dashboard dashboard, User user, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<Dashboard> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("dashboards", Dashboard.class);
                        collection.find(ServiceUtils.writeACL(user));
                        collection.deleteOne(eq("_id", new ObjectId(id)));
                        return dashboard;
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
     * Method displays dashboards in a hierarchy, and distributes the contents to each corresponding dashboard.
     *
     * @param user
     * @return the list of dashboards
     */
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
                    List<Dashboard> allDashboards = new ArrayList<>();
                    dashboards.forEach(dashboard -> {
                        allDashboards.add(dashboard);
                        allDashboards.addAll(dashboard.getChildren());
                    });

                    List<ObjectId> allDashboardId = allDashboards
                            .stream()
                            .map(BaseModel::getId)
                            .collect(Collectors.toList());

                    List<ContentType> contents = mongoDB
                            .getMongoDatabase()
                            .getCollection("content", ContentType.class)
                            .find(Filters.and(
                                            in("dashboardId", allDashboardId),
                                            ServiceUtils.readACL(user)
                                    )
                            )
                            .into(new ArrayList<>());
                    allDashboards.forEach(d ->
                            d.setContent(contents
                                    .stream()
                                    .filter(x ->
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

    /**
     * The recursive method to display dashboards in a hierarchy.
     *
     * @param parent - the dashboard that has a null parent id
     * @param child  - the list of dashboards that need to be added to the parent dashboard
     */
    public void addChildren(Dashboard parent, List<Dashboard> child) {
        for (Dashboard d : child) {
            if (parent.getId().equals(d.getParentId())) {
                parent.getChildren().add(d);
                addChildren(d, child);
            }
        }
    }



}