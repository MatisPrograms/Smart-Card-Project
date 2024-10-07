package fr.polytech.unice;

import com.licel.jcardsim.io.JavaxSmartCardInterface;
import com.licel.jcardsim.samples.HelloWorldApplet;
import javacard.framework.AID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardAppletTest {
    AID appletAID;

    @BeforeEach
    void setUp() {
        String appletAIDStr = "A000000018434D";
        appletAID = new AID(appletAIDStr.getBytes(), (short) 0, (byte) appletAIDStr.length());
        System.out.println("Applet AID: " + appletAID);
    }

    @Test
    void install() {
        //1. create simulator
        JavaxSmartCardInterface simulator = new JavaxSmartCardInterface();
        //2. install applet
        simulator.installApplet(appletAID, HelloWorldApplet.class);
        //3. select applet
        simulator.selectApplet(appletAID);
        //4. send apdu
        ResponseAPDU response = simulator.transmitCommand(new CommandAPDU(0x01, 0x01, 0x00, 0x00));
        //5. check response
        assertEquals(0x9000, response.getSW());
    }

    @Test
    void process() {

    }
}