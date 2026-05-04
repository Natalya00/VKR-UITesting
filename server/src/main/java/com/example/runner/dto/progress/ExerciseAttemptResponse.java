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
public class ExerciseAttemptResponse {

    private Long id;
    private String exerciseId;
    private String moduleId;
    private String codeSnapshot;
    private Boolean isSuccess;
    private String errorMessage;
    private LocalDateTime createdAt;
    private Integer exerciseNumber;
}