package com.example.runner.service.module3.ast;

import java.util.ArrayList;
import java.util.List;

public class AstValidationResult {
    
    private boolean success;
    private List<String> errors;
    private List<String> warnings;
    
    public AstValidationResult() {
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
    
    public void merge(AstValidationResult other) {
        if (!other.success) {
            this.success = false;
        }
        this.errors.addAll(other.errors);
        this.warnings.addAll(other.warnings);
    }
    
    public static AstValidationResult success() {
        return new AstValidationResult();
    }
    
    public static AstValidationResult failure(String error) {
        AstValidationResult result = new AstValidationResult();
        result.addError(error);
        return result;
    }
}
