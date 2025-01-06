package fr.polytech.unice;

import javax.smartcardio.*;
import java.util.List;
import java.util.Scanner;

public class VerificationServer {

    public static final int SUCCESS = 0x9000;
    private static final byte[] APPLET_AID = new byte[]{(byte) 0xa0, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x10, 0x01};

    public static final byte CLA = (byte) 0x42;
    static final byte NEW_PIN = (byte) 0x10;
    static final byte VERIFY = (byte) 0x20;
    static final byte CREDIT = (byte) 0x30;
    static final byte DEBIT = (byte) 0x40;
    static final byte GET_BALANCE = (byte) 0x50;
    static final byte UNBLOCK = (byte) 0x60;

    public VerificationServer(int pin) throws Exception {
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

        // Initialize the PIN
//        initializePIN(channel, pin);
        getBalance(channel);
    }

    private static void selectApplet(CardChannel channel) throws Exception {
        CommandAPDU selectCommand = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID);
        ResponseAPDU response = channel.transmit(selectCommand);

        if (response.getSW() == SUCCESS) {
            System.out.println("Applet selected successfully.");
        } else {
            throw new Exception("Failed to select applet. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static void initializePIN(CardChannel channel, int pin) throws Exception {
        byte[] pinBytes = new byte[]{(byte) (pin >> 24), (byte) (pin >> 16), (byte) (pin >> 8), (byte) pin};
        CommandAPDU resetCommand = new CommandAPDU(CLA, UNBLOCK, 0x00, 0x00, 0x00);
        channel.transmit(resetCommand);

        verifyPIN(channel, new byte[]{(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04});

        CommandAPDU initCommand = new CommandAPDU(CLA, NEW_PIN, 0x00, 0x00, pinBytes);
        ResponseAPDU response = channel.transmit(initCommand);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN initialized successfully.");
            verifyPIN(channel, pinBytes);
            getBalance(channel);
            signData(channel, new byte[]{0x01, 0x02, 0x03, 0x04});
        } else {
            throw new Exception("Failed to initialize PIN. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static void verifyPIN(CardChannel channel, byte[] pin) throws Exception {
        CommandAPDU verifyCommand = new CommandAPDU(CLA, VERIFY, 0x00, 0x00, pin);
        ResponseAPDU response = channel.transmit(verifyCommand);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN verified successfully.");
        } else {
            throw new Exception("Failed to verify PIN. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static void getBalance(CardChannel channel) throws Exception {
        CommandAPDU balanceCommand = new CommandAPDU(CLA, GET_BALANCE, 0x00, 0x00, 0x00);
        ResponseAPDU response = channel.transmit(balanceCommand);

        if (response.getSW() == SUCCESS) {
            byte[] data = response.getData();
            short balance = (short) ((data[0] << 8) | (data[1] & 0xFF));
            System.out.println("Current balance: " + balance);
        } else {
            throw new Exception("Failed to get balance. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static void signData(CardChannel channel, byte[] data) throws Exception {
        CommandAPDU signCommand = new CommandAPDU(CLA, UNBLOCK, 0x00, 0x00, data);
        ResponseAPDU response = channel.transmit(signCommand);

        if (response.getSW() == SUCCESS) {
            byte[] signedData = response.getData();
            System.out.println("Signed data: " + bytesToHex(signedData));
        } else {
            throw new Exception("Failed to sign data. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X", b));
        return sb.toString();
    }
}
