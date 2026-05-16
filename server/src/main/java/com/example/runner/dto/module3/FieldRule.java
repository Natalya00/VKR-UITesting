package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Правило проверки объявления поля в исходном коде
 *
 * Задаёт имя, тип и модификаторы поля в указанном классе.
 * Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class FieldRule {

    /** Имя класса, в котором должно быть объявлено поле */
    @JsonProperty("className")
    private String className;

    /** Имя проверяемого поля */
    @JsonProperty("fieldName")
    private String fieldName;

    /** Ожидаемый тип поля */
    @JsonProperty("fieldType")
    private String fieldType;

    /** Ожидаемые модификаторы (private, static и т.д.) */
    @JsonProperty("modifiers")
    private String[] modifiers;

    /** Обязательно ли поле в решении студента */
    @JsonProperty("required")
    private boolean required = true;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
