package com.itinerary.auth;

import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class JwtUtil {

    private final JWTAuth jwtAuth;

    public JwtUtil(Vertx vertx, String jwtSecret) {
        // Configure JWT using the correct format for Vert.x 4+
        JsonObject config = new JsonObject()
                .put("key-store", new JsonObject()
                        .put("type", "secret")
                        .put("value", jwtSecret)
                        .put("algorithm", "HS256"));

        // Create JWTAuthProvider
        this.jwtAuth = JWTAuth.create(vertx, new JWTAuthOptions(config));
    }

    public String generateToken(String userId) {
        // Use "sub" (subject) field for standard compliance
        JsonObject claims = new JsonObject()
                .put("sub", userId);

        return jwtAuth.generateToken(claims);
    }

    public JWTAuth getProvider() {
        return jwtAuth;
    }
}