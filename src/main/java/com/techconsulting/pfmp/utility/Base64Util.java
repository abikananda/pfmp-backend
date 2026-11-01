package com.techconsulting.pfmp.utility;

import com.techconsulting.pfmp.exception.BadRequestException;
import com.techconsulting.pfmp.model.JwtRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
public class Base64Util {
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public static String encode(String plainStr) {
        if (plainStr == null || plainStr.isEmpty()) {
            throw new IllegalArgumentException("Cannot encode a null or empty string");
        }

        String encodedString = Base64.getEncoder().encodeToString(plainStr.getBytes(StandardCharsets.UTF_8));
        log.info("Base64 Encoded String: {}", encodedString);
        return encodedString;
    }

    public static String decode(String encryptStr) {
        if (encryptStr == null || encryptStr.isEmpty()) {
            throw new IllegalArgumentException("Cannot decode a null or empty string");
        }

        try {
            byte[] decodedBytes = Base64.getDecoder().decode(encryptStr);
            String decodedString = new String(decodedBytes, StandardCharsets.UTF_8);
            log.info("Decoded String: {}", decodedString);
            return decodedString;
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 input: {}", encryptStr);
            throw new IllegalArgumentException("Invalid Base64 input. Decoding failed.");
        }
    }

    public static String getBasicAuthToken(String authorizationHeader) {
        if (StringUtils.isBlank(authorizationHeader)) {
            throw new BadRequestException("Authorization header is missing.");
        }

        // Ensure "Basic" is present and case-insensitive
        if (!authorizationHeader.toLowerCase().startsWith("basic ")) {
            throw new BadRequestException("Invalid Authorization header format. Expected 'Basic <token>'.");
        }

        // Extract token safely
        String token = authorizationHeader.substring(6).trim();
        if (StringUtils.isBlank(token)) {
            throw new BadRequestException("Basic Auth token is empty.");
        }

        return token;
    }


    public static JwtRequest getUsernameAndPassword(String basicAuthToken) {
        log.debug("Extracting username and password from Basic Auth token");
        if (StringUtils.isBlank(basicAuthToken)) {
            log.error("No Authorization header value provided");
            throw new BadRequestException("No Authorization header value");
        }

        String basicAuthStr = getBasicAuthToken(basicAuthToken);
        if (StringUtils.isBlank(basicAuthStr)) {
            log.error("No Basic auth token extracted");
            throw new BadRequestException("No Basic auth token");
        }

        try {
            String decodedAuth = decode(basicAuthStr);
            String[] userpass = decodedAuth.split(":", 2); // Ensures it splits at most once

            if (userpass.length < 2 || StringUtils.isBlank(userpass[0]) || StringUtils.isBlank(userpass[1])) {
                log.error("Invalid Basic Auth format");
                throw new BadRequestException("Invalid Basic Auth format. Expected 'username:password'.");
            }

            log.debug("Successfully extracted username from Basic Auth token");
            return new JwtRequest(userpass[0], userpass[1]);

        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 encoding in auth token", e);
            throw new BadRequestException("Invalid Base64 encoding");
        } catch (Exception e) {
            log.error("Error processing Basic Auth token", e);
            throw new BadRequestException("Wrong data format");
        }
    }


    // Generate a unique token using UUID and hashing (Optional: Use SHA-256)
    public static String generateUniqueToken() {
        log.debug("Generating unique token");
        String uuid = UUID.randomUUID().toString().replace("-", ""); // Remove dashes
        return hashToken(uuid); // Hash it for added security
    }

    // Generate a random value of specified length using SecureRandom
    public static String generateRandomValue(int length) {
        log.debug("Generating random value of length: {}", length);
        if (length <= 0) {
            log.error("Invalid length: {}", length);
            throw new IllegalArgumentException("Length must be greater than 0");
        }

        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(CHARACTERS.length());
            builder.append(CHARACTERS.charAt(index));
        }
        log.debug("Random value generated successfully");
        return builder.toString();
    }

    // Hash a token using SHA-256 for added security
    private static String hashToken(String token) {
        log.debug("Hashing token using SHA-256");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            log.debug("Token hashed successfully");
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 Algorithm not found", e);
            throw new RuntimeException("SHA-256 Algorithm not found", e);
        }
    }
}
