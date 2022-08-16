package io.exercise.api.controllers;

import com.google.inject.Inject;
import io.exercise.api.actions.Authentication;
import io.exercise.api.actions.Validation;
import io.exercise.api.models.content.ContentType;
import io.exercise.api.services.ContentService;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.DatabaseUtils;
import io.exercise.api.utils.ServiceUtils;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.concurrent.CompletableFuture;

@Authentication
public class ContentController {
    @Inject
    SerializationService serializationService;
    @Inject
    ContentService service;


    @Validation(type = ContentType.class)
    public CompletableFuture<Result> create(Http.Request request, String id) {
        return serializationService.parseBodyOfType(request, ContentType.class)
                .thenCompose(data -> service.create(data, id))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> read(Http.Request request, String id) {
        return service.read(ServiceUtils.getUserFrom(request), id)
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    @Validation(type = ContentType.class)
    public CompletableFuture<Result> update(Http.Request request, String id, String contentId) {
        return serializationService.parseBodyOfType(request, ContentType.class)
                .thenCompose(data -> service.update(data, id,contentId, ServiceUtils.getUserFrom(request)))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }

    public CompletableFuture<Result> delete(Http.Request request, String id, String contentId) {
        return serializationService.parseBodyOfType(request, ContentType.class)
                .thenCompose(data -> service.delete(data, id,contentId, ServiceUtils.getUserFrom(request)))
                .thenCompose(data -> serializationService.toJsonNode(data))
                .thenApply(Results::ok)
                .exceptionally(DatabaseUtils::throwableToResult);
    }
}
