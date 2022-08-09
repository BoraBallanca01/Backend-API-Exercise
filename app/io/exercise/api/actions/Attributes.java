package io.exercise.api.actions;

import io.exercise.api.models.AuthenticatedUser;
import io.exercise.api.models.User;
import play.libs.typedmap.TypedKey;

/**
 * Created by Agon on 09/08/2020
 */
public class Attributes {
    public static final TypedKey<User> USER_TYPED_KEY = TypedKey.<User>create("user");
    public static final TypedKey<AuthenticatedUser> AUTH_USER_TYPED_KEY = TypedKey.<AuthenticatedUser>create("auth");
}
