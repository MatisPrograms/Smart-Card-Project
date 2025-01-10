package fr.polytech.unice.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.polytech.unice.VerificationServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class Router implements HttpHandler {
    private static final Gson gson = new Gson();
    private static final String SEPARATOR = "::";

    private static final Map<String, BiConsumer<HttpExchange, CryptographicWallet>> routes = new HashMap<>();
    private final CryptographicWallet wallet;

    public Router(VerificationServer verificationServer) {
        this.wallet = new CryptographicWallet(verificationServer.getKeyPair(), verificationServer.getCardsPublicKey(), false);

        Router.GET("/", (exchange, path) -> {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "Welcome to the Verification Server!");
            Router.respond(exchange, this.wallet, responseJson);
        });
    }

    @SuppressWarnings("unused")
    private static void route(HTTP_METHOD method, String path, BiConsumer<HttpExchange, CryptographicWallet> callback) {
        Router.routes.put(method + Router.SEPARATOR + path, callback);
    }

    @SuppressWarnings("unused")
    public static void GET(String path, BiConsumer<HttpExchange, CryptographicWallet> callback) {
        Router.route(HTTP_METHOD.GET, path, callback);
    }

    @SuppressWarnings("unused")
    public static void POST(String path, BiConsumer<HttpExchange, CryptographicWallet> callback) {
        Router.route(HTTP_METHOD.POST, path, callback);
    }

    @SuppressWarnings("unused")
    public static void PUT(String path, BiConsumer<HttpExchange, CryptographicWallet> callback) {
        Router.route(HTTP_METHOD.PUT, path, callback);
    }

    @SuppressWarnings("unused")
    public static void DELETE(String path, BiConsumer<HttpExchange, CryptographicWallet> callback) {
        Router.route(HTTP_METHOD.DELETE, path, callback);
    }

    @SuppressWarnings("unused")
    public static void respond(HttpExchange exchange, CryptographicWallet wallet, String response) {
        Router.respond(exchange, wallet, response, 200);
    }

    @SuppressWarnings("unused")
    public static void respond(HttpExchange exchange, CryptographicWallet wallet, String response, int statusCode) {
        try (exchange) {
            byte[] responseBytes = wallet.getBytes(response);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            exchange.getResponseBody().write(responseBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unused")
    public static void respond(HttpExchange exchange, CryptographicWallet wallet, JsonObject response) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        Router.respond(exchange, wallet, gson.toJson(response), 200);
    }

    @SuppressWarnings("unused")
    public static void respond(HttpExchange exchange, CryptographicWallet wallet, JsonObject response, int statusCode) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        Router.respond(exchange, wallet, gson.toJson(response), statusCode);
    }

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String key = method + Router.SEPARATOR + path;

        BiConsumer<HttpExchange, CryptographicWallet> callback = routes.get(key);
        if (callback != null) callback.accept(exchange, this.wallet);
        else Router.respond(exchange, this.wallet, "404 Not Found", 404);
    }
}
