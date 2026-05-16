package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Набор AST-правил для статической проверки исходного Java-кода
 *
 * Содержит списки правил для классов, методов, полей, конструкторов,
 * интерфейсов, тел методов и аннотаций.
 */
@Data
@NoArgsConstructor
public class AstRules {

    /** Правила объявления классов */
    @JsonProperty("classRules")
    private List<ClassRule> classRules;

    /** Правила объявления методов */
    @JsonProperty("methodRules")
    private List<MethodRule> methodRules;

    /** Правила объявления полей */
    @JsonProperty("fieldRules")
    private List<FieldRule> fieldRules;

    /** Правила объявления конструкторов */
    @JsonProperty("constructorRules")
    private List<ConstructorRule> constructorRules;

    /** Правила объявления интерфейсов */
    @JsonProperty("interfaceRules")
    private List<InterfaceRule> interfaceRules;

    /** Правила содержимого тел методов */
    @JsonProperty("methodBodyRules")
    private List<MethodBodyRule> methodBodyRules;

    /** Правила наличия и отсутствия аннотаций */
    @JsonProperty("annotationRules")
    private List<AnnotationRule> annotationRules;
}
