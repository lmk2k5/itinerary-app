package com.itinerary.models;

import io.vertx.core.json.JsonObject;

public class User {
    private String id;
    private String name;
    private String email;
    private String passwordHash;

    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public JsonObject toMongoDoc() {
        return new JsonObject()
                .put("name", name)
                .put("email", email)
                .put("password", passwordHash);
    }

    public static User fromMongoDoc(JsonObject doc) {
        return new User(
                doc.getString("name"),
                doc.getString("email"),
                doc.getString("password")
        );
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPasswordHash() { return passwordHash; }
}
