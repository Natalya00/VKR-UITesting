package com.example.runner.dto.module3;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO для результата валидации POM-кода
 * 
 * Содержит информацию о статусе валидации,
 * ошибках и предупреждениях. Предоставляет удобные
 * статические методы для создания результатов.
 */
@Data
@Builder
public class ValidationResult {

    /** Флаг успешности валидации */
    private boolean success;
    
    /** Основное сообщение об ошибке */
    private String errorMessage;
    
    /** Список всех ошибок валидации */
    private List<String> errors;
    
    /** Список предупреждений */
    private List<String> warnings;

    /**
     * Создает успешный результат валидации
     * @return результат с success = true и пустыми списками ошибок
     */
    public static ValidationResult success() {
        return ValidationResult.builder()
                .success(true)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }

    /**
     * Создает неуспешный результат валидации с ошибкой
     * @param message сообщение об ошибке
     * @return результат с success = false и указанной ошибкой
     */
    public static ValidationResult failure(String message) {
        return ValidationResult.builder()
                .success(false)
                .errorMessage(message)
                .errors(new ArrayList<>(List.of(message)))
                .warnings(new ArrayList<>())
                .build();
    }

    /**
     * Добавляет ошибку в список и помечает валидацию как неуспешную
     * @param error текст ошибки
     */
    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.success = false;
    }

    /**
     * Добавляет предупреждение в список
     * @param warning текст предупреждения
     */
    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }
}
