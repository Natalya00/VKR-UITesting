
package com.example.runner.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "Токен обязателен")
        String token,
        
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password,
        
        @NotBlank(message = "Подтверждение пароля обязательно")
        String confirmPassword
) {
    public boolean passwordsMatch() {
        return password.equals(confirmPassword);
    }
}

