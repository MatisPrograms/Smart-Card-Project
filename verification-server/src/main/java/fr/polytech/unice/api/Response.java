package fr.polytech.unice.api;

public record Response(int statusCode, Object body) {
    public Response {
        if (statusCode < 100 || statusCode > 599) {
            throw new IllegalArgumentException("Invalid status code");
        }
    }
}
