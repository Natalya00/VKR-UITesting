package com.example.runner.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Модель попытки выполнения упражнения
 * Сохраняет детальную информацию о каждой попытке:
 * код, результат, ошибки, время.
 */
@Entity
@Table(name = "exercise_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt {

    /** Уникальный идентификатор попытки */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ссылка на пользователя */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_exercise_attempts_user"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /** Идентификатор модуля */
    @Column(name = "module_id", nullable = false, length = 50)
    private String moduleId;

    /** Идентификатор упражнения */
    @Column(name = "exercise_id", nullable = false, length = 50)
    private String exerciseId;

    /** Снимок кода пользователя на момент выполнения */
    @Column(name = "code_snapshot", nullable = false, columnDefinition = "TEXT")
    private String codeSnapshot;

    /** Флаг успешности выполнения */
    @Column(name = "is_success", nullable = false)
    @Builder.Default
    private Boolean isSuccess = false;

    /** Сообщение об ошибке (если есть) */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /** Количество попыток */
    @Column(name = "attempts_count", nullable = false)
    @Builder.Default
    private Integer attemptsCount = 1;

    /** Время создания попытки */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Извлекает номер упражнения из ID */
    public Integer getExerciseNumber() {
        if (exerciseId != null && exerciseId.matches(".*\\d+.*")) {
            try {
                return Integer.parseInt(exerciseId.replaceAll("\\D+", ""));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /** Проверяет успешность попытки */
    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.isSuccess);
    }
}
