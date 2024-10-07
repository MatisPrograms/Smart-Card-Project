package fr.polytech.unice;

import java.util.Map;

public class VendorMachine {

    private static final String currency = "€";
    private static final Map<String, Float> products = Map.of(
            "Coca", 2.5f,
            "Pepsi", 2.0f,
            "Fanta", 2.0f,
            "Sprite", 2.0f,
            "Water", 1.0f
    );
    private final int uuid;

    public VendorMachine(int uuid) {
        this.uuid = uuid;
    }

    public void selected() {
        System.out.println("ID: " + this.uuid + " | has been selected!");
        printProducts();
    }

    private void printProducts() {
        System.out.println("Products available: ");
        System.out.printf("| %-10s | %-10s |\n", "Product", "Price");
        for (Map.Entry<String, Float> product : products.entrySet()) {
            System.out.printf("| %-10s | %-10s |\n", product.getKey(), product.getValue() + currency);
        }
    }

    public boolean buy(String product) {
        if (products.containsKey(product)) {
            System.out.println("Buying " + product + "...");
            return true;
        }
        return false;
    }
}
