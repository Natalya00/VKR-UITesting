package com.example.runner.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса регистрации нового пользователя
 * 
 * Содержит данные для создания новой учетной записи.
 * Включает валидацию email и минимальной длины пароля.
 */
@Data
public class RegisterRequest {

    /** Email адрес нового пользователя (обязательно, должен быть уникальным) */
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;

    /** Пароль нового пользователя (минимум 6 символов) */
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;
}
