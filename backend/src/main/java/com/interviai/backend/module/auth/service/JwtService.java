package com.interviai.backend.module.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Service for JWT token operations.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.issuer}")
    private String jwtIssuer;

    @Value("${app.jwt.access-token-validity}")
    private long accessTokenValiditySeconds;

    @Value("${app.jwt.refresh-token-validity}")
    private long refreshTokenValiditySeconds;

    /**
     * Extract username (email) from JWT token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extract user ID from JWT token.
     */
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    /**
     * Extract expiration date from JWT token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extract issued at date from JWT token.
     */
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    /**
     * Extract token ID from JWT token.
     */
    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    /**
     * Extract token type from JWT token.
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("type", String.class));
    }

    /**
     * Extract specific claim from JWT token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Generate access token for user.
     */
    public String generateAccessToken(UUID userId, String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId.toString());
        extraClaims.put("role", role);
        extraClaims.put("type", "access");
        
        return generateToken(extraClaims, email, accessTokenValiditySeconds);
    }

    /**
     * Generate refresh token for user.
     */
    public String generateRefreshToken(UUID userId, String email, String tokenId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId.toString());
        extraClaims.put("type", "refresh");
        
        return buildToken(extraClaims, email, refreshTokenValiditySeconds, tokenId);
    }

    /**
     * Generate token with extra claims.
     */
    public String generateToken(Map<String, Object> extraClaims, String subject, long validitySeconds) {
        return buildToken(extraClaims, subject, validitySeconds, UUID.randomUUID().toString());
    }

    /**
     * Check if token format is valid.
     */
    public boolean isValidTokenFormat(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }

    /**
     * Check if token is valid for the given user.
     */
    public boolean isTokenValid(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return extractedUsername.equals(username) && !isTokenExpired(token);
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            log.debug("Failed to check token expiration: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Check if token is refresh token.
     */
    public boolean isRefreshToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            log.debug("Failed to check token type: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Check if token is access token.
     */
    public boolean isAccessToken(String token) {
        try {
            String tokenType = extractTokenType(token);
            return "access".equals(tokenType);
        } catch (Exception e) {
            log.debug("Failed to check token type: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Extract claims without signature verification (for expired tokens).
     */
    public Claims extractClaimsUnsafe(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.debug("Failed to extract claims unsafely: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get remaining time until token expiration in seconds.
     */
    public long getRemainingTokenTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Get token creation time as LocalDateTime.
     */
    public LocalDateTime getTokenCreationTime(String token) {
        try {
            Date issuedAt = extractIssuedAt(token);
            return issuedAt.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get token expiration time as LocalDateTime.
     */
    public LocalDateTime getTokenExpirationTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Build JWT token with specified parameters.
     */
    private String buildToken(Map<String, Object> claims, String subject, long validitySeconds, String tokenId) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validitySeconds * 1000);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtIssuer)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setId(tokenId)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract all claims from JWT token.
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Failed to extract claims from token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Get signing key for JWT tokens.
     * The secret is stored as a hex string; decode it to raw bytes for HMAC.
     */
    private SecretKey getSignInKey() {
        try {
            // Attempt to decode as hex (e.g. "404E635266556A58..." format)
            byte[] keyBytes = hexStringToByteArray(jwtSecret);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            // Fallback: treat secret as a plain UTF-8 string (for local dev secrets)
            log.debug("JWT secret is not hex-encoded, using raw bytes");
            return Keys.hmacShaKeyFor(jwtSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    /**
     * Decode a hex string to a byte array.
     */
    private byte[] hexStringToByteArray(String hex) {
        if (hex == null || hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            int hi = Character.digit(hex.charAt(i), 16);
            int lo = Character.digit(hex.charAt(i + 1), 16);
            if (hi == -1 || lo == -1) throw new IllegalArgumentException("Non-hex character in secret");
            data[i / 2] = (byte) ((hi << 4) + lo);
        }
        return data;
    }

    /**
     * Get access token validity in seconds.
     */
    public long getAccessTokenValidity() {
        return accessTokenValiditySeconds;
    }

    /**
     * Check if token should be refreshed (near expiration).
     */
    public boolean shouldRefreshToken(String token) {
        try {
            long remainingTime = getRemainingTokenTime(token);
            // Refresh if less than 5 minutes remaining
            return remainingTime > 0 && remainingTime < 300;
        } catch (Exception e) {
            log.debug("Failed to check if token should be refreshed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get refresh token validity in seconds.
     */
    public long getRefreshTokenValidity() {
        return refreshTokenValiditySeconds;
    }
}