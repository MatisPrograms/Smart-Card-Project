package fr.polytech.unice;

import com.sun.net.httpserver.HttpServer;
import fr.polytech.unice.api.ApiHandler;
import fr.polytech.unice.api.VerificationHandler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class VerificationServer {

    private final HttpServer server;

    public VerificationServer() throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(8000), 0);
        new VerificationHandler();
        this.server.createContext("/", new ApiHandler());
    }

    public void start() {
        System.out.println("Verification server is running...");
        this.server.start();
    }
}
