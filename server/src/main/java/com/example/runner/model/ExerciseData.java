package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseData {
    private String id;
    private String title;
    private String description;
    private String hint;

    @JsonProperty("trainingComponent")
    private String trainingComponent;

    @JsonProperty("componentConfig")
    private Map<String, Object> componentConfig;

    @JsonProperty("initialCode")
    private String initialCode;
    private Integer order;
    private Difficulty difficulty;

    @JsonProperty("validationRules")
    private ValidationRules validationRules;

    @JsonProperty("expectedSyntax")
    private ExerciseSyntaxRules expectedSyntax;
}
