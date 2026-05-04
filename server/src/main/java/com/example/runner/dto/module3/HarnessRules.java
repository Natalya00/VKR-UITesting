package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class HarnessRules {

    @JsonProperty("entryPointClass")
    private String entryPointClass;

    @JsonProperty("entryPointMethod")
    private String entryPointMethod;

    @JsonProperty("scaffoldClasses")
    private String[] scaffoldClasses;

    @JsonProperty("harnessClasses")
    private String[] harnessClasses;

    @JsonProperty("expectedExceptions")
    private String[] expectedExceptions;

    @JsonProperty("compileOnly")
    private boolean compileOnly = false;

    @JsonProperty("timeoutMs")
    private long timeoutMs = 30000;

    @JsonProperty("forbiddenInstantiations")
    private String[] forbiddenInstantiations;

    @JsonProperty("requiredMethodCalls")
    private String[] requiredMethodCalls;

    @JsonProperty("selenide")
    private boolean selenide = false;

    @JsonProperty("processArgs")
    private List<String> processArgs;

    @JsonProperty("jUnitTest")
    private boolean jUnitTest = false;

    @JsonProperty("testClass")
    private String testClass;

    @JsonProperty("testMethods")
    private List<String> testMethods;
}
