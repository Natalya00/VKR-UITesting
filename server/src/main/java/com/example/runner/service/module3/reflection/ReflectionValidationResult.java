package com.example.runner.service.module3.reflection;

import java.util.ArrayList;
import java.util.List;

public class ReflectionValidationResult {
    
    private boolean success;
    private List<String> errors;
    private List<String> warnings;
    
    public ReflectionValidationResult() {
        this.success = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public List<String> getWarnings() {
        return warnings;
    }
    
    public void addError(String error) {
        this.errors.add(error);
        this.success = false;
    }
    
    public void addWarning(String warning) {
        this.warnings.add(warning);
    }
    
    public void merge(ReflectionValidationResult other) {
        if (!other.success) {
            this.success = false;
        }
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
    }
    
    public static ReflectionValidationResult success() {
        return new ReflectionValidationResult();
    }
    
    public static ReflectionValidationResult failure(String error) {
        ReflectionValidationResult result = new ReflectionValidationResult();
        result.addError(error);
        return result;
    }
}
