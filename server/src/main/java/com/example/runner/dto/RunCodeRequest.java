package com.example.runner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record RunCodeRequest(
        @JsonProperty("code")     String code,
        @JsonProperty("baseUrl")  String baseUrl,
        @JsonProperty("exercise") Integer exercise,
        @JsonProperty("moduleId") String moduleId,
        @JsonProperty("exerciseId") String exerciseId,

        @JsonProperty("files") Map<String, String> files
) {}
