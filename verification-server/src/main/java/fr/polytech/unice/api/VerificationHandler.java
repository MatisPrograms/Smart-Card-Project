package fr.polytech.unice.api;

public class VerificationHandler {

    public VerificationHandler() {
        ApiHandler.get("/", (request) -> new Response(200, "Verification server is running..."));
        ApiHandler.get("/verify", (request) -> new Response(200, "Verification successful!"));
    }
}
