package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для ответа с общим прогрессом пользователя
 * 
 * Содержит сводную статистику по всем модулям
 * и детальный прогресс по каждому модулю.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProgressResponse {

    /** Идентификатор пользователя */
    private Long userId;
    
    /** Общее количество выполненных упражнений */
    private Integer totalCompleted;
    
    /** Общее количество упражнений во всех модулях */
    private Integer totalExercises;
    
    /** Общий процент выполнения (0.0 - 100.0) */
    private Double totalPercentage;
    
    /** Список прогресса по каждому модулю */
    private List<ModuleProgressResponse> modules;
}
