package fr.polytech.unice.api;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.net.URI;

public record Request(
        Headers headers,
        URI uri,
        String method,
        String body) {
    public static Request from(HttpExchange exchange) {
        return new Request(exchange.getRequestHeaders(), exchange.getRequestURI(), exchange.getRequestMethod(), exchange.getRequestBody().toString());
    }
}
