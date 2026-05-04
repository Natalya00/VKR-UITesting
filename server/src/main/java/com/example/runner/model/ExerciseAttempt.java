package com.example.runner.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "exercise_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_exercise_attempts_user"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "module_id", nullable = false, length = 50)
    private String moduleId;

    @Column(name = "exercise_id", nullable = false, length = 50)
    private String exerciseId;

    @Column(name = "code_snapshot", nullable = false, columnDefinition = "TEXT")
    private String codeSnapshot;

    @Column(name = "is_success", nullable = false)
    @Builder.Default
    private Boolean isSuccess = false;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "attempts_count", nullable = false)
    @Builder.Default
    private Integer attemptsCount = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public boolean isSuccess() {
        return Boolean.TRUE.equals(this.isSuccess);
    }
}
