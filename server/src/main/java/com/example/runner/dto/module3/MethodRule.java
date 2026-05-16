package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Правило проверки объявления метода в исходном коде
 *
 * Задаёт сигнатуру, модификаторы и аннотации метода
 * в указанном классе. Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class MethodRule {

    /** Имя класса, в котором должен быть объявлен метод */
    @JsonProperty("className")
    private String className;

    /** Имя проверяемого метода */
    @JsonProperty("methodName")
    private String methodName;

    /** Ожидаемый тип возвращаемого значения */
    @JsonProperty("returnType")
    private String returnType;

    /** Список типов параметров метода в порядке объявления */
    @JsonProperty("parameters")
    private List<String> parameters;

    /** Ожидаемые модификаторы (public, static и т.д.) */
    @JsonProperty("modifiers")
    private String[] modifiers;

    /** Обязателен ли метод в решении студента */
    @JsonProperty("required")
    private boolean required = true;

    /** Ожидаемые аннотации метода */
    @JsonProperty("annotations")
    private String[] annotations;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
