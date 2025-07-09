package com.itinerary;

import com.itinerary.auth.AuthRouter;
import com.itinerary.auth.JwtUtil;
import com.itinerary.db.MongoService;
import com.itinerary.services.MailService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {

        // Step 1: Load config.json
        ConfigStoreOptions fileStore = new ConfigStoreOptions()
                .setType("file")
                .setFormat("json")
                .setConfig(new JsonObject().put("path", "config.json"));

        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions().addStore(fileStore);
        ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOptions);

        retriever.getConfig(configResult -> {
            if (configResult.failed()) {
                startPromise.fail(configResult.cause());
                return;
            }

            JsonObject config = configResult.result();

            // Step 2: Create core services
            MongoService mongoService = new MongoService(vertx, config.getString("mongo_uri"));
            JwtUtil jwtUtil = new JwtUtil(vertx, config.getString("jwt_secret"));
            MailService mailService = new MailService(
                    vertx,
                    config.getString("smtp_host"),
                    config.getInteger("smtp_port"),
                    config.getString("smtp_user"),
                    config.getString("smtp_pass"),
                    true,
                    config.getString("smtp_from")
            );

            // Step 3: Create router and register handlers
            Router router = Router.router(vertx);
            router.route().handler(BodyHandler.create());

            // Register Auth Routes
            new AuthRouter(mongoService, jwtUtil, mailService).registerRoutes(router);

            // Test mail route (optional)
            router.get("/test-mail").handler(ctx -> {
                mailService.sendPasswordEmail("lohithm978@gmail.com", "testpassword123")
                        .onSuccess(v -> ctx.response().end("✅ Mail sent successfully"))
                        .onFailure(err -> {
                            err.printStackTrace();
                            ctx.response().setStatusCode(500).end("❌ Mail failed: " + err.getMessage());
                        });
            });

            // Serve static HTML from webroot/
            router.route("/*").handler(StaticHandler.create("webroot"));

            // Optional clean routes to serve /signup and /login
            router.get("/signup").handler(ctx -> ctx.response().sendFile("webroot/signup.html"));
            router.get("/login").handler(ctx -> ctx.response().sendFile("webroot/login.html"));

            // Step 4: Start the server
            int port = config.getInteger("port", 8888);
            vertx.createHttpServer()
                    .requestHandler(router)
                    .listen(port)
                    .onSuccess(server -> {
                        System.out.println("✅ Server started on port " + port);
                        startPromise.complete();
                    })
                    .onFailure(startPromise::fail);
        });
    }
}
