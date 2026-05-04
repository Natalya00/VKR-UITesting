package com.example.runner.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secretKey = "mySecretKeyForTestingPurposesOnly1234567890";
    private final Long testUserId = 1L;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpirationMinutes", 15L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationDays", 7L);
        
        ReflectionTestUtils.invokeMethod(jwtTokenProvider, "init");
    }

    @Test
    @DisplayName("Должен генерировать валидный access токен")
    void shouldGenerateValidAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(testUserId, testEmail);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(testUserId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(testEmail, jwtTokenProvider.getEmailFromToken(token));
        assertFalse(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    @DisplayName("Должен генерировать валидный refresh токен")
    void shouldGenerateValidRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(testUserId, testEmail);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(testUserId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(testEmail, jwtTokenProvider.getEmailFromToken(token));
        assertTrue(jwtTokenProvider.isRefreshToken(token));
    }

    @Test
    @DisplayName("Должен корректно извлекать userId из токена")
    void shouldExtractUserIdFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        Long extractedUserId = jwtTokenProvider.getUserIdFromToken(token);
        assertEquals(testUserId, extractedUserId);
    }

    @Test
    @DisplayName("Должен корректно извлекать email из токена")
    void shouldExtractEmailFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String extractedEmail = jwtTokenProvider.getEmailFromToken(token);
        assertEquals(testEmail, extractedEmail);
    }

    @Test
    @DisplayName("Должен корректно определять тип токена")
    void shouldCorrectlyIdentifyTokenType() {
        String accessToken = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUserId, testEmail);

        assertFalse(jwtTokenProvider.isRefreshToken(accessToken));
        assertTrue(jwtTokenProvider.isRefreshToken(refreshToken));
    }

    @Test
    @DisplayName("Должен валидировать корректные токены")
    void shouldValidateCorrectTokens() {
        String accessToken = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUserId, testEmail);

        assertTrue(jwtTokenProvider.validateToken(accessToken));
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
    }

    @Test
    @DisplayName("Должен отклонять невалидные токены")
    void shouldRejectInvalidTokens() {
        String invalidToken = "invalid.token.here";
        String emptyToken = "";
        String nullToken = null;
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
        assertFalse(jwtTokenProvider.validateToken(emptyToken));
        assertFalse(jwtTokenProvider.validateToken(nullToken));
    }

    @Test
    @DisplayName("Должен отклонять токены с неверной подписью")
    void shouldRejectTokensWithWrongSignature() {
        String tokenWithWrongSignature = createTokenWithWrongSignature();
        assertFalse(jwtTokenProvider.validateToken(tokenWithWrongSignature));
    }

    @Test
    @DisplayName("Должен корректно извлекать дату истечения токена")
    void shouldExtractExpirationDateFromToken() {
        String token = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        LocalDateTime expirationDate = jwtTokenProvider.getExpiryDateFromToken(token);

        assertNotNull(expirationDate);
        assertTrue(expirationDate.isAfter(LocalDateTime.now()));
        assertTrue(expirationDate.isBefore(LocalDateTime.now().plusMinutes(16)));
    }

    @Test
    @DisplayName("Должен корректно хешировать токены")
    void shouldHashTokensCorrectly() {
        String token = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String hash1 = jwtTokenProvider.hashToken(token);
        String hash2 = jwtTokenProvider.hashToken(token);

        assertNotNull(hash1);
        assertNotNull(hash2);
        assertEquals(hash1, hash2); 
        assertNotEquals(token, hash1); 
    }

    @Test
    @DisplayName("Должен генерировать разные хеши для разных токенов")
    void shouldGenerateDifferentHashesForDifferentTokens() {
        String token1 = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String token2 = jwtTokenProvider.generateAccessToken(testUserId + 1, testEmail);
        String hash1 = jwtTokenProvider.hashToken(token1);
        String hash2 = jwtTokenProvider.hashToken(token2);

        assertNotEquals(hash1, hash2);
    }

    @Test
    @DisplayName("Должен обрабатывать null и пустые строки при хешировании")
    void shouldHandleNullAndEmptyStringsWhenHashing() {
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.hashToken(null));
        assertThrows(IllegalArgumentException.class, () -> jwtTokenProvider.hashToken(""));
    }

    @Test
    @DisplayName("Должен генерировать токены с правильным временем жизни")
    void shouldGenerateTokensWithCorrectLifetime() {
        String accessToken = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String refreshToken = jwtTokenProvider.generateRefreshToken(testUserId, testEmail);

        LocalDateTime accessExpiry = jwtTokenProvider.getExpiryDateFromToken(accessToken);
        LocalDateTime refreshExpiry = jwtTokenProvider.getExpiryDateFromToken(refreshToken);

        LocalDateTime now = LocalDateTime.now();

        assertTrue(accessExpiry.isAfter(now.plusMinutes(14)));
        assertTrue(accessExpiry.isBefore(now.plusMinutes(16)));

        assertTrue(refreshExpiry.isAfter(now.plusDays(6)));
        assertTrue(refreshExpiry.isBefore(now.plusDays(8)));
    }

    @Test
    @DisplayName("Должен обрабатывать исключения при извлечении данных из невалидного токена")
    void shouldHandleExceptionsWhenExtractingFromInvalidToken() {
        String invalidToken = "invalid.token.here";
        assertThrows(Exception.class, () -> jwtTokenProvider.getUserIdFromToken(invalidToken));
        assertThrows(Exception.class, () -> jwtTokenProvider.getEmailFromToken(invalidToken));
        assertThrows(Exception.class, () -> jwtTokenProvider.getExpiryDateFromToken(invalidToken));
    }

    @Test
    @DisplayName("Должен генерировать токены с одинаковыми данными для одного пользователя")
    void shouldGenerateUniqueTokensForSameUser() {
        String token1 = jwtTokenProvider.generateAccessToken(testUserId, testEmail);
        String token2 = jwtTokenProvider.generateAccessToken(testUserId, testEmail);

        assertEquals(jwtTokenProvider.getUserIdFromToken(token1), jwtTokenProvider.getUserIdFromToken(token2));
        assertEquals(jwtTokenProvider.getEmailFromToken(token1), jwtTokenProvider.getEmailFromToken(token2));
    }

    @Test
    @DisplayName("Должен корректно работать с различными типами пользователей")
    void shouldWorkWithDifferentUserTypes() {
        Long userId1 = 1L;
        Long userId2 = 999999L;
        String email1 = "user1@example.com";
        String email2 = "very.long.email.address@subdomain.example.com";
        String token1 = jwtTokenProvider.generateAccessToken(userId1, email1);
        String token2 = jwtTokenProvider.generateAccessToken(userId2, email2);

        assertTrue(jwtTokenProvider.validateToken(token1));
        assertTrue(jwtTokenProvider.validateToken(token2));
        
        assertEquals(userId1, jwtTokenProvider.getUserIdFromToken(token1));
        assertEquals(userId2, jwtTokenProvider.getUserIdFromToken(token2));
        assertEquals(email1, jwtTokenProvider.getEmailFromToken(token1));
        assertEquals(email2, jwtTokenProvider.getEmailFromToken(token2));
    }

    private String createTokenWithWrongSignature() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("wrongSecretKeyForTestingPurposes123456789".getBytes());
        
        return Jwts.builder()
                .setSubject(testUserId.toString())
                .claim("email", testEmail)
                .claim("type", "access")
                .setIssuedAt(new Date())
                .setExpiration(Date.from(LocalDateTime.now().plusMinutes(15)
                        .atZone(ZoneId.systemDefault()).toInstant()))
                .signWith(wrongKey)
                .compact();
    }
}