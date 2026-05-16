package com.example.runner.controller;

import com.example.runner.dto.auth.AuthResponse;
import com.example.runner.dto.auth.LoginRequest;
import com.example.runner.dto.auth.ForgotPasswordRequest;
import com.example.runner.dto.auth.RefreshTokenRequest;
import com.example.runner.dto.auth.RegisterRequest;
import com.example.runner.dto.auth.ResetPasswordRequest;
import com.example.runner.security.UserDetailsImpl;
import com.example.runner.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * REST контроллер для управления аутентификацией и авторизацией пользователей
 * 
 * Предоставляет эндпоинты для:
 * - Регистрации новых пользователей
 * - Входа в систему с выдачей JWT токенов
 * - Выхода из системы с отзывом токенов
 * - Обновления access токенов через refresh токены
 * - Восстановления и сброса паролей
 * - Получения информации о текущем пользователе
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /** Сервис для работы с аутентификацией */
    private final AuthService authService;

    /**
     * Регистрирует нового пользователя в системе
     * @param request данные для регистрации (email, password)
     * @param response HTTP ответ для установки cookies с токенами
     * @return информация о пользователе и токены доступа
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.register(request, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Выполняет вход пользователя в систему
     * @param request данные для входа (email, password)
     * @param response HTTP ответ для установки cookies с токенами
     * @return информация о пользователе и токены доступа
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(request, response);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * Выполняет выход пользователя из системы
     * Отзывает токены и очищает cookies
     * @param request HTTP запрос для получения токенов из cookies
     * @param response HTTP ответ для очистки cookies
     * @return пустой ответ со статусом 200
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String accessToken = getCookieValue(request, "accessToken");
        String refreshToken = getCookieValue(request, "refreshToken");

        authService.logout(accessToken, refreshToken, response);
        return ResponseEntity.ok().build();
    }

    /**
     * Обновляет access токен используя refresh токен из cookies
     * @param request HTTP запрос для получения refresh токена из cookies
     * @param response HTTP ответ для установки новых cookies с токенами
     * @return новые токены доступа
     * @throws IllegalArgumentException если refresh токен не найден
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = getCookieValue(request, "refreshToken");
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token not found");
        }
        AuthResponse authResponse = authService.refreshTokens(refreshToken, response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh/body")
    public ResponseEntity<AuthResponse> refreshWithBody(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.refreshTokens(request.getRefreshToken(), response);
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {
        String token = authService.forgotPassword(request);
        Map<String, String> response = Map.of(
            "message", "Email подтвержден. Введите новый пароль.",
            "token", token
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password") 
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        authService.resetPassword(request);
        Map<String, String> response = Map.of("message", "Пароль успешно сброшен");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        AuthResponse.UserInfo userInfo = authService.getCurrentUser(userDetails.getId());
        return ResponseEntity.ok(userInfo);
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", "Ошибка сервера: " + ex.getMessage());
        return ResponseEntity.internalServerError().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> error = new HashMap<>();
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("Ошибка валидации");
        error.put("message", message);
        return ResponseEntity.badRequest().body(error);
    }
}
