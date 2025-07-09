package com.itinerary.services;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;


public class MailService {

    private final MailClient mailClient;
    private final String fromEmail;

    public MailService(Vertx vertx, String host, int port, String username, String password, boolean isStartTLS, String fromEmail) {
        this.fromEmail = fromEmail;

        MailConfig config = new MailConfig()
                .setHostname(host)
                .setPort(port)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername(username)
                .setPassword(password);

        this.mailClient = MailClient.create(vertx, config);
    }

    public Future<Void> sendPasswordEmail(String toEmail, String password) {
        MailMessage message = new MailMessage()
                .setFrom(fromEmail)
                .setTo(toEmail)
                .setSubject("Your Itinerary App Password")
                .setText("Welcome to Itinerary App!\n\nYour password is: " + password);

        return mailClient.sendMail(message).mapEmpty();
    }
}
