package fr.polytech.unice;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.Thread;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static javax.swing.SwingConstants.VERTICAL;

public class VendingMachine extends JFrame {
    public static final Color BACKGROUND = new Color(221, 218, 208);
    private final Font customFont = Font.createFont(Font.TRUETYPE_FONT, new File("vending-machine/src/main/resources/fonts/amongus vector.ttf"));

    // GUI components
    private final JPanel mainPanel = new JPanel();
    private final JPanel itemsPanel = new JPanel();
    private final JPanel commandPanel = new JPanel();
    private final JPanel buttonPanel = new JPanel();
    private final JLabel screen = new JLabel("Screen");
    private final JButton buttonA = new JButton("A");
    private final JButton buttonB = new JButton("B");
    private final JButton buttonC = new JButton("C");
    private final JButton button0 = new JButton("0");
    private final JButton button1 = new JButton("1");
    private final JButton button2 = new JButton("2");
    private final JButton button3 = new JButton("3");
    private final JButton button4 = new JButton("4");
    private final JButton button5 = new JButton("5");
    private final JButton button6 = new JButton("6");
    private final JButton button7 = new JButton("7");
    private final JButton button8 = new JButton("8");
    private final JButton button9 = new JButton("9");
    private final JButton buttonYes = new JButton("✔");
    private final JButton buttonNo = new JButton("❌");

    // Products placement
    private final Map<String, String[]> products = new HashMap<>() {{
        put("A1", new String[]{"/coconut.png", price()});
        put("A3", new String[]{"/dondew.png", price()});
        put("B3", new String[]{"/lemon.png", price()});
        put("B2", new String[]{"/nrg.png", price()});
        put("C2", new String[]{"/starbucks.png", price()});
        put("C4", new String[]{"/peach.png", price()});
    }};
    private final String[] empty = new String[]{"/empty.png", "0.00€"};
    // Variables
    private String selected = "";

    public VendingMachine() throws Exception {
        // Set the look and feel of the frame and the title
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(customFont);
        this.setTitle("Vending Machine");

        // Set the size and the location of the frame
        this.setSize((int) (0.75 * Toolkit.getDefaultToolkit().getScreenSize().width), (int) (0.75 * Toolkit.getDefaultToolkit().getScreenSize().height));
        this.setLocationRelativeTo(null);

        // Set the layout
        this.setContentPane(mainPanel);

        // Set the exit operation and make the frame visible
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);

