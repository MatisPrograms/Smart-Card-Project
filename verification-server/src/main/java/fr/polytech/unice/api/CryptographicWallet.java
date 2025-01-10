package fr.polytech.unice.api;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

import static fr.polytech.unice.JavaCardTerminal.concatenateArrays;

public record CryptographicWallet(
        KeyPair serversKeyPair,
        RSAPublicKey cardsPublicKey,
        boolean isToEncrypt
) {

    private static final int MAX_RSA_DATA_SIZE = 53;

    public CryptographicWallet encrypt() {
        return new CryptographicWallet(this.serversKeyPair, this.cardsPublicKey, true);
    }

    public byte[] getBytes(String response) {
        // If the wallet is not to encrypt, return the response as bytes
        if (!this.isToEncrypt) return response.getBytes();

        // Encrypt the response
        try {
            // Initialize the Cipher for encryption
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, this.cardsPublicKey);

            // Get the response as bytes
            byte[] responseBytes = response.getBytes();
            byte[] encryptedData = new byte[0];

            // Encrypt the response chunk by chunk
            for (int i = 0; i < responseBytes.length; i += MAX_RSA_DATA_SIZE) {
                byte[] chunk = new byte[Math.min(MAX_RSA_DATA_SIZE, responseBytes.length - i)];
                System.arraycopy(responseBytes, i, chunk, 0, chunk.length);
                byte[] encryptedChunk = cipher.doFinal(chunk);
                encryptedData = concatenateArrays(encryptedData, encryptedChunk);
            }

            return encryptedData;
        } catch (Exception e) {
            System.out.println("Error while encrypting the response: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
