package com.execunit.jwt.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class PublicKeyService {
    private static final Logger log = LoggerFactory.getLogger(PublicKeyService.class);

    public RSAPublicKey loadPublicKeyFromFile(File file) throws Exception {
        log.debug("Loading public key from file: {}", file.getAbsolutePath());
        String key = Files.readString(file.toPath());

        // Remove the PEM format headers and newlines if present
        String publicKeyPEM = key
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

        // Decode the Base64 encoded key
        byte[] encoded = Base64.getDecoder().decode(publicKeyPEM);

        // Create the public key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
        RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

        log.debug("Successfully loaded RSA public key");
        return publicKey;
    }
}
