package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClassRule {

    @JsonProperty("className")
    private String className;

    @JsonProperty("extendsClass")
    private String extendsClass;

    @JsonProperty("implementsInterfaces")
    private String[] implementsInterfaces;

    @JsonProperty("modifiers")
    private String[] modifiers;

    @JsonProperty("required")
    private boolean required = true;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
