package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRules {

    @JsonProperty("requiredMethods")
    private List<String> requiredMethods;

    @JsonProperty("alternativeMethods")
    private List<List<String>> alternativeMethods;

    @JsonProperty("forbiddenMethods")
    private List<String> forbiddenMethods;

    @JsonProperty("requiredSelectors")
    private List<String> requiredSelectors;

    @JsonProperty("alternativeSelectors")
    private List<List<String>> alternativeSelectors;

    @JsonProperty("forbiddenSelectors")
    private List<String> forbiddenSelectors;

    @JsonProperty("requiredConditions")
    private List<String> requiredConditions;

    @JsonProperty("alternativeConditions")
    private List<List<String>> alternativeConditions;

    @JsonProperty("regexPatterns")
    private Map<String, String> regexPatterns;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("maxCodeLength")
    private Integer maxCodeLength;

    @JsonProperty("minCodeLength")
    private Integer minCodeLength;

    @JsonProperty("requiredImports")
    private List<String> requiredImports;

    @JsonProperty("expectedAction")
    private String expectedAction;

    @JsonProperty("expectedElementType")
    private String expectedElementType;

    @JsonProperty("requiredFinder")
    private String requiredFinder;
}
