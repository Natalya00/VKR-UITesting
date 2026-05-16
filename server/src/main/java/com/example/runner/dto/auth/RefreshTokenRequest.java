package com.example.runner.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса обновления access токена
 * 
 * Используется для получения нового access токена
 * по refresh токену без повторной аутентификации.
 */
@Data
public class RefreshTokenRequest {

    /** Refresh токен для обновления access токена (обязательно) */
    @NotBlank(message = "Refresh token не может быть пустым")
    private String refreshToken;
}
