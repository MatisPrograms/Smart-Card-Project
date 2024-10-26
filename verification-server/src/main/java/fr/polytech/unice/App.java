package fr.polytech.unice;

import java.io.IOException;

/**
 * Run the verification server.
 */
public class App
{
    public static void main( String[] args ) throws IOException {
        new VerificationServer().start();
    }
}
