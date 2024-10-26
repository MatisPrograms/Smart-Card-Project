package fr.polytech.unice;

import javax.swing.*;
import java.awt.*;

public class VendingMachine extends JFrame {
    // GUI components
    private JPanel panel1;
    private JPasswordField passwordField1;
    private JCheckBox xCheckBox;
    private JButton checkButton;
    private JButton creditButton;
    private JButton debitButton;
    private JTextField a0€TextField;
    private JButton unblockCardButton;
    private JButton establishConnectionWithCardReaderButton;
    private JButton closeTheConnectionWithButton;
    private JTextField textField1;
    private JTextField textField2;
    private JTextField a0€TextField1;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTextField textField7;
    private JTextField textField8;
    private JTextField textField9;
    private JTextField textField10;
    private JTextField textField11;
    private JTextField textField12;
    private JButton enterButton;

    // codes of CLA byte in the command APDU header
    static byte counter;
    static final byte WALLET_CLA =(byte)0xB0;
    static final byte VERIFY = (byte) 0x20;
    static final byte CREDIT = (byte) 0x30;
    static final byte DEBIT = (byte) 0x40;
    static final byte GET_BALANCE = (byte) 0x50;
    static final byte UNBLOCK = (byte) 0x60;

    public VendingMachine() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {
        }

        setTitle("Vending Machine");

        setSize((int) (0.75 * Toolkit.getDefaultToolkit().getScreenSize().width), (int) (0.75 * Toolkit.getDefaultToolkit().getScreenSize().height));
        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setContentPane(panel1);
        setVisible(true);
    }
}
