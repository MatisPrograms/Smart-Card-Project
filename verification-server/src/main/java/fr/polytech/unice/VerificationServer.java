package fr.polytech.unice;

import com.sun.net.httpserver.HttpServer;

import javax.smartcardio.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import fr.polytech.unice.api.Router;

import static fr.polytech.unice.JavaCardTerminal.bytesToHex;
import static fr.polytech.unice.JavaCardTerminal.stringToBytes;
import static polytech.CardApplet.*;

public class VerificationServer {

    // Cryptographic constants
    private final KeyPair keyPair;
    private RSAPublicKeySpec cardsPublicKey;

    // Http server constants
    private final int PORT = 8080;

    public VerificationServer() {
        this.keyPair = null;
        this.cardsPublicKey = null;
    }

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

        // Send HTTP IP address and port to the card
        jcTerminal.sendServerAddress("127.0.0.1", PORT);

        // Disconnect the card and wait for card removal
        card.disconnect(false);
    }

    public void runHttpServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // General handler for all routes
        Router router = new Router(this);
        server.createContext("/", router);

        // Set up the thread pool
        Executor executor = new ThreadPoolExecutor(10, 100, 30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        server.setExecutor(executor);

        // Start server
        System.out.println("Server running on http://localhost:" + PORT);
        server.start();
    }
}
