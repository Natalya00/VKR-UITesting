package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AstRules {

    @JsonProperty("classRules")
    private List<ClassRule> classRules;

    @JsonProperty("methodRules")
    private List<MethodRule> methodRules;

    @JsonProperty("fieldRules")
    private List<FieldRule> fieldRules;

    @JsonProperty("constructorRules")
    private List<ConstructorRule> constructorRules;

    @JsonProperty("interfaceRules")
    private List<InterfaceRule> interfaceRules;

    @JsonProperty("methodBodyRules")
    private List<MethodBodyRule> methodBodyRules;

    @JsonProperty("annotationRules")
    private List<AnnotationRule> annotationRules;
}
