package com.example.runner.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для ответа на запросы аутентификации
 * 
 * Содержит информацию о пользователе и токены доступа.
 * Используется при регистрации, входе и обновлении токенов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    /** Информация о пользователе */
    private UserInfo user;
    
    /** JWT access токен для авторизации запросов */
    private String accessToken;
    
    /** JWT refresh токен для обновления access токена */
    private String refreshToken;

    /**
     * Вложенный класс с базовой информацией о пользователе
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        /** Уникальный идентификатор пользователя */
        private Long id;
        
        /** Email адрес пользователя */
        private String email;
        
        /** Отображаемое имя пользователя (может быть null) */
        private String displayName;
    }
}
