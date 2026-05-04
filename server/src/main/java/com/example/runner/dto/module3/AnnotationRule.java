package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AnnotationRule {

    @JsonProperty("targetType")
    private String targetType;

    @JsonProperty("className")
    private String className;

    @JsonProperty("methodName")
    private String methodName;

    @JsonProperty("requiredAnnotations")
    private List<String> requiredAnnotations;

    @JsonProperty("forbiddenAnnotations")
    private List<String> forbiddenAnnotations;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
