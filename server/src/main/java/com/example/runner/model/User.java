package com.example.runner.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Модель пользователя системы
 * Основная сущность для аутентификации и авторизации.
 * Содержит учетные данные и временные метки.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /** Уникальный идентификатор пользователя */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Email адрес пользователя (уникальный) */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /** Хеш пароля (BCrypt) */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /** Время создания записи */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Время последнего обновления */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
