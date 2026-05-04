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
public class ExerciseSyntaxRules {

    @JsonProperty("requiredPatterns")
    private List<String> requiredPatterns;

    @JsonProperty("forbiddenPatterns")
    private List<String> forbiddenPatterns;

    @JsonProperty("allowedAxes")
    private List<String> allowedAxes;

    @JsonProperty("maxComplexity")
    private Integer maxComplexity;

    @JsonProperty("requireAllTargets")
    private Boolean requireAllTargets;

    @JsonProperty("exactOnly")
    private ExactOnlyRules exactOnly;

    @JsonProperty("requireAttrValue")
    private Map<String, String> requireAttrValue;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExactOnlyRules {
        @JsonProperty("attributes")
        private List<String> attributes;

        @JsonProperty("functions")
        private List<String> functions;

        @JsonProperty("axes")
        private List<String> axes;
    }
}
