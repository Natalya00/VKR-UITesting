package com.example.runner.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Модель отозванного JWT токена
 * Используется для ведения черного списка токенов
 * при выходе из системы и обновлении токенов.
 */
@Entity
@Table(name = "revoked_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedToken {

    /** Уникальный идентификатор записи */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ссылка на пользователя */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_revoked_tokens_user"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /** Хеш отозванного токена */
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    /** Дата истечения токена */
    @Column(name = "expiry_date", nullable = false)
    private LocalDateTime expiryDate;

    /** Время создания записи */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Проверяет, истек ли токен */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
}
