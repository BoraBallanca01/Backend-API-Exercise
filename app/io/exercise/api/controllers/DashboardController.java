package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.models.DashboardUser;
import io.exercise.api.services.DashboardServices;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;

public class DashboardController extends Controller {
    @Inject
    SerializationService serializationService;
    @Inject
    DashboardServices service;
//
//    public CompletableFuture<Result> setup(Http.Request request) {
//        return serializationService.parseListBodyOfType(request, DashboardUser.class)
//                .thenCompose(data -> service.setup(data))
//                .thenCompose(data -> serializationService.toJsonNode(data))
//                .thenApply(Results::ok)
//                .exceptionally(DatabaseUtils::throwableToResult);
//    }
//
//
//
    public CompletableFuture<Result> all(Http.Request request) {
        return service.all()
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> save(Http.Request request) {
        return serializationService.parseBodyOfType(request, DashboardUser.class)
                .thenCompose(data -> service.save(data))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardUser.class)
                .thenCompose(data -> service.update(data, id))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);

    }
//
    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, DashboardUser.class)
                .thenCompose(data -> service.delete(data, id))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }


}
