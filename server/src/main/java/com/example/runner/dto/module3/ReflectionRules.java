package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Набор правил проверки скомпилированного кода через рефлексию
 *
 * Описывает ожидаемую структуру классов после компиляции:
 * наличие типов, иерархию наследования, методы и поля с заданной видимостью.
 */
@Data
@NoArgsConstructor
public class ReflectionRules {

    /** Классы, которые должны присутствовать в скомпилированном решении */
    @JsonProperty("requiredClasses")
    private String[] requiredClasses;

    /** Интерфейсы, которые должны присутствовать в решении */
    @JsonProperty("requiredInterfaces")
    private String[] requiredInterfaces;

    /** Правила наследования в формате «потомок:родитель» */
    @JsonProperty("extendsRules")
    private String[] extendsRules;

    /** Правила реализации интерфейсов в формате «класс:интерфейс» */
    @JsonProperty("implementsRules")
    private String[] implementsRules;

    /** Ожидаемые public-методы */
    @JsonProperty("publicMethods")
    private String[] publicMethods;

    /** Ожидаемые private-методы */
    @JsonProperty("privateMethods")
    private String[] privateMethods;

    /** Ожидаемые protected-методы */
    @JsonProperty("protectedMethods")
    private String[] protectedMethods;

    /** Ожидаемые static-методы */
    @JsonProperty("staticMethods")
    private String[] staticMethods;

    /** Ожидаемые private-поля */
    @JsonProperty("privateFields")
    private String[] privateFields;

    /** Ожидаемые public-поля */
    @JsonProperty("publicFields")
    private String[] publicFields;

    /** Ожидаемые protected-поля */
    @JsonProperty("protectedFields")
    private String[] protectedFields;

    /** Ожидаемые static final-поля */
    @JsonProperty("staticFinalFields")
    private String[] staticFinalFields;

    /** Ожидаемые protected-конструкторы */
    @JsonProperty("protectedConstructors")
    private String[] protectedConstructors;

    /** Ожидаемые private-конструкторы */
    @JsonProperty("privateConstructors")
    private String[] privateConstructors;

    /** Классы, которые должны быть объявлены abstract */
    @JsonProperty("abstractClasses")
    private String[] abstractClasses;

    /** Классы, которые должны быть объявлены final */
    @JsonProperty("finalClasses")
    private String[] finalClasses;

    /** Типы, которые должны быть интерфейсами */
    @JsonProperty("interfaceClasses")
    private String[] interfaceClasses;
}
