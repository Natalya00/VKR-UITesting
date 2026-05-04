package com.example.runner.dto.module3;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ValidationResult {

    private boolean success;
    private String errorMessage;
    private List<String> errors;
    private List<String> warnings;

    public static ValidationResult success() {
        return ValidationResult.builder()
                .success(true)
                .errors(new ArrayList<>())
                .warnings(new ArrayList<>())
                .build();
    }

    public static ValidationResult failure(String message) {
        return ValidationResult.builder()
                .success(false)
                .errorMessage(message)
                .errors(new ArrayList<>(List.of(message)))
                .warnings(new ArrayList<>())
                .build();
    }

    public void addError(String error) {
        if (this.errors == null) {
            this.errors = new ArrayList<>();
        }
        this.errors.add(error);
        this.success = false;
    }

    public void addWarning(String warning) {
        if (this.warnings == null) {
            this.warnings = new ArrayList<>();
        }
        this.warnings.add(warning);
    }
}
