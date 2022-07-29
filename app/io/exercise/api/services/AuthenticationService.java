package io.exercise.api.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.typesafe.config.Config;
import io.exercise.api.exceptions.RequestException;
import io.exercise.api.models.AuthenticatedUser;
import io.exercise.api.models.User;
import io.exercise.api.mongo.IMongoDB;
import io.exercise.api.utils.Hash;
import org.bson.types.ObjectId;
import play.libs.Json;
import play.mvc.Http;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthenticationService {
    @Inject
    IMongoDB mongoDB;

    @Inject
    Config config;

    public CompletableFuture<String> authenticate(AuthenticatedUser authUser) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("authentication", User.class);

                        User user = collection
                                .find(Filters.or(
                                                Filters.eq("username", authUser.getUsername()),
                                                Filters.eq("password", authUser.getPassword()))
                                ).first();

                        if (user == null) {
                            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
                        }

                        if (!Hash.checkPassword(authUser.getPassword(), user.getPassword())) {
                            throw new CompletionException(new RequestException(Http.Status.UNAUTHORIZED, Json.toJson("You are not authorized! Password Related")));
                        }

                        String secret = config.getString("play.http.secret.key");
                        Algorithm algorithm = Algorithm.HMAC256(secret);
                        return JWT.create()
                                .withIssuer(user.getId().toString())
                                .sign(algorithm);
                    } catch (JWTCreationException ex) {
                        ex.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid Signing configuration / Couldn't convert Claims.")));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.UNAUTHORIZED, Json.toJson("You are not authorized! Catch Related")));
                    }
                }
        );
    }

    public CompletableFuture<User> verify (String token) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        MongoCollection<User> collection = mongoDB
                                .getMongoDatabase()
                                .getCollection("authentication", User.class);

                        byte[] decoded = Base64.getDecoder().decode(token.split("\\.")[1]);
                        String decodedString = new String(decoded);
                        JsonNode jsonNode = Json.parse(decodedString);
                        String id = jsonNode.get("iss").asText();

                        User user = collection
                                .find(Filters.eq("_id", new ObjectId(id))).first();

                        if (user == null) {
                            throw new CompletionException(new RequestException(Http.Status.NOT_FOUND, Json.toJson("User not found!")));
                        }

//                        if (!Hash.checkPassword(authUser.getPassword(), user.getPassword())) {
//                            throw new CompletionException(new RequestException(Http.Status.UNAUTHORIZED, Json.toJson("You are not authorized! Password Related")));
//                        }

                        String secret = config.getString("play.http.secret.key");
                        Algorithm algorithm = Algorithm.HMAC256(secret);
                        JWTVerifier verifier = JWT.require(algorithm)
                                .withIssuer(user.getId().toString())
                                .build();
                        verifier.verify(token);

                        return user;
                    } catch (JWTCreationException ex) {
                        ex.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.BAD_REQUEST, Json.toJson("Invalid Signing configuration / Couldn't convert Claims.")));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new CompletionException(new RequestException(Http.Status.UNAUTHORIZED, Json.toJson("You are not authorized! Catch Related")));
                    }
                }
        );
    }
}
