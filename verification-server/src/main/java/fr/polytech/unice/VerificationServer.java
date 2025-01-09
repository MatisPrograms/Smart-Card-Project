package fr.polytech.unice;

import javax.smartcardio.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;

import static fr.polytech.unice.JavaCardTerminal.bytesToHex;
import static fr.polytech.unice.JavaCardTerminal.stringToBytes;
import static polytech.CardApplet.*;

public class VerificationServer {

    // Cryptographic constants
    private final KeyPair keyPair;
    private RSAPublicKeySpec cardsPublicKey;

    // Http server constants
    private final int PORT = 8080;

    // At installation time
    public VerificationServer(String pin) throws Exception {
        // Check the format of the PIN
        if (Integer.parseInt(pin) < 0 || Integer.parseInt(pin) > Integer.parseInt("9".repeat(PIN_LENGTH)))
            throw new IllegalArgumentException("Invalid PIN. Must be a " + PIN_LENGTH + "-digit number.");
        else System.out.println("PIN selected: " + pin);

        // Generate the RSA key pair for the server
        KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
        rsaGenerator.initialize(KEY_SIZE);
        this.keyPair = rsaGenerator.generateKeyPair();

        // Get the card terminal
        JavaCardTerminal jcTerminal = new JavaCardTerminal();
        CardTerminal terminal = jcTerminal.getTerminal();

        // Wait for card insertion
        terminal.waitForCardPresent(0);
        Card card = terminal.connect("*");
        System.out.println("Card inserted.\n");

        // Select the applet
        jcTerminal.selectApplet();

        // Set the PIN to the card
        byte[] newPIN = stringToBytes(pin);
        if (!jcTerminal.tryPIN(newPIN)) {
            System.out.println("New PIN is not set, defaulting to " + bytesToHex(DEFAULT_PIN));
            if (!jcTerminal.tryPIN(DEFAULT_PIN)) throw new Exception("Please reset the card.");
            jcTerminal.changePIN(newPIN);
            jcTerminal.tryPIN(newPIN);
            jcTerminal.getPINStatus();
        }

        // Exchange public keys
        this.cardsPublicKey = jcTerminal.exchangePublicKeys((RSAPublicKey) this.keyPair.getPublic());

        // Disconnect the card and wait for card removal
        card.disconnect(false);

        // Run the verification server
        this.runHttpServer();
    }

    private void runHttpServer() {
        System.out.println("HTTP Server running on port 8080...");
    }
}
