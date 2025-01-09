package fr.polytech.unice;

import javax.smartcardio.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.List;
import java.util.Scanner;

import static polytech.CardApplet.*;

public class VerificationServer {

    public static final int SUCCESS = 0x9000;
    public static final int MAX_SIZE = 0xff;
    public static final int ASCII_OFFSET = 48;
    private static final byte[] APPLET_AID = new byte[]{(byte) 0xa0, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x10, 0x01};

    private final CardChannel channel;
    private final KeyPair keyPair;

    public VerificationServer(String pin) throws Exception {
        if (Integer.parseInt(pin) < 0 || Integer.parseInt(pin) > 9999)
            throw new IllegalArgumentException("Invalid PIN. Must be a 4-digit number.");
        else System.out.println("PIN: " + pin);

        KeyPairGenerator rsaGenerator = KeyPairGenerator.getInstance("RSA");
        rsaGenerator.initialize(512);
        this.keyPair = rsaGenerator.generateKeyPair();

        // Connect to the card reader
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        int terminalIndex = 0;

        if (terminals.isEmpty()) {
            throw new CardException("No card terminal found");
        } else {
            if (terminals.size() == 1) {
                System.out.println("Only one terminal found, selecting it.");
            } else {
                System.out.println("Select a terminal:");
                for (int i = 0; i < terminals.size(); i++) {
                    System.out.println(" " + (i + 1) + ": " + terminals.get(i).getName());
                }
                System.out.println("\nEnter the terminal number: (1-" + terminals.size() + ") ");
                terminalIndex = new Scanner(System.in).nextInt() - 1;
            }
        }

        // Get the selected terminal
        CardTerminal terminal = terminals.get(terminalIndex);
        System.out.println("\nTerminal: " + terminal.getName());

        // Wait for card insertion
        terminal.waitForCardPresent(0);
        Card card = terminal.connect("*");
        System.out.println("Card inserted.");

        // Get the card channel
        this.channel = card.getBasicChannel();

        // Select the applet
        this.selectApplet();

        // Set the PIN to the card
        byte[] newPIN = stringToBytes(pin);
        if (!this.tryPIN(newPIN)) {
            System.out.println("PIN is not set. Setting PIN...");
            if (!this.tryPIN(DEFAULT_PIN)) throw new Exception("Java Card blocked.");
            this.changePIN(newPIN);
            this.tryPIN(newPIN);
            this.getPINStatus();
        }

        this.exchangePublicKeys(this.keyPair.getPublic());
    }

    @SuppressWarnings("unused")
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("0x%02X ", b));
        return sb.toString().trim();
    }

    @SuppressWarnings("unused")
    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append((char) b);
        return sb.toString();
    }

    @SuppressWarnings("unused")
    private static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (int index = 0; index < bytes.length; index++) value += bytes[index] << (8 * index);
        return value;
    }

    @SuppressWarnings("unused")
    private static boolean bytesToBoolean(byte[] bytes) {
        return bytes.length > 0 && bytes[0] == 0x01;
    }

    @SuppressWarnings("unused")
    private static byte[] stringToBytes(String str) {
        byte[] bytes = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) bytes[i] = (byte) (str.charAt(i) - ASCII_OFFSET);
        return bytes;
    }

    private void selectApplet() throws Exception {
        CommandAPDU command = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Applet selected successfully.");
        } else {
            System.err.println("Failed to select applet. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private void exchangePublicKeys(PublicKey key) throws Exception {
        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_EXCHANGE_PUBLICKEYS, 0x00, 0x00, key.getEncoded(), MAX_SIZE);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Server's Public Key: " + key.getEncoded().length + " | " + bytesToHex(key.getEncoded()));
            System.out.println("Card's Public Key: " + bytesToHex(response.getData()));
        } else {
            System.err.println("Failed to exchange public keys. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private void getPINStatus() throws Exception {
        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_GET_PIN_TRIES, 0x00, 0x00, MAX_SIZE);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN Tries left: " + bytesToInt(response.getData()));
        } else {
            System.err.println("Failed to get PIN tries. SW=" + Integer.toHexString(response.getSW()));
        }

        command = new CommandAPDU(CLA_SECRET_APPLET, INS_IS_PIN_VALIDATED, 0x00, 0x00, MAX_SIZE);
        response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN is Validated: " + bytesToBoolean(response.getData()));
        } else {
            System.err.println("Failed to validate PIN. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private boolean tryPIN(byte[] pin) throws Exception {
        System.out.println("Trying PIN..." + bytesToHex(pin));
        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_VALIDATE_PIN, 0x00, 0x00, pin);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN validated successfully.");
        } else {
            System.err.println("Failed to validate PIN. SW=" + Integer.toHexString(response.getSW()));
        }
        return response.getSW() == SUCCESS;
    }

    private void changePIN(byte[] pin) throws Exception {
        System.out.println("Changing PIN..." + bytesToHex(pin));
        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_CHANGE_PIN, 0x00, 0x00, pin);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN changed successfully.");
        } else {
            System.err.println("Failed to change PIN. SW=" + Integer.toHexString(response.getSW()));
        }
    }
}
