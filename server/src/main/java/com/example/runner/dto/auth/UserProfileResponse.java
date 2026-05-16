package com.example.runner.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с информацией о профиле пользователя
 * 
 * Содержит полную информацию о пользователе,
 * включая дату регистрации.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    /** Уникальный идентификатор пользователя */
    private Long id;
    
    /** Email адрес пользователя */
    private String email;
    
    /** Отображаемое имя пользователя (может быть null) */
    private String displayName;
    
    /** Дата и время регистрации пользователя */
    private LocalDateTime createdAt;
}
