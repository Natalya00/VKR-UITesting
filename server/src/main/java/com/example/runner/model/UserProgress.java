package com.example.runner.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Модель прогресса пользователя по упражнениям
 * Отслеживает состояние выполнения каждого упражнения.
 * Одна запись на комбинацию пользователь-модуль-упражнение.
 */
@Entity
@Table(name = "user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "module_id", "exercise_id"}, name = "uq_user_module_exercise")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProgress {

    /** Уникальный идентификатор записи */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Ссылка на пользователя */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_progress_user"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    /** Идентификатор модуля (module-1, module-2, module-3) */
    @Column(name = "module_id", nullable = false, length = 50)
    private String moduleId;

    /** Идентификатор упражнения */
    @Column(name = "exercise_id", nullable = false, length = 50)
    private String exerciseId;

    /** Флаг завершенности упражнения */
    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean isCompleted = false;

    /** Время завершения упражнения */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /** Общее количество попыток выполнения */
    @Column(name = "attempts_count", nullable = false)
    @Builder.Default
    private Integer attemptsCount = 0;

    /** Время создания записи */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Время последнего обновления */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /** Увеличивает счетчик попыток на 1 */
    public void incrementAttempts() {
        this.attemptsCount = this.attemptsCount + 1;
    }

    /** Помечает упражнение как завершенное с текущим временем */
    public void markCompleted() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}
