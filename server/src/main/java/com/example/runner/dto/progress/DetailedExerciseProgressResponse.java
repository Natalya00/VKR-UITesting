package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedExerciseProgressResponse {

    private String exerciseId;
    private String moduleId;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Integer attemptsCount;
    private Integer successfulAttempts;
    private Integer failedAttempts;
    private LocalDateTime firstAttemptAt;
    private LocalDateTime lastAttemptAt;
    private List<ExerciseAttemptResponse> attempts;
}