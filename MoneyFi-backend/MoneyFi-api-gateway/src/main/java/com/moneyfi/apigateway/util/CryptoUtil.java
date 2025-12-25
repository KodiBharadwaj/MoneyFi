package com.moneyfi.apigateway.util;

import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class CryptoUtil {
    // 16 bytes = AES-128 (use 32 for AES-256 if allowed)
    private static final String SECRET_KEY = "MoneyFiSecretKey";
    private static final String INIT_VECTOR = "RandomInitVector";

    public String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes());
            SecretKeySpec skeySpec =
                    new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception ex) {
            throw new RuntimeException("Error encrypting value", ex);
        }
    }

    public String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INIT_VECTOR.getBytes());
            SecretKeySpec skeySpec =
                    new SecretKeySpec(SECRET_KEY.getBytes(), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original =
                    cipher.doFinal(Base64.getDecoder().decode(encrypted));

            return new String(original);

        } catch (Exception ex) {
            throw new RuntimeException("Error decrypting value", ex);
        }
    }
}
