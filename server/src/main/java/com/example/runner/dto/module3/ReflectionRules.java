package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReflectionRules {

    @JsonProperty("requiredClasses")
    private String[] requiredClasses;

    @JsonProperty("requiredInterfaces")
    private String[] requiredInterfaces;

    @JsonProperty("extendsRules")
    private String[] extendsRules;

    @JsonProperty("implementsRules")
    private String[] implementsRules;

    @JsonProperty("publicMethods")
    private String[] publicMethods;

    @JsonProperty("privateMethods")
    private String[] privateMethods;

    @JsonProperty("protectedMethods")
    private String[] protectedMethods;

    @JsonProperty("staticMethods")
    private String[] staticMethods;

    @JsonProperty("privateFields")
    private String[] privateFields;

    @JsonProperty("publicFields")
    private String[] publicFields;

    @JsonProperty("protectedFields")
    private String[] protectedFields;

    @JsonProperty("staticFinalFields")
    private String[] staticFinalFields;

    @JsonProperty("protectedConstructors")
    private String[] protectedConstructors;

    @JsonProperty("privateConstructors")
    private String[] privateConstructors;

    @JsonProperty("abstractClasses")
    private String[] abstractClasses;

    @JsonProperty("finalClasses")
    private String[] finalClasses;

    @JsonProperty("interfaceClasses")
    private String[] interfaceClasses;
}
