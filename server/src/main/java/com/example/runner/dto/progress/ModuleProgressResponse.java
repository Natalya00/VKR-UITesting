package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа с прогрессом по одному модулю
 * 
 * Содержит статистику выполнения упражнений
 * в конкретном модуле и детальный прогресс по каждому упражнению.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProgressResponse {

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
    
    /** Список прогресса по каждому упражнению */
    private List<ExerciseProgressResponse> exercises;
}
