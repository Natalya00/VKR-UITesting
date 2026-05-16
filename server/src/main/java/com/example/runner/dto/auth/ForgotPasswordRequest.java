
package com.example.runner.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO для запроса восстановления пароля
 * 
 * Используется для инициации процесса сброса пароля.
 * После валидации email генерируется токен сброса.
 * 
 * @param email email адрес пользователя (обязательно, должен существовать)
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "Email не может быть пустым")
        @Email(message = "Некорректный формат email")
        String email
) {}

