package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Правило проверки объявления класса в исходном коде
 *
 * Задаёт ожидаемое имя, наследование, реализуемые интерфейсы
 * и модификаторы доступа. Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class ClassRule {

    /** Полное или короткое имя проверяемого класса */
    @JsonProperty("className")
    private String className;

    /** Имя родительского класса (extends) */
    @JsonProperty("extendsClass")
    private String extendsClass;

    /** Список реализуемых интерфейсов */
    @JsonProperty("implementsInterfaces")
    private String[] implementsInterfaces;

    /** Ожидаемые модификаторы (public, abstract и т.д.) */
    @JsonProperty("modifiers")
    private String[] modifiers;

    /** Обязателен ли класс в решении студента */
    @JsonProperty("required")
    private boolean required = true;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
