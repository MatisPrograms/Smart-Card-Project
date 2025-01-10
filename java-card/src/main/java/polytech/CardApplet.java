package polytech;

import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;
import javacard.security.RSAPublicKey;
import javacard.security.Signature;
import javacardx.crypto.Cipher;

public class CardApplet extends Applet {

    // CLA and INS constants
    public static final byte CLA_SECRET_APPLET = (byte) 0x88;
    public static final byte INS_GET_PIN_TRIES = (byte) 0x01;
    public static final byte INS_VALIDATE_PIN = (byte) 0x02;
    public static final byte INS_IS_PIN_VALIDATED = (byte) 0x03;
    public static final byte INS_CHANGE_PIN = (byte) 0x04;
    public static final byte INS_EXCHANGE_PUBLIC_KEYS = (byte) 0x05;
    public static final byte INS_SERVER_ADDRESS = (byte) 0x06;
    public static final byte INS_RECEIVE_MESSAGE = (byte) 0x07;
    public static final byte INS_SEND_MESSAGE = (byte) 0x08;
    public static final byte INS_ENCRYPT_MESSAGE = (byte) 0x09;
    public static final byte INS_DECRYPT_MESSAGE = (byte) 0x0A;

    // PIN constants
    public static final byte PIN_LENGTH = 4;
    public static final byte MAX_PIN_TRY = 3;
    public static final byte[] DEFAULT_PIN = {0x01, 0x02, 0x03, 0x04};

    // Cryptographic and Packet constants
    public static final short KEY_SIZE = 512;
    public static final short MAX_PACKET_SIZE = 127;
    public static final byte START_MESSAGE_FLAG = (byte) 0x00;
    public static final byte CONTINUE_MESSAGE_FLAG = (byte) 0x7f;
    public static final byte END_MESSAGE_FLAG = (byte) 0xff;
    // SW Error codes
    private static final byte SW_PIN_FAILED = (byte) 0x99;
    // Cryptographic objects
    private final OwnerPIN pin;
    private final KeyPair keyPair;
    private RSAPublicKey serversPublicKey;
    // Server address
    private byte[] serversAddress = null;
    // Message buffer
    private byte[] messageBuffer;
    private short messageBufferOffset = 0;
    private short messageBufferIndex = 0;

    /**
     * Creates a new instance of the applet.
     * The installation parameters are read from the <code>bArray</code> buffer.
     *
     * @param bArray  The array containing installation parameters
     * @param bOffset The starting offset in bArray
     * @param bLength The length of the installation parameters in bArray
     * @throws ISOException if the installation failed
     */
    private CardApplet(byte[] bArray, short bOffset, byte bLength) {
        // Create the PIN object
        this.pin = new OwnerPIN(MAX_PIN_TRY, PIN_LENGTH);

        // Set the default PIN
        this.pin.update(DEFAULT_PIN, (short) 0, PIN_LENGTH);

        // Generate the key pair
        this.keyPair = new KeyPair(KeyPair.ALG_RSA, KEY_SIZE);
        this.keyPair.genKeyPair();
    }

