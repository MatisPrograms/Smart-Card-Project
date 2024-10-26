package fr.polytech.unice;

import javacard.framework.*;

public class CardApplet extends Applet {

    // codes of CLA byte in the command APDU header
    static final byte WALLET_CLA = (byte) 0xB0;

    // codes of INS byte in the command APDU header
    static final byte VERIFY = (byte) 0x20;
    static final byte CREDIT = (byte) 0x30;
    static final byte DEBIT = (byte) 0x40;
    static final byte GET_BALANCE = (byte) 0x50;
    static final byte UNBLOCK = (byte) 0x60;

    // maximum balance | 0x7FFF = 32767
    static final short MAX_BALANCE = 0x7FFF;

    // maximum transaction amount | 0xFF = 255
    static final short MAX_TRANSACTION_AMOUNT = 0xFF;

    // maximum number of incorrect tries before the PIN is blocked | 0x03 = 3
    static final byte PIN_TRY_LIMIT = (byte) 0x03;

    // maximum size PIN | 0x06 = 6
    static final byte MAX_PIN_SIZE = (byte) 0x06;

    // signal that the PIN verification failed
    static final short SW_VERIFICATION_FAILED = 0x6312;

    // signal the PIN validation is required for a credit or a debit transaction
    static final short SW_PIN_VERIFICATION_REQUIRED = 0x6311;

    // signal invalid transaction amount | amount > MAX_TRANSACTION_AMOUNT or amount < 0
    static final short SW_INVALID_TRANSACTION_AMOUNT = 0x6A83;

    // signal that the balance exceed the maximum
    static final short SW_EXCEED_MAXIMUM_BALANCE = 0x6A84;

    // signal the balance becomes negative
    static final short SW_NEGATIVE_BALANCE = 0x6A85;

    // instance of the PIN
    OwnerPIN pin;

    // balance of the wallet
    short balance;

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
        this.pin = new OwnerPIN(PIN_TRY_LIMIT, MAX_PIN_SIZE);

        // The PIN code is 1234
        byte[] pinCode = {(byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04};
        this.pin.update(pinCode, (short) 0, (byte) pinCode.length);
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
     * Selects the applet.
     *
     * @return <code>true</code> if the applet is selected, <code>false</code> otherwise
     */
    @Override
    public boolean select() {
        return this.pin.getTriesRemaining() != 0;
    }

    /**
     * Deselects the applet and resets the PIN.
     */
    @Override
    public void deselect() {
        this.pin.reset();
    }

    /**
     * Validates the PIN code.
     */
    private void pinValidated() {
        if (!this.pin.isValidated()) ISOException.throwIt(SW_PIN_VERIFICATION_REQUIRED);
    }

    /**
     * Retrieves the amount from the APDU buffer.
     *
     * @param apdu the incoming <code>APDU</code> object
     * @return the amount
     */
    private short getAmount(APDU apdu) {
        this.pinValidated();

        // APDU buffer
        byte[] buffer = apdu.getBuffer();

        // retrieve the credit amount
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (byteRead != 1) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        short amount = Util.makeShort((byte) 0, buffer[ISO7816.OFFSET_CDATA]);

        // check the amount
        if (amount > MAX_TRANSACTION_AMOUNT || amount < 0) ISOException.throwIt(SW_INVALID_TRANSACTION_AMOUNT);
        return amount;
    }

    /**
     * Processes an incoming APDU.
     *
     * @param apdu the incoming <code>APDU</code> object
     * @throws ISOException if an ISO 7816-4 exception occurs
     */
    @Override
    public void process(APDU apdu) throws ISOException {
        // APDU buffer
        byte[] buffer = apdu.getBuffer();

        // check SELECT APDU command
        if (buffer[ISO7816.OFFSET_CLA] == 0 && buffer[ISO7816.OFFSET_INS] == (byte) 0xA4) return;

        // verify the CLA byte
        if (buffer[ISO7816.OFFSET_CLA] != WALLET_CLA) ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);

        // check the INS byte to determine the operation
        switch (buffer[ISO7816.OFFSET_INS]) {
            case GET_BALANCE -> this.getBalance(apdu);
            case VERIFY -> this.verify(apdu);
            case CREDIT -> this.credit(apdu);
            case DEBIT -> this.debit(apdu);
            case UNBLOCK -> this.pin.resetAndUnblock();
            default -> ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    /**
     * Retrieves the balance of the wallet.
     *
     * @param apdu the incoming <code>APDU</code> object
     */
    private void getBalance(APDU apdu) {
        this.pinValidated();

        // APDU buffer
        byte[] buffer = apdu.getBuffer();

        // set the outgoing data length
        short le = apdu.setOutgoing();
        if (le < 2) ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);

        // send the balance
        apdu.setOutgoingLength((byte) 2);
        Util.setShort(buffer, (short) 0, this.balance);
        apdu.sendBytes((short) 0, (short) 2);
    }

    /**
     * Verifies the PIN code.
     *
     * @param apdu the incoming <code>APDU</code> object
     */
    private void verify(APDU apdu) {
        // APDU buffer
        byte[] buffer = apdu.getBuffer();

        // retrieve the PIN data
        byte byteRead = (byte) (apdu.setIncomingAndReceive());
        if (!this.pin.check(buffer, ISO7816.OFFSET_CDATA, byteRead)) ISOException.throwIt(SW_VERIFICATION_FAILED);
    }

    /**
     * Credits the wallet.
     *
     * @param apdu the incoming <code>APDU</code> object
     */
    private void credit(APDU apdu) {
        short amount = getAmount(apdu);

        // check the balance
        if (this.balance + amount > MAX_BALANCE) ISOException.throwIt(SW_EXCEED_MAXIMUM_BALANCE);

        // credit the wallet
        this.balance += amount;
    }

    /**
     * Debits the wallet.
     *
     * @param apdu the incoming <code>APDU</code> object
     */
    private void debit(APDU apdu) {
        short amount = getAmount(apdu);

        // check the balance
        if (this.balance - amount < 0) ISOException.throwIt(SW_NEGATIVE_BALANCE);

        // debit the wallet
        this.balance -= amount;
    }
}
