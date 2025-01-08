package fr.polytech.unice;

import javax.smartcardio.*;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static polytech.CardApplet.*;

public class VerificationServer {

    public static final int SUCCESS = 0x9000;
    public static final int ASCII_OFFSET = 48;
    private static final byte[] APPLET_AID = new byte[]{(byte) 0xa0, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x10, 0x01};

    public VerificationServer(String pin) throws Exception {
        if (Integer.parseInt(pin) < 0 || Integer.parseInt(pin) > 9999)
            throw new IllegalArgumentException("Invalid PIN. Must be a 4-digit number.");
        else System.out.println("PIN: " + pin);

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
        CardChannel channel = card.getBasicChannel();

        // Select the applet
        selectApplet(channel);
        getPINTries(channel);
//        tryPIN(channel, stringToBytes(pin));
//        getPINTries(channel);
        tryPIN(channel, DEFAULT_PIN);
        getPINTries(channel);
        isPINValidated(channel);
    }

    private static void selectApplet(CardChannel channel) throws Exception {
        CommandAPDU selectCommand = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID);
        ResponseAPDU response = channel.transmit(selectCommand);

        if (response.getSW() == SUCCESS) {
            System.out.println("Applet selected successfully.");
        } else {
            System.err.println("Failed to select applet. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static void getPINTries(CardChannel channel) throws Exception {
        CommandAPDU selectCommand = new CommandAPDU(CLA_SECRET_APPLET, INS_GET_PIN_TRIES, 0x00, 0x00, 6);
        ResponseAPDU response = channel.transmit(selectCommand);

        if (response.getSW() == SUCCESS) {
            System.out.println("Data received: " + bytesToInt(response.getData()));
        } else {
            System.err.println("Failed to get PIN tries. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static void isPINValidated(CardChannel channel) throws Exception {
        CommandAPDU selectCommand = new CommandAPDU(CLA_SECRET_APPLET, INS_IS_PIN_VALIDATED, 0x00, 0x00, 6);
        ResponseAPDU response = channel.transmit(selectCommand);

        if (response.getSW() == SUCCESS) {
            System.out.println("Data received: " + bytesToBoolean(response.getData()));
        } else {
            System.err.println("Failed to validate PIN. SW=" + Integer.toHexString(response.getSW()));
        }
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

    private void tryPIN(CardChannel channel, byte[] bytes) throws Exception {
        System.out.println("Trying PIN: " + Arrays.toString(bytes));
        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_VALIDATE_PIN, 0x00, 0x00, bytes, 0, bytes.length);
        ResponseAPDU response = channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN validated successfully.");
        } else {
            System.err.println("Failed to validate PIN. SW=" + Integer.toHexString(response.getSW()));
        }
    }
}
