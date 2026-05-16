package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Модель правил валидации для упражнений модуля 2
 * Определяет требования к Java-коду для взаимодействия
 * с элементами страницы, включая обязательные и запрещенные
 * методы, селекторы и условия.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ValidationRules {

    /** Обязательные методы, которые должны присутствовать в коде */
    @JsonProperty("requiredMethods")
    private List<String> requiredMethods;

    /** Альтернативные группы методов (достаточно одного из группы) */
    @JsonProperty("alternativeMethods")
    private List<List<String>> alternativeMethods;

    /** Запрещенные методы, которые не должны использоваться */
    @JsonProperty("forbiddenMethods")
    private List<String> forbiddenMethods;

    /** Обязательные селекторы элементов */
    @JsonProperty("requiredSelectors")
    private List<String> requiredSelectors;

    /** Альтернативные группы селекторов */
    @JsonProperty("alternativeSelectors")
    private List<List<String>> alternativeSelectors;

    /** Запрещенные селекторы */
    @JsonProperty("forbiddenSelectors")
    private List<String> forbiddenSelectors;

    /** Обязательные условия в коде */
    @JsonProperty("requiredConditions")
    private List<String> requiredConditions;

    /** Альтернативные группы условий */
    @JsonProperty("alternativeConditions")
    private List<List<String>> alternativeConditions;

    /** Регулярные выражения для проверки кода (имя -> паттерн) */
    @JsonProperty("regexPatterns")
    private Map<String, String> regexPatterns;

    /** Сообщение об ошибке при несоответствии правилам */
    @JsonProperty("errorMessage")
    private String errorMessage;

    /** Максимальная длина кода */
    @JsonProperty("maxCodeLength")
    private Integer maxCodeLength;

    /** Минимальная длина кода */
    @JsonProperty("minCodeLength")
    private Integer minCodeLength;

    /** Обязательные импорты в коде */
    @JsonProperty("requiredImports")
    private List<String> requiredImports;

    /** Ожидаемое действие (например, click, sendKeys) */
    @JsonProperty("expectedAction")
    private String expectedAction;

    /** Ожидаемый тип элемента (например, button, input) */
    @JsonProperty("expectedElementType")
    private String expectedElementType;

    /** Обязательный метод поиска элемента */
    @JsonProperty("requiredFinder")
    private String requiredFinder;
}
