package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с информацией о попытке выполнения упражнения
 * 
 * Содержит детальную информацию о конкретной попытке,
 * включая снимок кода, результат и ошибки.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseAttemptResponse {

    /** Уникальный идентификатор попытки */
    private Long id;
    
    /** Идентификатор упражнения */
    private String exerciseId;
    
    /** Идентификатор модуля */
    private String moduleId;
    
    /** Снимок кода на момент попытки */
    private String codeSnapshot;
    
    /** Флаг успешности выполнения */
    private Boolean isSuccess;
    
    /** Сообщение об ошибке (если есть) */
    private String errorMessage;
    
    /** Дата и время создания попытки */
    private LocalDateTime createdAt;
    
    /** Номер упражнения в модуле */
    private Integer exerciseNumber;
}