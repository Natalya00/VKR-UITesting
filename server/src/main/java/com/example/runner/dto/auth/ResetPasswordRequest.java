
package com.example.runner.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса сброса пароля
 * 
 * Используется для установки нового пароля по токену сброса.
 * Включает валидацию совпадения паролей и их минимальной длины.
 * 
 * @param token токен сброса пароля (обязательно)
 * @param password новый пароль (минимум 6 символов)
 * @param confirmPassword подтверждение нового пароля (должно совпадать)
 */
public record ResetPasswordRequest(
        @NotBlank(message = "Токен обязателен")
        String token,
        
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
        String password,
        
        @NotBlank(message = "Подтверждение пароля обязательно")
        String confirmPassword
) {
    /**
     * Проверяет совпадение пароля и его подтверждения
     * 
     * @return true если пароли совпадают, false иначе
     */
    public boolean passwordsMatch() {
        return password.equals(confirmPassword);
    }
}

