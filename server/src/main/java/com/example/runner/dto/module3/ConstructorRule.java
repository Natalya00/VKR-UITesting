package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Правило проверки конструктора в исходном коде
 *
 * Задаёт сигнатуру, модификаторы и ожидаемые вызовы/операторы
 * внутри тела конструктора. Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class ConstructorRule {

    /** Имя класса, для которого проверяется конструктор */
    @JsonProperty("className")
    private String className;

    /** Список типов параметров конструктора */
    @JsonProperty("parameters")
    private List<String> parameters;

    /** Ожидаемые модификаторы (public, protected и т.д.) */
    @JsonProperty("modifiers")
    private String[] modifiers;

    /** Обязателен ли конструктор в решении */
    @JsonProperty("required")
    private boolean required = true;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;

    /** Вызовы методов, которые должны присутствовать в теле конструктора */
    @JsonProperty("requiredMethodCalls")
    private List<String> requiredMethodCalls;

    /** Вызовы методов, которые запрещены в теле конструктора */
    @JsonProperty("forbiddenMethodCalls")
    private List<String> forbiddenMethodCalls;

    /** Операторы или фрагменты кода, обязательные в теле конструктора */
    @JsonProperty("requiredStatements")
    private List<String> requiredStatements;
}