        // Set shortcut Ctrl+Q to exit the application
        KeyStroke exitKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK);
        this.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(exitKeyStroke, "exit");
        this.getRootPane().getActionMap().put("exit", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        this.initComponents();
    }

    private static String price() {
        return String.format("1.%02d€", (int) (Math.random() * 100));
    }

    private static Border padding(int left, int top, int right, int bottom) {
        return new EmptyBorder(top, left, bottom, right);
    }

    private static Border padding(int horizontal, int vertical) {
        return padding(horizontal, vertical, horizontal, vertical);
    }

    private static Border padding(int padding) {
        return padding(padding, padding);
    }

    private void initComponents() {
        // Set the layout of the main panel 2/3 items and 1/3 commands
        this.mainPanel.setLayout(new BorderLayout());
        this.mainPanel.add(this.itemsPanel, BorderLayout.WEST);
        this.mainPanel.add(this.commandPanel, BorderLayout.CENTER);

        // Set outline border for the items panel
        this.itemsPanel.setPreferredSize(new Dimension(2 * this.getWidth() / 3, this.getHeight()));
        this.itemsPanel.setLayout(new GridLayout(3, 4));
        this.itemsPanel.setBorder(new CompoundBorder(padding(20, 30), new LineBorder(Color.BLACK, 5, true)));
        this.itemsPanel.setBackground(BACKGROUND);

        // Set the layout of the command panel
        this.commandPanel.setLayout(new BoxLayout(this.commandPanel, BoxLayout.Y_AXIS));
        this.commandPanel.setBorder(padding(20, 10));
        this.commandPanel.setBackground(BACKGROUND);

        // Create the separators
        JSeparator separator1 = new JSeparator(VERTICAL);
        JSeparator separator2 = new JSeparator(VERTICAL);
        separator1.setForeground(BACKGROUND);
        separator2.setForeground(BACKGROUND);
        separator1.setBackground(BACKGROUND);
        separator2.setBackground(BACKGROUND);

        // Add the components to the command panel
        this.commandPanel.add(separator1);
        this.commandPanel.add(this.screen);
        this.commandPanel.add(separator2);
        this.commandPanel.add(this.buttonPanel);

        // Set the properties of the screen
        this.screen.setBorder(new CompoundBorder(padding(10), new CompoundBorder(new LineBorder(Color.WHITE, 2, true), padding(10))));
        this.screen.setFont(this.customFont.deriveFont(Font.BOLD, 24));
        this.screen.setForeground(Color.WHITE);
        this.screen.setBackground(new Color(52, 52, 52));
        this.screen.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        this.screen.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.screen.setOpaque(true);

        // Set the layout of the button panel
        this.buttonPanel.setLayout(new GridLayout(0, 3, 5, 5));
        this.buttonPanel.setBorder(padding(20));
        this.buttonPanel.setBackground(new Color(104, 104, 104));
        this.buttonPanel.add(this.buttonA);
        this.buttonPanel.add(this.buttonB);
        this.buttonPanel.add(this.buttonC);
        this.buttonPanel.add(this.button1);
        this.buttonPanel.add(this.button2);
        this.buttonPanel.add(this.button3);
        this.buttonPanel.add(this.button4);
        this.buttonPanel.add(this.button5);
        this.buttonPanel.add(this.button6);
        this.buttonPanel.add(this.button7);
        this.buttonPanel.add(this.button8);
        this.buttonPanel.add(this.button9);
        this.buttonPanel.add(this.buttonNo);
        this.buttonPanel.add(this.button0);
        this.buttonPanel.add(this.buttonYes);

        // Set the properties of the buttons
        for (Component component : this.buttonPanel.getComponents()) {
            JButton button = (JButton) component;

            // Set style for the buttons
            button.setFont(button.getText().matches("[✔❌]")
                    ? new Font("Arial", Font.PLAIN, 20)
                    : this.customFont.deriveFont(Font.BOLD, 20)
            );
            button.setForeground(Color.WHITE);
            if (button.getText().equals("✔")) {
                button.setBackground(new Color(134, 203, 14));
            } else if (button.getText().equals("❌")) {
                button.setBackground(new Color(188, 52, 27));
            } else {
                button.setBackground(new Color(188, 188, 188));
            }
            button.setBorder(new BevelBorder(BevelBorder.RAISED));
            button.setFocusPainted(false);

            button.addActionListener(buttonLogic(button));
        }

        this.updateItems();

        new Thread(() -> {
            try {
                // Get the selected terminal
                JavaCardTerminal jcTerminal = new JavaCardTerminal();
                CardTerminal cardTerminal = jcTerminal.getTerminal();

                // Handle the card terminal
                while (true) {
                    this.handleCardTerminal(jcTerminal, cardTerminal);
                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private ActionListener buttonLogic(JButton button) {
        return _ -> {
            if (this.selected.length() == 3 && button.getText().contains("✔")) {
                this.screen.setText("Payment successful    Thank you for your purchase");
                this.selected = "";
            } else if ((this.selected.isEmpty() || this.selected.length() == 2) && button.getText().matches("[A-C]")) {
                this.selected = button.getText();
                this.screen.setText(this.selected);
            } else if (this.selected.length() == 1 && button.getText().matches("[1-4]")) {
                this.selected += button.getText();
                this.screen.setText(this.selected);
            } else if (this.selected.length() == 2 && button.getText().equals("✔")) {
                this.screen.setText("Item: " + this.selected + " selected for: " + this.products.getOrDefault(this.selected, this.empty)[1] + "    Revalidate to accept");
                this.selected += "✔";
            } else if (button.getText().equals("❌")) {
                this.selected = "";
                this.screen.setText("Screen");
            } else {
                this.selected = "";
                this.screen.setText("Invalid selection");
            }
        };
    }

    private void handleCardTerminal(JavaCardTerminal jcTerminal, CardTerminal terminal) {
        try {
            // Wait for card insertion
            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");
            System.out.println("Card inserted.");

            jcTerminal.selectApplet();
            jcTerminal.tryPIN(JavaCardTerminal.stringToBytes("6969"));
//
            URL url = jcTerminal.getServerAddress();
            System.out.println("The Server's address is: " + url);

            this.updateItems(this.products);

            // Wait for the card to be removed
            card.disconnect(false);
            terminal.waitForCardAbsent(0);
            System.out.println("Card removed.");
            this.updateItems();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void updateItems() {
        this.updateItems(new HashMap<>());
    }

    private void updateItems(Map<String, String[]> products) {
        this.itemsPanel.removeAll();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 4; x++) {
                this.addPanelItem(products, x, y);
            }
        }
        this.itemsPanel.revalidate();
        this.itemsPanel.repaint();
    }

    private void addPanelItem(Map<String, String[]> products, int x, int y) {
        String key = (char) ('A' + y) + "" + (x + 1);

        // Create the item panel
        JPanel itemPanel = new JPanel();

        // Set the layout of the item panel
        itemPanel.setLayout(new BorderLayout());
        itemPanel.setBackground(BACKGROUND);
        itemPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // Create the item image
        JLabel image = new JLabel();
        image.setIcon(new ImageIcon("vending-machine/src/main/resources/assets" + products.getOrDefault(key, empty)[0]));
        image.setHorizontalAlignment(SwingConstants.CENTER);
        itemPanel.add(image, BorderLayout.CENTER);

        // Create the item label
        JLabel item = new JLabel(key + (products.containsKey(key) ? " - " + products.getOrDefault(key, empty)[1] : ""));
        item.setFont(this.customFont.deriveFont(Font.BOLD, 24));
        item.setForeground(Color.WHITE);
        item.setBackground(new Color(178, 7, 8));
        item.setOpaque(true);
        item.setHorizontalAlignment(SwingConstants.CENTER);
        item.setBorder(new BevelBorder(BevelBorder.RAISED));
        itemPanel.add(item, BorderLayout.SOUTH);

        // Add the item to the items panel
        this.itemsPanel.add(itemPanel);
    }
}
