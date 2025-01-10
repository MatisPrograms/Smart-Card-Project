package fr.polytech.unice.api;

public enum HTTP_METHOD {
    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String method;

    HTTP_METHOD(String method) {
        this.method = method;
    }
}
