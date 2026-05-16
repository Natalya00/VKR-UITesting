
package com.example.runner.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Модель токена для сброса пароля
 * Представляет одноразовый токен для восстановления пароля.
 * Токен имеет ограниченное время жизни и может быть
 * использован только однажды.
 */
@Entity
@Table(name = "reset_token")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetToken {
    
    /** Уникальный идентификатор токена */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /** Пользователь, для которого создан токен */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    /** Строковое представление токена */
    @Column(name = "token", nullable = false, length = 500)
    private String token;
    
    /** Дата и время истечения токена */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;
    
    /** Флаг, обозначающий, что токен уже был использован */
    @Column(name = "used", nullable = false)
    private boolean used = false;
    
    /**
     * Проверяет, истек ли токен
     * @return true, если текущая дата позже даты истечения
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * Проверяет, был ли токен уже использован
     * @return true, если токен уже был использован
     */
    public boolean isUsed() {
        return used;
    }
}

