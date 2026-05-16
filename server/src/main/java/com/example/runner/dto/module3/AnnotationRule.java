package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Правило проверки аннотаций в исходном коде
 *
 * Задаёт обязательные и запрещённые аннотации для класса
 * или метода. Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class AnnotationRule {

    /** Тип цели проверки: CLASS, METHOD и т.д. */
    @JsonProperty("targetType")
    private String targetType;

    /** Имя класса, к которому применяется правило */
    @JsonProperty("className")
    private String className;

    /** Имя метода (если правило относится к методу) */
    @JsonProperty("methodName")
    private String methodName;

    /** Список аннотаций, которые должны присутствовать */
    @JsonProperty("requiredAnnotations")
    private List<String> requiredAnnotations;

    /** Список аннотаций, которые запрещены */
    @JsonProperty("forbiddenAnnotations")
    private List<String> forbiddenAnnotations;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
