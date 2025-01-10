package fr.polytech.unice;

import javax.smartcardio.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.List;
import java.util.Scanner;

import static polytech.CardApplet.*;

public class JavaCardTerminal {

    // Constants
    public static final int SUCCESS = 0x9000;
    public static final int MAX_SIZE = 0xff;
    public static final int ASCII_OFFSET = 48;
    private static final byte[] APPLET_AID = new byte[]{(byte) 0xa0, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x10, 0x01};

    private CardTerminal terminal;
    private CardChannel channel;

    @SuppressWarnings("unused")
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("0x%02X ", b));
        return sb.toString().trim();
    }

    @SuppressWarnings("unused")
    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    @SuppressWarnings("unused")
    public static int bytesToInt(byte[] bytes) {
        int value = 0;
        for (int index = 0; index < bytes.length; index++) value += bytes[index] << (8 * index);
        return value;
    }

    @SuppressWarnings("unused")
    public static boolean bytesToBoolean(byte[] bytes) {
        return bytes.length > 0 && bytes[0] == 0x01;
    }

    @SuppressWarnings("unused")
    public static byte[] stringToBytes(String str) {
        byte[] bytes = new byte[str.length()];
        for (int i = 0; i < str.length(); i++) bytes[i] = (byte) (str.charAt(i) - ASCII_OFFSET);
        return bytes;
    }

    public static byte[] concatenateArrays(byte[] first, byte[] second) {
        byte[] result = new byte[first.length + second.length];
        System.arraycopy(first, 0, result, 0, first.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    @SuppressWarnings("unused")
    public static byte[] buildPublicKeyArray(byte[] array1, byte[] array2, short lc) {
        byte[] result = new byte[array1.length + array2.length + 2 * lc];
        result[0] = (byte) (array1.length >> 8);
        result[1] = (byte) array1.length;
        System.arraycopy(array1, 0, result, lc, array1.length);

        result[array1.length + lc] = (byte) (array2.length >> 8);
        result[array1.length + lc + 1] = (byte) array2.length;
        System.arraycopy(array2, 0, result, 2 * lc + array1.length, array2.length);
        return result;
    }

    public CardTerminal getTerminal() throws CardException {
        // Connect to the card reader
        TerminalFactory factory = TerminalFactory.getDefault();
        List<CardTerminal> terminals = factory.terminals().list();
        int terminalIndex = 0;

        // Check if a card terminal is found
        if (terminals.isEmpty()) {
            throw new CardException("No card terminal found");
        } else {
            // Select the terminal if there are multiple
            if (terminals.size() != 1) {
                System.out.println("Select a terminal:");
                for (int i = 0; i < terminals.size(); i++) {
                    System.out.println(" " + (i + 1) + ": " + terminals.get(i).getName());
                }
                System.out.print("\nEnter the terminal number: (1-" + terminals.size() + ") ");
                terminalIndex = new Scanner(System.in).nextInt() - 1;
            }
        }

        // Get the selected terminal
        this.terminal = terminals.get(terminalIndex);
        System.out.println("Terminal: " + this.terminal.getName());
        return this.terminal;
    }

    private void connect() throws CardException {
        if (this.terminal == null) this.getTerminal();

        // Connect to the card
        this.terminal.waitForCardPresent(0);
        Card card = this.terminal.connect("*");
        this.channel = card.getBasicChannel();
    }

    public void refresh() throws CardException {
        this.channel = null;
        this.connect();
    }

    public void selectApplet() throws CardException {
        if (this.channel == null) this.connect();

        CommandAPDU command = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, APPLET_AID);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Applet selected successfully.");
        } else {
            System.err.println("Failed to select applet. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    public void getPINStatus() throws CardException {
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

    public boolean checkPIN(byte[] pin) throws CardException {
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

    public void changePIN(byte[] pin) throws CardException {
        System.out.println("Changing PIN..." + bytesToHex(pin));
        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_CHANGE_PIN, 0x00, 0x00, pin);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("PIN changed successfully.");
        } else {
            System.err.println("Failed to change PIN. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    public RSAPublicKey exchangePublicKeys(RSAPublicKey publicKey) throws Exception {
        if (this.channel == null) this.connect();

        short lc = 2;
        byte[] rsaPublicKey = buildPublicKeyArray(publicKey.getPublicExponent().toByteArray(), publicKey.getModulus().toByteArray(), lc);

        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_EXCHANGE_PUBLIC_KEYS, 0x00, 0x00, rsaPublicKey, MAX_SIZE);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Server's Public Key: " + rsaPublicKey.length + " | " + bytesToHex(rsaPublicKey));
            System.out.println("Card's Public Key: " + response.getData().length + " | " + bytesToHex(response.getData()));

            short expLen = (short) ((response.getData()[0] << 8) | (response.getData()[1] & 0xff));
            short modLen = (short) ((response.getData()[2 + expLen] << 8) | (response.getData()[3 + expLen] & 0xff));

            BigInteger exponent = new BigInteger(1, response.getData(), lc, expLen);
            BigInteger modulus = new BigInteger(1, response.getData(), 2 * lc + expLen, modLen);

            return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
        } else {
            System.err.println("Failed to exchange public keys. SW=" + Integer.toHexString(response.getSW()));
            return null;
        }
    }

    public void sendServerAddress(String address, int port) throws CardException {
        if (port > 0xffff) throw new IllegalArgumentException("Invalid port number: " + port);
        if (this.channel == null) this.connect();

        String[] parts = address.split("\\.");
        byte[] ip = new byte[parts.length];
        for (int i = 0; i < parts.length; i++) ip[i] = Byte.parseByte(parts[i]);

        byte[] serverInfo = new byte[ip.length + 2];
        System.arraycopy(ip, 0, serverInfo, 0, ip.length);

        serverInfo[ip.length] = (byte) (port >> 8);
        serverInfo[ip.length + 1] = (byte) port;

        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_SERVER_ADDRESS, 0x00, 0x00, serverInfo);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Server address sent successfully.");
        } else {
            System.err.println("Failed to send server address. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    public String getServerAddress() throws CardException {
        if (this.channel == null) this.connect();

        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_SERVER_ADDRESS, 0x00, 0x00, MAX_SIZE);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            byte[] data = response.getData();
            StringBuilder sb = new StringBuilder(":" + ((data[data.length - 2] & 0xff) << 8 | (data[data.length - 1] & 0xff)));
            for (int i = data.length - 3; i >= 0; i--) sb.insert(0, data[i]).insert(0, ".");
            return sb.deleteCharAt(0).insert(0, "http://").toString();
        } else {
            System.err.println("Failed to send server address. SW=" + Integer.toHexString(response.getSW()));
            return null;
        }
    }

    public void sendData(byte[] bytes) throws CardException {
        if (this.channel == null) this.connect();

        // Send the data in packets
        for (int i = 0; i < bytes.length; i += MAX_PACKET_SIZE - 1) {
            byte[] packet = new byte[Math.min(MAX_PACKET_SIZE, bytes.length - i)];

            // Set the first byte to 0x00 if there are more packets to send
            packet[0] = i == 0
                    ? START_MESSAGE_FLAG
                    : i + packet.length < bytes.length
                    ? CONTINUE_MESSAGE_FLAG
                    : END_MESSAGE_FLAG;
            System.arraycopy(bytes, i, packet, 1, packet.length - 1);

            CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_RECEIVE_MESSAGE, 0x00, 0x00, packet);
            ResponseAPDU response = this.channel.transmit(command);

            if (response.getSW() != SUCCESS) System.err.println("Failed to send data. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    public String receiveData() throws CardException {
        if (this.channel == null) this.connect();

        byte[] buffer = new byte[1024];
        boolean end = false;
        int offset = 0;

        while (!end && offset < buffer.length) {
            CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_SEND_MESSAGE, 0x00, 0x00, MAX_SIZE);
            ResponseAPDU response = this.channel.transmit(command);

            if (response.getSW() == SUCCESS) {
                byte[] packet = response.getData();
                int dataPacketLength = packet.length - 1;
                if (packet[0] == END_MESSAGE_FLAG) end = true;

                System.arraycopy(packet, 1, buffer, offset, dataPacketLength);
                offset += dataPacketLength;
            } else {
                System.err.println("Failed to receive data. SW=" + Integer.toHexString(response.getSW()));
            }
        }

        byte[] data = new byte[offset];
        System.arraycopy(buffer, 0, data, 0, offset);
        return bytesToString(data);
    }

    public void encryptData() throws CardException {
        if (this.channel == null) this.connect();

        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_ENCRYPT_MESSAGE, 0x00, 0x00);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Data encrypted successfully.");
        } else {
            System.err.println("Failed to encrypt data. SW=" + Integer.toHexString(response.getSW()));
        }
    }

    public void decryptData() throws CardException {
        if (this.channel == null) this.connect();

        CommandAPDU command = new CommandAPDU(CLA_SECRET_APPLET, INS_DECRYPT_MESSAGE, 0x00, 0x00);
        ResponseAPDU response = this.channel.transmit(command);

        if (response.getSW() == SUCCESS) {
            System.out.println("Data decrypted successfully.");
        } else {
            System.err.println("Failed to decrypt data. SW=" + Integer.toHexString(response.getSW()));
        }
    }
}
