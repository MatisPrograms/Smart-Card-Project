package fr.polytech.unice;

import java.util.ArrayList;
import java.util.List;

/**
 * Hello world!
 */
public class App {
    static final int MAX = 6;

    public static void main(String[] args) {
        List<VendorMachine> machines = new ArrayList<>();

        System.out.println("Initializing vending machines...");
        for (int i = 0; i < MAX; i++) {
            VendorMachine machine = new VendorMachine(i);
            machines.add(machine);
        }

        while (true) {
            System.out.println("Choose a vending machine to stop (0 to " + (MAX - 1) + "): ");
            int choice = Integer.parseInt(System.console().readLine());

            VendorMachine machine = machines.get(choice);
            machine.selected();

            System.out.println("Choose a product to buy ");
            String product = System.console().readLine();

            if (machine.buy(product)) {
                System.out.println("Product bought!");
            } else {
                System.out.println("Product not available!");
            }
        }
    }
}
