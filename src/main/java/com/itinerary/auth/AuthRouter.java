package com.itinerary.auth;

import com.itinerary.db.MongoService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.mongo.MongoClient;
import org.mindrot.jbcrypt.BCrypt;

public class AuthRouter {

    private final MongoClient mongo;
    private final JwtUtil jwtUtil;

    public AuthRouter(MongoService mongoService, JwtUtil jwtUtil) {
        this.mongo = mongoService.getClient();
        this.jwtUtil = jwtUtil;
    }

    public void registerRoutes(Router router) {
        router.post("/api/signup").handler(this::handleSignup);
        router.post("/api/login").handler(this::handleLogin);
    }

    private void handleSignup(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");

        if (username == null || password == null) {
            ctx.response().setStatusCode(400).end("Missing username or password");
            return;
        }

        JsonObject query = new JsonObject().put("username", username);
        mongo.findOne("users", query, null, res -> {
            if (res.succeeded() && res.result() != null) {
                ctx.response().setStatusCode(409).end("Username already exists");
            } else {
                String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                JsonObject newUser = new JsonObject()
                        .put("username", username)
                        .put("password", hashedPassword);

                mongo.insert("users", newUser, insertRes -> {
                    if (insertRes.succeeded()) {
                        ctx.response().setStatusCode(201).end("User created");
                    } else {
                        ctx.response().setStatusCode(500).end("Database error");
                    }
                });
            }
        });
    }

    private void handleLogin(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String username = body.getString("username");
        String password = body.getString("password");

        if (username == null || password == null) {
            ctx.response().setStatusCode(400).end("Missing username or password");
            return;
        }

        JsonObject query = new JsonObject().put("username", username);

        mongo.findOne("users", query, null, res -> {
            if (res.succeeded() && res.result() != null) {
                String storedHash = res.result().getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    String token = jwtUtil.generateToken(username);
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("token", token).encode());
                } else {
                    ctx.response().setStatusCode(401).end("Invalid credentials");
                }
            } else {
                ctx.response().setStatusCode(401).end("Invalid credentials");
            }
        });
    }
}
