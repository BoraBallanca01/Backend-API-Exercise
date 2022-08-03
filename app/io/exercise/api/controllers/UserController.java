package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.actions.Authentication;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.Dashboard;
import io.exercise.api.models.User;
import io.exercise.api.services.DashboardServices;
import io.exercise.api.services.SerializationService;
import io.exercise.api.services.UserService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;

public class UserController extends Controller {
    @Inject
    SerializationService serializationService;
    @Inject
    UserService service;

    @Validation(type= User.class)
    public CompletableFuture<Result> create(Http.Request request) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(data -> service.create(data))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> read(Http.Request request) {
        return service.read()
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
    @Validation(type=Dashboard.class)
    public CompletableFuture<Result> update(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(data -> service.update(data, id))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);

    }

    public CompletableFuture<Result> delete(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, User.class)
                .thenCompose(data -> service.delete(data, id))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }


}