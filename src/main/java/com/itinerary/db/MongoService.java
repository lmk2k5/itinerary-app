package com.itinerary.db;

import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.json.JsonObject;

public class MongoService {
    private final MongoClient client;

    public MongoService(Vertx vertx, String mongoUri) {
        JsonObject config = new JsonObject()
                .put("connection_string", mongoUri)
                .put("db_name", "itinerary-app");
        this.client = MongoClient.createShared(vertx, config);
    }

    public MongoClient getClient() {
        return client;
    }
}
