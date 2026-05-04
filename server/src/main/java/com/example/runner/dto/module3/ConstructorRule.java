package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ConstructorRule {

    @JsonProperty("className")
    private String className;

    @JsonProperty("parameters")
    private List<String> parameters;

    @JsonProperty("modifiers")
    private String[] modifiers;

    @JsonProperty("required")
    private boolean required = true;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("requiredMethodCalls")
    private List<String> requiredMethodCalls;

    @JsonProperty("forbiddenMethodCalls")
    private List<String> forbiddenMethodCalls;

    @JsonProperty("requiredStatements")
    private List<String> requiredStatements;
}
