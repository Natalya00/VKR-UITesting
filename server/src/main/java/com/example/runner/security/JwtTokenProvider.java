package com.example.runner.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Провайдер для работы с JWT токенами
 * 
 * Предоставляет функциональность для:
 * - Генерации access, refresh и reset токенов
 * - Валидации токенов с обработкой ошибок
 * - Извлечения данных из токенов (userId, email, тип)
 * - Хеширования токенов для безопасного хранения
 */
@Component
public class JwtTokenProvider {

    /** Логгер для отслеживания операций с токенами */
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    /** Секретный ключ для подписи токенов */
    @Value("${jwt.secret}")
    private String secretKey;

    /** Время жизни access токена в минутах */
    @Value("${jwt.access-token-expiration-minutes:15}")
    private long accessTokenExpirationMinutes;

    /** Время жизни refresh токена в днях */
    @Value("${jwt.refresh-token-expiration-days:7}")
    private long refreshTokenExpirationDays;

    /** Время жизни reset токена в минутах */
    @Value("${jwt.reset-token-expiration-minutes:5}")
    private long resetTokenExpirationMinutes;

    /** Ключ для подписи токенов */
    private SecretKey key;

    /** Инициализирует ключ подписи на основе секретной строки */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерирует access токен для авторизации
     * @param userId идентификатор пользователя
     * @param email email пользователя
     * @return подписанный JWT токен
     */
    public String generateAccessToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + accessTokenExpirationMinutes * 60 * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Генерирует refresh токен для обновления access токенов
     * @param userId идентификатор пользователя
     * @param email email пользователя
     * @return подписанный JWT токен с уникальным JTI
     */
    public String generateRefreshToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        claims.put("jti", java.util.UUID.randomUUID().toString());

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + refreshTokenExpirationDays * 24 * 60 * 60 * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * Генерирует одноразовый токен для сброса пароля
     * @param userId идентификатор пользователя
     * @param email email пользователя
     * @return короткоживущий JWT токен
     */
    public String generateResetToken(Long userId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "reset");

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + resetTokenExpirationMinutes * 60 * 1000);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /** Извлекает ID пользователя из токена */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /** Извлекает email пользователя из токена */
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("email", String.class);
    }

    /**
     * Валидирует JWT токен
     * @param token JWT токен для проверки
     * @return true если токен действителен
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.error("JWT signature does not match: {}", e.getMessage());
        }
        return false;
    }

    /** Проверяет, является ли токен access токеном */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "access".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /** Проверяет, является ли токен refresh токеном */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    /** Извлекает дату истечения токена */
    public LocalDateTime getExpiryDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    /**
     * Создает хеш токена для безопасного хранения
     * @param token JWT токен для хеширования
     * @return хеш токена
     * @throws IllegalArgumentException если токен null или пустой
     */
    public String hashToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }
        return String.valueOf(token.hashCode());
    }
}
