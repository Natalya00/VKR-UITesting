package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldRule {

    @JsonProperty("className")
    private String className;

    @JsonProperty("fieldName")
    private String fieldName;

    @JsonProperty("fieldType")
    private String fieldType;

    @JsonProperty("modifiers")
    private String[] modifiers;

    @JsonProperty("required")
    private boolean required = true;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
