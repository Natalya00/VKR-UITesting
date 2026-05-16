package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с прогрессом по одному упражнению
 * 
 * Содержит информацию о статусе выполнения
 * конкретного упражнения и количестве попыток.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseProgressResponse {

    /** Идентификатор упражнения */
    private String exerciseId;
    
    /** Идентификатор модуля */
    private String moduleId;
    
    /** Флаг завершения упражнения */
    private Boolean isCompleted;
    
    /** Дата и время завершения (может быть null) */
    private LocalDateTime completedAt;
    
    /** Количество попыток выполнения */
    private Integer attemptsCount;
}
