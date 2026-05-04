package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Module3ValidationRules {

    @JsonProperty("astRules")
    private AstRules astRules;

    @JsonProperty("reflectionRules")
    private ReflectionRules reflectionRules;

    @JsonProperty("harnessRules")
    private HarnessRules harnessRules;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
