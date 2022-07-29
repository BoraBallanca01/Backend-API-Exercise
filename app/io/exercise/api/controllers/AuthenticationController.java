package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.models.AuthenticatedUser;
import io.exercise.api.services.AuthenticationService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import java.util.concurrent.CompletableFuture;

public class AuthenticationController {
    @Inject
    SerializationService serializationService;
    @Inject
    AuthenticationService service;

    public CompletableFuture<Result> authenticate (Http.Request request) {
        return serializationService.parseBodyOfType(request, AuthenticatedUser.class)
                .thenCompose(data -> service.authenticate(data))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> verify (Http.Request request) {
        return service.verify(ServiceUtils.getTokenFromRequest(request))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
