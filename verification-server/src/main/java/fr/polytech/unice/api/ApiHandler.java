package fr.polytech.unice.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ApiHandler implements HttpHandler {
    private static final List<Route> routes = new ArrayList<>();

    private static void route(String method, String path, Function<Request, Response> callback) {
        routes.add(new Route(path, method, callback));
    }

    public static void get(String path, Function<Request, Response> callback) {
        ApiHandler.route("GET", path, callback);
    }

    public static void post(String path, Function<Request, Response> callback) {
        ApiHandler.route("GET", path, callback);
    }

    public static void put(String path, Function<Request, Response> callback) {
        ApiHandler.route("GET", path, callback);
    }

    public static void delete(String path, Function<Request, Response> callback) {
        ApiHandler.route("DELETE", path, callback);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Response response = null;

        for (Route route : routes) {
            if (exchange.getRequestMethod().equals(route.method()) && exchange.getRequestURI().getPath().equals(route.path())) {
                response = route.callback().apply(Request.from(exchange));
                break;
            }
        }

        if (response == null) {
            exchange.sendResponseHeaders(404, 0);
            exchange.close();
            return;
        }

        exchange.sendResponseHeaders(response.statusCode(), response.body().toString().length());
        exchange.getResponseBody().write(response.body().toString().getBytes());
        exchange.close();
    }
}
