package com.example.runner.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса входа в систему
 * 
 * Содержит учетные данные пользователя для аутентификации.
 * Включает валидацию формата email и обязательности полей.
 */
@Data
public class LoginRequest {

    /** Email адрес пользователя (обязательно, должен быть валидным) */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    /** Пароль пользователя (обязательно) */
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
