package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class MethodBodyRule {

    @JsonProperty("className")
    private String className;

    @JsonProperty("methodName")
    private String methodName;

    @JsonProperty("requiredMethodCalls")
    private List<String> requiredMethodCalls;

    @JsonProperty("forbiddenMethodCalls")
    private List<String> forbiddenMethodCalls;

    @JsonProperty("mustReturnThis")
    private Boolean mustReturnThis;

    @JsonProperty("requiredStatements")
    private List<String> requiredStatements;

    @JsonProperty("mustCallSuper")
    private String mustCallSuper;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
