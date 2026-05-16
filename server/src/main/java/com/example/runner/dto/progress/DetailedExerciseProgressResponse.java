package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для детального ответа о прогрессе по упражнению
 * 
 * Расширенная версия ExerciseProgressResponse с дополнительной
 * статистикой по попыткам и полным списком попыток.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedExerciseProgressResponse {

    /** Идентификатор упражнения */
    private String exerciseId;
    
    /** Идентификатор модуля */
    private String moduleId;
    
    /** Флаг завершения упражнения */
    private Boolean isCompleted;
    
    /** Дата и время завершения (может быть null) */
    private LocalDateTime completedAt;
    
    /** Общее количество попыток */
    private Integer attemptsCount;
    
    /** Количество успешных попыток */
    private Integer successfulAttempts;
    
    /** Количество неудачных попыток */
    private Integer failedAttempts;
    
    /** Дата и время первой попытки */
    private LocalDateTime firstAttemptAt;
    
    /** Дата и время последней попытки */
    private LocalDateTime lastAttemptAt;
    
    /** Полный список всех попыток выполнения */
    private List<ExerciseAttemptResponse> attempts;
}