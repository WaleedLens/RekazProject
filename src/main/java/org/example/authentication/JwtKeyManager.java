package org.example.authentication;

import com.google.inject.Singleton;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import java.util.Date;
import java.util.UUID;

/**
 * This class is responsible for providing RSA keys and JWT token operations.
 * It provides methods to read private and public keys, generate and validate JWT tokens.
 */
@Singleton
public class JwtKeyManager {
    private static final String PRIVATE_KEY_PATH = System.getProperty("PRIVATE_KEY_PATH");
    private static final String PUBLIC_KEY_PATH = System.getProperty("PUBLIC_KEY_PATH");
    private static final Logger log = LoggerFactory.getLogger(JwtKeyManager.class);

    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Constructor for the RsaKeyProvider.
     * Initializes the private and public keys.
     */
    public JwtKeyManager() {
        try {
            this.privateKey = readPrivateKey(PRIVATE_KEY_PATH);
            this.publicKey = readPublicKey(PUBLIC_KEY_PATH);
        } catch (Exception e) {
            throw new RuntimeException("Could not read RSA keys", e);
        }
    }

    /**
     * Reads the private key from the provided path.
     *
     * @param path The path to the private key.
     * @return The private key.
     * @throws Exception If an error occurs while reading the key.
     */
    private PrivateKey readPrivateKey(String path) throws Exception {
        byte[] privateKeyBytes = Files.readAllBytes(new File(path).toPath());
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        privateKey = keyFactory.generatePrivate(privateKeySpec);
        return privateKey;
    }

    /**
     * Reads the public key from the provided path.
     *
     * @param path The path to the public key.
     * @return The public key.
     * @throws Exception If an error occurs while reading the key.
     */
    private PublicKey readPublicKey(String path) throws Exception {
        byte[] publicKeyBytes = Files.readAllBytes(new File(path).toPath());
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
        return publicKey;
    }

    /**
     * Generates a JWT token with the provided subject.
     * The token is signed with the private key and has an expiration time of 1 hour.
     *
     * @param subject The subject of the JWT token.
     * @return The generated JWT token.
     */
    public String generateJwt(String subject) {
        log.info("Generating JWT...");
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour in milliseconds
        return Jwts.builder()
                .subject(subject)
                .expiration(expiryDate)
                .issuedAt(now)
                .id(UUID.randomUUID().toString())
                .signWith(privateKey)
                .compact();
    }

    /**
     * Validates a JWT token.
     * The token is verified with the public key.
     *
     * @param jwt The JWT token to validate.
     * @return true if the token is valid, false otherwise.
     */
    public boolean validateJwt(String jwt) {
        try {
            Jwts.parser()
                    .verifyWith(publicKey)
                    .build().parse(jwt);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token", e);
            return false;
        }
    }
}