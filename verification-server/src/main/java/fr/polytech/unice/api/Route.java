package fr.polytech.unice.api;

import java.util.function.Function;

public record Route(String path, String method, Function<Request, Response> callback) {
}
