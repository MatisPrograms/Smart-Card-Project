package fr.polytech.unice;

import java.util.Arrays;

/**
 * Run the verification server.
 */
public class App {
    public static void main(String[] args) throws Exception {
        int pinIndex = Arrays.asList(args).indexOf("--pin") + 1;
        String pin = pinIndex >= args.length ? "1234" : args[pinIndex];
        new VerificationServer(pin);
    }
}
