package fr.polytech.unice;

import java.util.Arrays;
import java.util.Scanner;

import static polytech.CardApplet.*;

/**
 * Run the verification server.
 */
public class App {
    public static void main(String[] args) throws Exception {
        int pinIndex = Arrays.asList(args).indexOf("--pin") + 1;
        String pin = pinIndex >= args.length ? askForPIN() : args[pinIndex];
        new VerificationServer(pin);
    }

    private static String askForPIN() {
        System.out.print("Enter a " + PIN_LENGTH + "-digit PIN for the card: ");
        return new Scanner(System.in).next("\\d{" + PIN_LENGTH + "}");
    }
}
