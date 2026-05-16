package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа с детальной статистикой попыток по модулю
 * 
 * Расширенная версия ModuleProgressResponse с дополнительной
 * статистикой по попыткам и детальным прогрессом по упражнениям.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAttemptsResponse {

    /** Идентификатор модуля */
    private String moduleId;
    
    /** Название модуля */
    private String moduleTitle;
    
    /** Общее количество упражнений в модуле */
    private Integer totalExercises;
    
    /** Количество выполненных упражнений */
    private Integer completedExercises;
    
    /** Процент выполнения модуля (0.0 - 100.0) */
    private Double percentage;
    
    /** Общее количество попыток во всех упражнениях */
    private Integer totalAttempts;
    
    /** Количество успешных попыток */
    private Integer successfulAttempts;
    
    /** Количество неудачных попыток */
    private Integer failedAttempts;
    
    /** Список детального прогресса по каждому упражнению */
    private List<DetailedExerciseProgressResponse> exercises;
}