package polytech;

import javacard.framework.*;
import javacard.security.KeyPair;

public class CardApplet extends Applet {

    // CLA and INS constants
    public static final byte CLA_SECRET_APPLET = (byte) 0x88;
    public static final byte INS_GET_SECRET = (byte) 0x10;
    public static final byte INS_CHANGE_PIN = (byte) 0x20;
    public static final byte INS_GET_PIN_TRIES = (byte) 0x30;
    public static final byte INS_IS_PIN_VALIDATED = (byte) 0x40;
    public static final byte INS_VALIDATE_PIN = (byte) 0x50;

    // PIN constants
    public static final byte PIN_LENGTH = 4;
    public static final byte MAX_PIN_TRY = 3;
    public static final byte[] DEFAULT_PIN = {0x01, 0x02, 0x03, 0x04};

    // SW Error codes
    private static final byte SW_PIN_FAILED = (byte) 0x99;

    // Cryptographic constants
    private static final short KEY_SIZE = 512;
    private static final byte[] SECRET = {'S', '3', 'C', 'R', '3', 'T'};

    private final OwnerPIN pin;
    private final KeyPair keyPair;


    /**
     * Creates a new instance of the applet.
     * The installation parameters are read from the <code>bArray</code> buffer.
     *
     * @param bArray  The array containing installation parameters
     * @param bOffset The starting offset in bArray
     * @param bLength The length of the installation parameters in bArray
     * @throws ISOException if the installation failed
     */
    @SuppressWarnings("unused")
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
            case INS_GET_SECRET:
                sendSecret(apdu);
                break;
            case INS_CHANGE_PIN:
                changePIN(apdu);
                break;
            case INS_GET_PIN_TRIES:
                getPINTries(apdu);
                break;
            case INS_IS_PIN_VALIDATED:
                isPINValidated(apdu);
                break;
            case INS_VALIDATE_PIN:
                validatePIN(apdu);
                break;
            default:
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void sendSecret(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short length = (short) SECRET.length;

        // Copy the secret data into the buffer
        Util.arrayCopyNonAtomic(SECRET, (short) 0, buffer, (short) 0, length);

        // Send the response to the terminal
        apdu.setOutgoingAndSend((short) 0, length);
    }

    private void changePIN(APDU apdu) {
//        this.validation();
        byte[] buffer = apdu.getBuffer();

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

        // Check the length
        if (buffer[ISO7816.OFFSET_LC] != PIN_LENGTH) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        byte[] checkingPin = new byte[PIN_LENGTH];
        Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, checkingPin, (short) 0, PIN_LENGTH);

        // Check the PIN
        if (!pin.check(checkingPin, (short) 0, PIN_LENGTH))
            ISOException.throwIt((short) ((SW_PIN_FAILED << 8) | (pin.getTriesRemaining() & 0xFF)));
    }

    @SuppressWarnings("unused")
    private void validation() {
        if (!pin.isValidated()) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    }
}
