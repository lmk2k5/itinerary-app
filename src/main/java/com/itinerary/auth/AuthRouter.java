package com.itinerary.auth;

import com.itinerary.db.MongoService;
import com.itinerary.services.MailService;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.mindrot.jbcrypt.BCrypt;

import java.security.SecureRandom;
import java.util.Base64;

public class AuthRouter {

    private final MongoClient mongo;
    private final JwtUtil jwtUtil;
    private final MailService mailService;

    public AuthRouter(MongoService mongoService, JwtUtil jwtUtil, MailService mailService) {
        this.mongo = mongoService.getClient();
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
    }

    public void registerRoutes(Router router) {
        router.post("/api/signup").handler(this::handleSignup);
        router.post("/api/login").handler(this::handleLogin);
    }

    private void handleSignup(RoutingContext ctx) {
        System.out.println("➡️ /api/signup endpoint hit");
        System.out.println("Request body: " + ctx.body().asString());

        JsonObject body = ctx.body().asJsonObject();
        String name = body.getString("name");
        String rawEmail = body.getString("email");

        if (name == null || rawEmail == null) {
            System.out.println("❌ Missing name or email");
            ctx.response().setStatusCode(400).end("Missing name or email");
            return;
        }

        final String email = rawEmail.trim().toLowerCase();

        System.out.println("DEBUG: Raw email from body: '" + rawEmail + "'");
        System.out.println("DEBUG: Cleaned email: '" + email + "'");
        JsonObject query = new JsonObject().put("email", email);
        System.out.println("DEBUG: MongoDB query: " + query.encode());

        mongo.findOne("users", query, null, res -> {
            if (res.succeeded() && res.result() != null) {
                System.out.println("⚠️ User already exists with email: " + email);
                ctx.response().setStatusCode(409).end("User with this email already exists");
            } else {
                String plainPassword = generatePassword(10);
                String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());

                JsonObject newUser = new JsonObject()
                        .put("name", name)
                        .put("email", email)
                        .put("password", hashedPassword);

                mongo.insert("users", newUser, insertRes -> {
                    if (insertRes.succeeded()) {
                        System.out.println("✅ User inserted into MongoDB: " + email);
                        mailService.sendPasswordEmail(email, plainPassword)
                                .onSuccess(v -> {
                                    System.out.println("📧 Password email sent to: " + email);
                                    ctx.response().end("User created and password emailed");
                                })
                                .onFailure(err -> {
                                    err.printStackTrace();
                                    System.out.println("❌ Failed to send email to: " + email);
                                    ctx.response().setStatusCode(500).end("User created but failed to send email");
                                });
                    } else {
                        System.out.println("❌ Failed to insert user into MongoDB: " + insertRes.cause());
                        ctx.response().setStatusCode(500).end("Failed to save user to database");
                    }
                });
            }
        });
    }

    private void handleLogin(RoutingContext ctx) {
        System.out.println("➡️ /api/login endpoint hit");
        System.out.println("Request body: " + ctx.body().asString());

        JsonObject body = ctx.body().asJsonObject();
        String rawEmail = body.getString("email");
        String password = body.getString("password");

        if (rawEmail == null || password == null) {
            System.out.println("❌ Missing email or password");
            ctx.response().setStatusCode(400).end("Missing email or password");
            return;
        }

        final String email = rawEmail.trim().toLowerCase();

        System.out.println("DEBUG: Raw email from body: '" + rawEmail + "'");
        System.out.println("DEBUG: Cleaned email: '" + email + "'");
        JsonObject query = new JsonObject().put("email", email);
        System.out.println("DEBUG: MongoDB query: " + query.encode());

        mongo.findOne("users", query, null, res -> {
            if (res.succeeded() && res.result() != null) {
                String storedHash = res.result().getString("password");
                if (BCrypt.checkpw(password, storedHash)) {
                    String token = jwtUtil.generateToken(email);
                    System.out.println("✅ Login successful for " + email);
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("token", token).encode());
                } else {
                    System.out.println("❌ Invalid password for: " + email);
                    ctx.response().setStatusCode(401).end("Invalid credentials");
                }
            } else {
                System.out.println("❌ User not found with email: " + email);
                ctx.response().setStatusCode(401).end("Invalid credentials");
            }
        });
    }

    private String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, length);
    }
}
