package com.mahajan.habittracker.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT generation & verification overview:
 *
 * 1. Signature: cryptographic proof using server secret
 *    - When generating a JWT, the header and payload are signed with a secret key.
 *    - Formula (simplified): JWT = base64(header) + "." + base64(payload) + "." + HMAC_SHA256(header+payload, secret)
 *    - Only the server knows the secret; if the payload is modified, the signature becomes invalid.
 *
 * 2. Verification: extracts token → verifies signature with secret
 *    - Incoming requests include the JWT in the Authorization header.
 *    - The server recalculates the signature from header+payload and secret.
 *    - If signatures match → token is authentic; if not → reject request (401).
 *    - The server also checks the 'exp' claim to ensure the token is not expired.
 *
 * This ensures stateless, tamper-proof authentication for users.
 */
@Component
@Setter
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration; // in milliseconds

    public String generateToken(String email) {
        return Jwts.builder()
                .setSubject(email) // email is JWT 'sub'
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(Keys.hmacShaKeyFor(secret.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes()))
                .build()
                .parseClaimsJws(token)// ensures token is not tampered and unexpired
                .getBody()
                .getSubject();
    }
}
