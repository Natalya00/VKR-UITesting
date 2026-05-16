package com.example.runner.service.module3.reflection;

import java.util.ArrayList;
import java.util.List;

/**
 * Результат рефлексивной валидации скомпилированного кода
 */
public class ReflectionValidationResult {
    
    /** Признак успешной проверки */
    private boolean success;
    /** Список сообщений об ошибках */
    private List<String> errors;
    /** Список предупреждений */
    private List<String> warnings;
    
    /** Создаёт пустой успешный результат */
    public ReflectionValidationResult() {
        this.success = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    /** @return true, если ошибок нет */
    public boolean isSuccess() {
        return success;
    }
    
    /**
     * @param success новое значение признака успеха
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    /** @return список ошибок */
    public List<String> getErrors() {
        return errors;
    }
    
    /** @return список предупреждений */
    public List<String> getWarnings() {
        return warnings;
    }
    
    /**
     * Добавляет ошибку и помечает результат как неуспешный
     * @param error текст ошибки
     */
    public void addError(String error) {
        this.errors.add(error);
        this.success = false;
    }
    
    /**
     * Добавляет предупреждение без изменения признака успеха
     * @param warning текст предупреждения
     */
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    /**
     * Объединяет другой результат с текущим
     * @param other результат для слияния
     */
    public void merge(ReflectionValidationResult other) {
        if (!other.success) {
            this.success = false;
        }
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
    }
    
    /** @return новый успешный результат без ошибок */
    public static ReflectionValidationResult success() {
        return new ReflectionValidationResult();
    }
    
    /**
     * @param error сообщение об ошибке
     * @return результат с одной ошибкой
     */
    public static ReflectionValidationResult failure(String error) {
        ReflectionValidationResult result = new ReflectionValidationResult();
        result.addError(error);
        return result;
    }
}
