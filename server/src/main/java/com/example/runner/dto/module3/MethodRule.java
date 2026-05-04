package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MethodRule {

    @JsonProperty("className")
    private String className;

    @JsonProperty("methodName")
    private String methodName;

    @JsonProperty("returnType")
    private String returnType;

    @JsonProperty("parameters")
    private List<String> parameters;

    @JsonProperty("modifiers")
    private String[] modifiers;

    @JsonProperty("required")
    private boolean required = true;

    @JsonProperty("annotations")
    private String[] annotations;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
