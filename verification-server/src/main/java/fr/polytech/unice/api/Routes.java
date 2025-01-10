package fr.polytech.unice.api;

import com.google.gson.JsonObject;
import nl.flotsam.xeger.Xeger;

public class Routes {

    // Products placement
    private final String[] products = new String[]{
        "/coconut.png",
        "/dondew.png",
        "/lemon.png",
        "/nrg.png",
        "/starbucks.png",
        "/peach.png"
    };

    private final Xeger productLocationGenerator = new Xeger("[A-C][1-4]");
    private final Xeger productPriceGenerator = new Xeger("[1-5]\\.[0-9]{2}€");

    public Routes() {
        Router.GET("/products", (exchange, wallet) -> {
            JsonObject responseJson = new JsonObject();

            // Get Query Parameters
            String query = exchange.getRequestURI().getQuery();

            // Check if the query parameter decrypt is present
            boolean decrypted = query != null && query.contains("decrypted");
            if (!decrypted) wallet = wallet.encrypt();

            for (String productImage : this.products) {
                JsonObject productJson = new JsonObject();

                String productLocation = generateRandomLocation(responseJson);

                productJson.addProperty("image", productImage);
                productJson.addProperty("price", this.productPriceGenerator.generate());

                responseJson.add(productLocation, productJson);
            }

            Router.respond(exchange, wallet, responseJson);
        });
    }

    private String generateRandomLocation(JsonObject jsonObject) {
        String location = this.productLocationGenerator.generate();
        while (jsonObject.has(location)) location = this.productLocationGenerator.generate();
        return location;
    }
}
