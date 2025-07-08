package com.itinerary;

import com.itinerary.auth.AuthRouter;
import com.itinerary.auth.JwtUtil;
import com.itinerary.db.MongoService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.BodyHandler;

import java.nio.file.Files;
import java.nio.file.Paths;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        JsonObject config = new JsonObject(Files.readString(Paths.get("src/main/resources/config.json")));

        String mongoUri = config.getString("mongodbUri");
        String jwtSecret = config.getString("jwtSecret");
        int port = config.getInteger("serverPort");

        MongoService mongoService = new MongoService(vertx, mongoUri);
        JwtUtil jwtUtil = new JwtUtil(vertx, jwtSecret);
        AuthRouter authRouter = new AuthRouter(mongoService, jwtUtil);

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        authRouter.registerRoutes(router);

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(port)
                .onSuccess(server -> {
                    System.out.println("Server running on http://localhost:" + port);
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }
}
