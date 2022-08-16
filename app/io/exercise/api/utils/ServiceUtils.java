package io.exercise.api.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.actions.Attributes;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.AuthenticatedUser;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;

public class ServiceUtils {

    public static String getTokenFromRequest(Http.Request request) {
        Optional<String> optionalToken = request.getHeaders().get("token");
        return optionalToken.orElse(null);
    }

    public static User getUserFrom(Http.Request request) {
        return request.attrs().get(Attributes.USER_TYPED_KEY);
    }

    /**
     * Method decodes token.
     * @param token
     * */
    public static CompletableFuture<String> decodeToken(String token) {
        return CompletableFuture.supplyAsync(() -> {
                    byte[] decoded = Base64
                            .getDecoder()
                            .decode(token.split("\\.")[1]);
                    String decodedString = new String(decoded);
                    JsonNode node = play.libs.Json.parse(decodedString);

                    return node.get("iss").asText();
                }
        );
    }
/**
 * Method gets user based on their id.
 * @param mongoDB
 * @param id
 * */
    public static CompletableFuture<User> getUserFromId(IMongoDB mongoDB, String id) {
        return CompletableFuture.supplyAsync(() -> {
                    MongoCollection<User> collection = mongoDB
                            .getMongoDatabase()
                            .getCollection("user", User.class);
                    User user = collection.find(Filters.eq("_id", new ObjectId(id))).first();

                    if (user == null) {
                        throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
                    }
                    return user;
                }
        );
    }
/**
 * Method verifies token for a specific user.
 * @param user
 * @param token
 * @param config
 * */
    public static CompletableFuture<User> verify(User user, String token, Config config) {
        return CompletableFuture.supplyAsync(() -> {
                    String secret = config.getString("play.http.secret.key");
                    Algorithm algorithm = null;
                    try {
                        algorithm = Algorithm.HMAC256(secret);
                    } catch (UnsupportedEncodingException e) {
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, e.getMessage()));
                    }
                    JWTVerifier verifier = JWT.require(algorithm)
                            .withIssuer(user.getId().toString())
                            .build();
                    verifier.verify(token);
                    return user;
                }
        );
    }

    /**
     * Method to filter users based on their ids in read/write ACL.
     */
    public static Bson readACL(User requestingUser) {
        return Filters.or(
                in("readACL", requestingUser.getId().toString()),
                in("readACL", requestingUser.getRoles()),
                in("writeACL", requestingUser.getId().toString()),
                in("writeACL", requestingUser.getRoles()),
                Filters.and(
                        eq("readACL", new ArrayList<>()),
                        eq("writeACL", new ArrayList<>())),
                Filters.and(
                        eq("readACL", List.of("*")),
                        eq("writeACL", List.of("*"))));
    }

    /**
     * Method to filter users based on their ids in write ACL.
     */
    public static Bson writeACL(User requestingUser) {
        return Filters.or(
                in("writeACL", requestingUser.getId().toString()),
                in("writeACL", requestingUser.getRoles()),
                Filters.and(
                        eq("readACL", new ArrayList<>()),
                        eq("writeACL", new ArrayList<>())),
                Filters.and(
                        eq("readACL", List.of("*")),
                        eq("writeACL", List.of("*"))));
    }
}

