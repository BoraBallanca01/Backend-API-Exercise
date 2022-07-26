package io.exercise.api.actions;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.services.SerializationService;
import io.exercise.api.utils.ServiceUtils;

import play.libs.Json;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

public class AuthenticationAction extends Action<Authentication> {

    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;


    /**
     * Method verifies a token for a given user!
     * */
    @Override
    public CompletionStage<Result> call(Http.Request request) {
        try {
            String token = ServiceUtils.getTokenFromRequest(request);
            User user = ServiceUtils.decodeToken(token)
                    .thenCompose(x -> ServiceUtils.getUserFromId(mongoDB, x))
                    .thenCompose(x -> ServiceUtils.verify(x, token, config))
                    .join();

            request = request.addAttr(Attributes.USER_TYPED_KEY, user);
            return delegate.call(request);
        } catch (JWTCreationException ex) {
            ex.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid Signing configuration / Couldn't convert Claims.")));
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new CompletionException(new RequestException(Http.Status.UNAUTHORIZED, Json.toJson("You are not authorized!")));
        }
    }
}
