package fr.polytech.unice;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISOException;

public class CardApplet extends Applet {

    public static void install(byte[] bArray, short bOffset, byte bLength) throws ISOException {
        new CardApplet().register();
    }

    @Override
    public void process(APDU apdu) throws ISOException {
        // Insert your code here
    }
}
