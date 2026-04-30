package org.instantmessenger.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class JwtService {

    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder URL_DECODER = Base64.getUrlDecoder();

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
            @Value("${app.jwt.secret:change-me-in-application-yaml}") String secret,
            @Value("${app.jwt.expiration-seconds:86400}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(long userId, String email) {
        long expiresAt = Instant.now().plusSeconds(expirationSeconds).getEpochSecond();
        String header = encode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = encode("{\"sub\":\"" + userId + "\",\"email\":\"" + escape(email) + "\",\"exp\":" + expiresAt + "}");
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    public long validateAndGetUserId(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        String payload = new String(URL_DECODER.decode(parts[1]), StandardCharsets.UTF_8);
        long expiresAt = readLongClaim(payload, "exp");
        if (Instant.now().getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("Token expired");
        }

        return readLongStringClaim(payload, "sub");
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign token", ex);
        }
    }

    private String encode(String value) {
        return URL_ENCODER.encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private long readLongClaim(String json, String claim) {
        String marker = "\"" + claim + "\":";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing token claim");
        }
        start += marker.length();
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        return Long.parseLong(json.substring(start, end));
    }

    private long readLongStringClaim(String json, String claim) {
        String marker = "\"" + claim + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Missing token claim");
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        if (end < 0) {
            throw new IllegalArgumentException("Invalid token claim");
        }
        return Long.parseLong(json.substring(start, end));
    }

    private boolean constantTimeEquals(String first, String second) {
        return MessageDigestHelper.constantTimeEquals(first.getBytes(StandardCharsets.UTF_8), second.getBytes(StandardCharsets.UTF_8));
    }

    private static class MessageDigestHelper {
        static boolean constantTimeEquals(byte[] first, byte[] second) {
            if (first.length != second.length) {
                return false;
            }

            int result = 0;
            for (int i = 0; i < first.length; i++) {
                result |= first[i] ^ second[i];
            }
            return result == 0;
        }
    }
}