    /**
     * Installs the applet.
     *
     * @param bArray  The array containing installation parameters
     * @param bOffset The starting offset in bArray
     * @param bLength The length of the installation parameters in bArray
     * @throws ISOException if the installation failed
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
        new CardApplet(bArray, bOffset, bLength).register();
    }

    /**
     * Processes an incoming APDU.
     *
     * @param apdu the incoming <code>APDU</code> object
     * @throws ISOException if an ISO 7816-4 exception occurs
     */
    public void process(APDU apdu) throws ISOException {
        // ignore if the applet is being selected
        if (selectingApplet()) ISOException.throwIt(ISO7816.SW_NO_ERROR);

        // Get the APDU buffer
        byte[] buffer = apdu.getBuffer();

        // return if this is a SELECT FILE command
        if ((buffer[ISO7816.OFFSET_CLA] == 0) && (buffer[ISO7816.OFFSET_INS] == (byte) 0xA4)) return;

        // throw exception if the CLA byte is not ours
        if (buffer[ISO7816.OFFSET_CLA] != CLA_SECRET_APPLET) ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        switch (buffer[ISO7816.OFFSET_INS]) {
            case INS_GET_PIN_TRIES:
                getPINTries(apdu);
                break;
            case INS_VALIDATE_PIN:
                validatePIN(apdu);
                break;
            case INS_IS_PIN_VALIDATED:
                isPINValidated(apdu);
                break;
            case INS_CHANGE_PIN:
                changePIN(apdu);
                break;
            case INS_EXCHANGE_PUBLIC_KEYS:
                exchangePublicKeys(apdu);
                break;
            case INS_SERVER_ADDRESS:
                serverAddress(apdu);
                break;
            case INS_RECEIVE_MESSAGE:
                receiveMessage(apdu);
                break;
            case INS_SEND_MESSAGE:
                sendMessage(apdu);
                break;
            case INS_ENCRYPT_MESSAGE:
                signMessage(apdu);
                break;
            case INS_DECRYPT_MESSAGE:
                decryptMessage(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void exchangePublicKeys(APDU apdu) {
        this.validation();
        byte[] buffer = apdu.getBuffer();

        apdu.setIncomingAndReceive();
        short lc = 2;

        // Store the server's public key
        short expLenServer = Util.getShort(buffer, ISO7816.OFFSET_CDATA);
        short modLenServer = Util.getShort(buffer, (short) (ISO7816.OFFSET_CDATA + lc + expLenServer));

        this.serversPublicKey = (RSAPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, KEY_SIZE, false);
        this.serversPublicKey.setExponent(buffer, (short) (ISO7816.OFFSET_CDATA + lc), expLenServer);
        this.serversPublicKey.setModulus(buffer, (short) (ISO7816.OFFSET_CDATA + 2 * lc + expLenServer), modLenServer);

        // Send the card's public key
        RSAPublicKey key = (RSAPublicKey) keyPair.getPublic();

        short expLenCard = key.getExponent(buffer, (short) (ISO7816.OFFSET_CDATA + lc));
        Util.setShort(buffer, ISO7816.OFFSET_CDATA, expLenCard);

        short modLenCard = key.getModulus(buffer, (short) (ISO7816.OFFSET_CDATA + 2 * lc + expLenCard));
        Util.setShort(buffer, (short) (ISO7816.OFFSET_CDATA + lc + expLenCard), modLenCard);

        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) (2 * lc + expLenCard + modLenCard));
    }

    private void changePIN(APDU apdu) {
        this.validation();
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // Verify the given PIN
        if (buffer[ISO7816.OFFSET_LC] != PIN_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        // Check the PIN
        pin.update(buffer, ISO7816.OFFSET_CDATA, PIN_LENGTH);
    }

    private void getPINTries(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        buffer[0] = pin.getTriesRemaining();
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }

    private void isPINValidated(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        buffer[0] = pin.isValidated() ? (byte) 0x01 : (byte) 0x00;
        apdu.setOutgoingAndSend((short) 0, (short) 1);
    }

    private void validatePIN(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // Check the length
        if (buffer[ISO7816.OFFSET_LC] != PIN_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        // Check the PIN
        if (!pin.check(buffer, ISO7816.OFFSET_CDATA, PIN_LENGTH))
            ISOException.throwIt((short) ((SW_PIN_FAILED << 8) | (pin.getTriesRemaining() & 0xFF)));
    }

    private void serverAddress(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        // Store the server address
        if (this.pin.isValidated()) {
            apdu.setIncomingAndReceive();

            // Store the server's address
            this.serversAddress = new byte[buffer[ISO7816.OFFSET_LC]];
            Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, this.serversAddress, (short) 0, buffer[ISO7816.OFFSET_LC]);
        } else {
            // Send the server's address
            Util.arrayCopy(this.serversAddress, (short) 0, buffer, (short) 0, (short) this.serversAddress.length);
            apdu.setOutgoingAndSend((short) 0, (short) this.serversAddress.length);
        }
    }

    private void receiveMessage(APDU apdu) {
        this.validation();
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // Receive the message
        byte messageFlag = buffer[ISO7816.OFFSET_CDATA];
        short messageOffset = (short) (ISO7816.OFFSET_CDATA + 1);
        short messageLength = (short) (buffer[ISO7816.OFFSET_LC] - 1);

        // Check the message flag
        switch (messageFlag) {
            case START_MESSAGE_FLAG:
                this.messageBuffer = new byte[1024];
                this.messageBufferOffset = 0;
                Util.arrayCopy(buffer, messageOffset, this.messageBuffer, (short) 0, messageLength);
                break;
            case CONTINUE_MESSAGE_FLAG:
                Util.arrayCopy(buffer, messageOffset, this.messageBuffer, this.messageBufferOffset, messageLength);
                break;
            case END_MESSAGE_FLAG:
                Util.arrayCopy(buffer, messageOffset, this.messageBuffer, this.messageBufferOffset, messageLength);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
        this.messageBufferOffset += messageLength;
    }

    private void sendMessage(APDU apdu) {
        this.validation();
        byte[] buffer = apdu.getBuffer();

        if (this.messageBufferOffset == 0) ISOException.throwIt(ISO7816.SW_WRONG_DATA);

        // Send the message
        short messageLength = (short) (this.messageBufferOffset - this.messageBufferIndex);
        short responseLength = (short) (messageLength > MAX_PACKET_SIZE ? MAX_PACKET_SIZE : messageLength + 1);

        if (this.messageBufferIndex == 0) {
            buffer[0] = START_MESSAGE_FLAG;
        } else if ((short) (this.messageBufferIndex + responseLength) < this.messageBufferOffset) {
            buffer[0] = CONTINUE_MESSAGE_FLAG;
        } else {
            buffer[0] = END_MESSAGE_FLAG;
        }
        Util.arrayCopy(this.messageBuffer, this.messageBufferIndex, buffer, (short) 1, (short) (responseLength - 1));

        if (buffer[0] != END_MESSAGE_FLAG) this.messageBufferIndex += (short) (responseLength - 1);
        else this.messageBufferIndex = 0;
        apdu.setOutgoingAndSend((short) 0, responseLength);
    }

    private void signMessage(APDU apdu) {
        this.validation();
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // Sign the message
    }

    private void decryptMessage(APDU apdu) {
        this.validation();
        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // Decrypt the message
    }

    public short encryptMessage(byte[] message, byte[] dest, short length) {
        Cipher cipher = Cipher.getInstance(Cipher.ALG_RSA_PKCS1, false);

        cipher.init(this.serversPublicKey, Cipher.MODE_ENCRYPT);
        return cipher.doFinal(message, ISO7816.OFFSET_CDATA, length, dest, (short) 0);
    }

    public short signMessage(byte[] message, byte[] dest, short lengthMessage) {

        Util.setShort(dest, (short) (0), lengthMessage);
        Util.arrayCopy(message, (byte) 0, dest, (short) (2), lengthMessage);

        Signature signature = Signature.getInstance(Signature.ALG_RSA_SHA_ISO9796, false);
        signature.init(this.keyPair.getPrivate(), Signature.MODE_SIGN);

        byte[] signedMessage = new byte[signature.getLength()];
        signature.sign(message, (short) 0, lengthMessage, signedMessage, (short) 0);

        short lengthSignature = signature.getLength();
        Util.setShort(dest, (short) (2 + lengthMessage), lengthSignature);
        Util.arrayCopy(signedMessage, (byte) 0, dest, (short) (4 + lengthMessage), lengthSignature);

        return (short) (4 + lengthMessage + lengthSignature);
    }

    private void validation() {
        if (!pin.isValidated()) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    }
}
