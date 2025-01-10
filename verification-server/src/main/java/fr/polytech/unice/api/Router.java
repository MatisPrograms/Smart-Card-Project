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

    private static final Map<String, BiConsumer<HttpExchange, String>> routes = new HashMap<>();
    private final VerificationServer verificationServer;

    public Router(VerificationServer verificationServer) {
        this.verificationServer = verificationServer;
        Router.GET("/", (exchange, path) -> {
            JsonObject responseJson = new JsonObject();
            responseJson.addProperty("message", "Welcome to the Verification Server!");
            Router.respond(exchange, responseJson);
        });
    }

    private static void route(String method, String path, BiConsumer<HttpExchange, String> callback) {
        Router.routes.put(method + Router.SEPARATOR + path, callback);
    }

    public static void GET(String path, BiConsumer<HttpExchange, String> callback) {
        Router.route("GET", path, callback);
    }

    public static void POST(String path, BiConsumer<HttpExchange, String> callback) {
        Router.route("GET", path, callback);
    }

    public static void PUT(String path, BiConsumer<HttpExchange, String> callback) {
        Router.route("GET", path, callback);
    }

    public static void DELETE(String path, BiConsumer<HttpExchange, String> callback) {
        Router.route("DELETE", path, callback);
    }

    // Utility method to respond to the client
    public static void respond(HttpExchange exchange, String response) {
        Router.respond(exchange, response, 200);
    }

    // Utility method to respond to the client
    public static void respond(HttpExchange exchange, JsonObject response) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        Router.respond(exchange, gson.toJson(response), 200);
    }

    // Overloaded method for custom status codes
    public static void respond(HttpExchange exchange, String response, int statusCode) {
        try (exchange) {
            exchange.sendResponseHeaders(statusCode, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Overloaded method for custom status codes
    public static void respond(HttpExchange exchange, JsonObject response, int statusCode) {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        Router.respond(exchange, gson.toJson(response), statusCode);
    }

    @SuppressWarnings("unused")
    public VerificationServer getVerificationServer() {
        return this.verificationServer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String key = method + Router.SEPARATOR + path;

        BiConsumer<HttpExchange, String> callback = routes.get(key);
        if (callback != null) callback.accept(exchange, path);
        else Router.respond(exchange, "404 Not Found", 404);
    }
}
