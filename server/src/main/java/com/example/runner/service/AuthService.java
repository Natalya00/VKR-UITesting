package com.example.runner.service;

import com.example.runner.dto.auth.AuthResponse;
import com.example.runner.dto.auth.ForgotPasswordRequest;
import com.example.runner.dto.auth.LoginRequest;
import com.example.runner.dto.auth.RegisterRequest;
import com.example.runner.dto.auth.ResetPasswordRequest;
import com.example.runner.model.RevokedToken;
import com.example.runner.model.ResetToken;
import com.example.runner.model.User;
import com.example.runner.repository.RevokedTokenRepository;
import com.example.runner.repository.ResetTokenRepository;
import com.example.runner.repository.UserRepository;
import com.example.runner.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Сервис для управления аутентификацией и авторизацией пользователей
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /** Логгер для отслеживания операций аутентификации */
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    /** Репозиторий для работы с пользователями */
    private final UserRepository userRepository;
    
    /** Репозиторий для управления отозванными токенами */
    private final RevokedTokenRepository revokedTokenRepository;
    
    /** Провайдер для генерации и валидации JWT токенов */
    private final JwtTokenProvider jwtTokenProvider;
    
    /** Энкодер для хеширования паролей */
    private final PasswordEncoder passwordEncoder;
    
    /** Менеджер аутентификации Spring Security */
    private final AuthenticationManager authenticationManager;
    
    /** Репозиторий для управления токенами сброса пароля */
    private final ResetTokenRepository resetTokenRepository;

    /**
     * Регистрирует нового пользователя в системе
     * 
     * Выполняет следующие операции:
     * - Проверяет уникальность email адреса
     * - Хеширует пароль с помощью BCrypt
     * - Создает нового пользователя в базе данных
     * - Генерирует JWT токены для автоматического входа
     * - Устанавливает HTTP-only cookies с токенами
     * 
     * @param request данные для регистрации (email, password)
     * @param response HTTP ответ для установки cookies
     * @return информация о пользователе и токены доступа
     * @throws IllegalArgumentException если пользователь с таким email уже существует
     */
    @Transactional
    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        log.info("Пользователь зарегистрирован: {}", user.getEmail());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        setAuthCookies(accessToken, refreshToken, response);

        return AuthResponse.builder()
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(null)
                        .build())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Выполняет вход пользователя в систему
     * 
     * Процесс аутентификации:
     * - Проверяет учетные данные через AuthenticationManager
     * - Находит пользователя в базе данных
     * - Генерирует новые JWT токены
     * - Устанавливает HTTP-only cookies с токенами
     * 
     * @param request данные для входа (email, password)
     * @param response HTTP ответ для установки cookies
     * @return информация о пользователе и токены доступа
     * @throws IllegalArgumentException если учетные данные неверны
     */
    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId(), user.getEmail());

        setAuthCookies(accessToken, refreshToken, response);

        return AuthResponse.builder()
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(null)
                        .build())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Выполняет выход пользователя из системы
     * 
     * Операции при выходе:
     * - Добавляет действующие токены в черный список (blacklist)
     * - Сохраняет хеши токенов в базе данных для предотвращения повторного использования
     * - Очищает HTTP-only cookies с токенами
     * - Логирует операцию для аудита безопасности
     * 
     * @param accessToken текущий access токен пользователя
     * @param refreshToken текущий refresh токен пользователя
     * @param response HTTP ответ для очистки cookies
     */
    @Transactional
    public void logout(String accessToken, String refreshToken, HttpServletResponse response) {
        if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(accessToken);
            String accessHash = jwtTokenProvider.hashToken(accessToken);
            RevokedToken revokedAccess = RevokedToken.builder()
                    .user(userRepository.findById(userId).orElse(null))
                    .tokenHash(accessHash)
                    .expiryDate(jwtTokenProvider.getExpiryDateFromToken(accessToken))
                    .build();
            revokedTokenRepository.save(revokedAccess);
            log.debug("Access token добавлен в blacklist для пользователя {}", userId);
        }

        if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
            String refreshHash = jwtTokenProvider.hashToken(refreshToken);
            RevokedToken revokedRefresh = RevokedToken.builder()
                    .user(userRepository.findById(userId).orElse(null))
                    .tokenHash(refreshHash)
                    .expiryDate(jwtTokenProvider.getExpiryDateFromToken(refreshToken))
                    .build();
            revokedTokenRepository.save(revokedRefresh);
            log.debug("Refresh token добавлен в blacklist для пользователя {}", userId);
        }

        clearAuthCookies(response);
    }

    /**
     * Обновляет access токен используя действующий refresh токен
     * 
     * Процесс обновления:
     * - Валидирует refresh токен (подпись, срок действия, тип)
     * - Проверяет, что токен не находится в черном списке
     * - Добавляет старый refresh токен в черный список
     * - Генерирует новую пару токенов (access + refresh)
     * - Устанавливает новые HTTP-only cookies
     * 
     * @param refreshToken действующий refresh токен
     * @param response HTTP ответ для установки новых cookies
     * @return новые токены доступа и информация о пользователе
     * @throws IllegalArgumentException если токен недействителен, отозван или не является refresh токеном
     */
    @Transactional
    public AuthResponse refreshTokens(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
                throw new IllegalArgumentException("Неверный refresh token");
        }

        String tokenHash = jwtTokenProvider.hashToken(refreshToken);
        if (revokedTokenRepository.existsByTokenHash(tokenHash)) {
                throw new IllegalArgumentException("Refresh token был отозван");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
                throw new IllegalArgumentException("Это не refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        RevokedToken revokedToken = RevokedToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(jwtTokenProvider.getExpiryDateFromToken(refreshToken))
                .build();
        revokedTokenRepository.save(revokedToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, user.getEmail());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId, user.getEmail());

        setAuthCookies(newAccessToken, newRefreshToken, response);

        return AuthResponse.builder()
                .user(AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .displayName(null)
                        .build())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    /**
     * Получает информацию о текущем аутентифицированном пользователе
     * @param userId идентификатор пользователя из JWT токена
     * @return базовая информация о пользователе (id, email, displayName)
     * @throws IllegalArgumentException если пользователь не найден
     */
    @Transactional(readOnly = true)
    public AuthResponse.UserInfo getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        return AuthResponse.UserInfo.builder()
                .id(user.getId())
                .email(user.getEmail())
                .displayName(null)
                .build();
    }

    /**
     * Инициирует процесс восстановления пароля
     * 
     * Выполняемые операции:
     * - Проверяет существование пользователя с указанным email
     * - Очищает просроченные токены сброса пароля
     * - Генерирует новый одноразовый токен сброса
     * - Сохраняет токен в базе данных с временем истечения
     * 
     * @param request запрос с email адресом пользователя
     * @return токен для сброса пароля
     * @throws IllegalArgumentException если пользователь с таким email не найден
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с таким email не найден"));
        
        resetTokenRepository.deleteByExpiryDateBefore(LocalDateTime.now());
        
        String resetToken = jwtTokenProvider.generateResetToken(user.getId(), user.getEmail());
        LocalDateTime expiryDate = jwtTokenProvider.getExpiryDateFromToken(resetToken);
        
        ResetToken tokenEntity = ResetToken.builder()
                .user(user)
                .token(resetToken)
                .expiryDate(expiryDate)
                .used(false)
                .build();
        
        resetTokenRepository.save(tokenEntity);
        log.info("Password reset requested for: {}", user.getEmail());
        
        return resetToken;
        }

    /**
     * Выполняет сброс пароля пользователя по токену
     * 
     * Процесс сброса:
     * - Проверяет совпадение нового пароля и его подтверждения
     * - Валидирует токен сброса (существование, срок действия, использование)
     * - Хеширует новый пароль и сохраняет в базе данных
     * - Помечает токен как использованный для предотвращения повторного использования
     * 
     * @param request данные для сброса (токен, новый пароль, подтверждение)
     * @throws IllegalArgumentException если пароли не совпадают или токен недействителен
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.passwordsMatch()) {
            throw new IllegalArgumentException("Пароли не совпадают");
        }

        LocalDateTime now = LocalDateTime.now();
        ResetToken tokenEntity = resetTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                request.token(), now)
                .orElseThrow(() -> new IllegalArgumentException("Неверный, просроченный или уже использованный токен"));

        User user = tokenEntity.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        tokenEntity.setUsed(true);
        resetTokenRepository.save(tokenEntity);

        log.info("Password reset successful for user: {}", user.getEmail());
    }

    /** Время жизни access токена в секундах (15 минут) */
    private static final long ACCESS_TOKEN_MAX_AGE = 15 * 60;
    
    /** Время жизни refresh токена в секундах (7 дней) */
    private static final long REFRESH_TOKEN_MAX_AGE = 7 * 24 * 60 * 60;

    /**
     * Устанавливает HTTP-only cookies с токенами аутентификации
     * @param accessToken JWT access токен
     * @param refreshToken JWT refresh токен
     * @param response HTTP ответ для установки cookies
     */
    private void setAuthCookies(String accessToken, String refreshToken, HttpServletResponse response) {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(false) 
                .sameSite("Strict")
                .path("/")
                .maxAge(ACCESS_TOKEN_MAX_AGE) 
                .build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(REFRESH_TOKEN_MAX_AGE) 
                .build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());
    }

    /**
     * Очищает cookies с токенами аутентификации
     * @param response HTTP ответ для очистки cookies
     */
    private void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", clearAccess.toString());
        response.addHeader("Set-Cookie", clearRefresh.toString());
    }
}
