package com.execunit.jwt.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.interfaces.RSAPublicKey;


public class JwtClaimsService {
    private static final Logger log = LoggerFactory.getLogger(JwtClaimsService.class);
    private final PublicKeyService publicKeyService = new PublicKeyService();

    private final RSAPublicKey publicKey;

    public JwtClaimsService(File publicKeyFile) throws Exception {
        log.debug("Initializing JwtService");
        this.publicKey = publicKeyService.loadPublicKeyFromFile(publicKeyFile);
    }


    public JWTClaimsSet validateToken(String token) throws Exception {
        log.debug("Validating JWT token");

        SignedJWT jwt = SignedJWT.parse(token);

        // Verify the signature
        JWSVerifier verifier = new RSASSAVerifier(publicKey);
        if (!jwt.verify(verifier)) {
            log.debug("JWT signature verification failed");
            throw new JOSEException("JWT signature verification failed");
        }

        // Get and validate the claims set
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        validateClaims(claims);

        log.debug("JWT token validated successfully");
        return claims;
    }

    private void validateClaims(JWTClaimsSet claims) throws Exception {
        // Check expiration if set
        if (claims.getExpirationTime() != null &&
                claims.getExpirationTime().getTime() < System.currentTimeMillis()) {
            log.debug("JWT is expired");
            throw new JOSEException("JWT is expired");
        }
    }
}
