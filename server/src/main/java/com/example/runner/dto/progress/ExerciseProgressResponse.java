package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseProgressResponse {

    private String exerciseId;
    private String moduleId;
    private Boolean isCompleted;
    private LocalDateTime completedAt;
    private Integer attemptsCount;
}
