package com.interviai.backend.module.auth.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtService.
 * 
 * @author InterviAI Team
 * @since 1.0.0
 */
class JwtServiceTest {

    private JwtService jwtService;
    
    private final String testSecret = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final String testIssuer = "test-issuer";
    private final long accessTokenValidity = 3600; // 1 hour
    private final long refreshTokenValidity = 2592000; // 30 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(jwtService, "jwtIssuer", testIssuer);
        ReflectionTestUtils.setField(jwtService, "accessTokenValiditySeconds", accessTokenValidity);
        ReflectionTestUtils.setField(jwtService, "refreshTokenValiditySeconds", refreshTokenValidity);
    }

    @Test
    void shouldGenerateAccessToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String role = "USER";

        // When
        String token = jwtService.generateAccessToken(userId, email, role);

        // Then
        assertNotNull(token);
        assertTrue(jwtService.isValidTokenFormat(token));
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
    }

    @Test
    void shouldGenerateRefreshToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";

        // When
        String token = jwtService.generateRefreshToken(userId, email, UUID.randomUUID().toString());

        // Then
        assertNotNull(token);
        assertTrue(jwtService.isValidTokenFormat(token));
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals("refresh", jwtService.extractTokenType(token));
    }

    @Test
    void shouldGenerateAccessTokenWithCustomClaims() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", "ADMIN");
        extraClaims.put("permissions", "read,write");

        extraClaims.put("userId", userId.toString());
        extraClaims.put("type", "access");

        // When
        String token = jwtService.generateToken(extraClaims, email, 3600);

        // Then
        assertNotNull(token);
        assertEquals(email, jwtService.extractUsername(token));
        assertEquals(userId, jwtService.extractUserId(token));
        assertEquals("access", jwtService.extractTokenType(token));
    }

    @Test
    void shouldValidateValidToken() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtService.generateAccessToken(userId, email, "USER");

        // When
        boolean isValid = jwtService.isTokenValid(token, email);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldRejectTokenWithWrongUsername() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String wrongEmail = "wrong@example.com";
        String token = jwtService.generateAccessToken(userId, email, "USER");

        // When
        boolean isValid = jwtService.isTokenValid(token, wrongEmail);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldDetectExpiredToken() throws InterruptedException {
        // Given - create service with very short token validity
        JwtService shortLivedJwtService = new JwtService();
        ReflectionTestUtils.setField(shortLivedJwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortLivedJwtService, "jwtIssuer", testIssuer);
        ReflectionTestUtils.setField(shortLivedJwtService, "accessTokenValiditySeconds", 1L); // 1 second
        
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = shortLivedJwtService.generateAccessToken(userId, email, "USER");

        // When - wait for token to expire
        Thread.sleep(1100); // Wait 1.1 seconds
        boolean isExpired = shortLivedJwtService.isTokenExpired(token);

        // Then
        assertTrue(isExpired);
    }

    @Test
    void shouldExtractClaims() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtService.generateAccessToken(userId, email, "USER");

        // When
        String extractedEmail = jwtService.extractClaim(token, Claims::getSubject);
        String extractedUserId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));

        // Then
        assertEquals(email, extractedEmail);
        assertEquals(userId.toString(), extractedUserId);
    }

    @Test
    void shouldCheckIfTokenNeedsRefresh() {
        // Given - create service with short token validity for testing
        JwtService shortLivedJwtService = new JwtService();
        ReflectionTestUtils.setField(shortLivedJwtService, "jwtSecret", testSecret);
        ReflectionTestUtils.setField(shortLivedJwtService, "jwtIssuer", testIssuer);
        ReflectionTestUtils.setField(shortLivedJwtService, "accessTokenValiditySeconds", 200L); // 200 seconds
        
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = shortLivedJwtService.generateAccessToken(userId, email, "USER");

        // When
        boolean shouldRefresh = shortLivedJwtService.shouldRefreshToken(token);

        // Then
        assertTrue(shouldRefresh); // Should be true because remaining time < 5 minutes
    }

    @Test
    void shouldValidateTokenFormat() {
        // Given
        String validToken = "header.payload.signature";
        String invalidToken1 = "invalid-token";
        String invalidToken2 = "header.payload";
        String invalidToken3 = "";
        String invalidToken4 = null;

        // When & Then
        assertTrue(jwtService.isValidTokenFormat(validToken));
        assertFalse(jwtService.isValidTokenFormat(invalidToken1));
        assertFalse(jwtService.isValidTokenFormat(invalidToken2));
        assertFalse(jwtService.isValidTokenFormat(invalidToken3));
        assertFalse(jwtService.isValidTokenFormat(invalidToken4));
    }

    @Test
    void shouldGetRemainingTokenTime() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String token = jwtService.generateAccessToken(userId, email, "USER");

        // When
        long remainingTime = jwtService.getRemainingTokenTime(token);

        // Then
        assertTrue(remainingTime > 0);
        assertTrue(remainingTime <= accessTokenValidity);
    }

    @Test
    void shouldReturnZeroRemainingTimeForInvalidToken() {
        // Given
        String invalidToken = "invalid.token.format";

        // When
        long remainingTime = jwtService.getRemainingTokenTime(invalidToken);

        // Then
        assertEquals(0, remainingTime);
    }

    @Test
    void shouldGetTokenValidityValues() {
        // When & Then
        assertEquals(accessTokenValidity, jwtService.getAccessTokenValidity());
        assertEquals(refreshTokenValidity, jwtService.getRefreshTokenValidity());
    }
}