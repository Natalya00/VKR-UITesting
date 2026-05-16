package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Корневой DTO правил валидации POM-кода модуля 3
 *
 * Объединяет наборы правил для проверки исходного кода:
 * синтаксический анализ (AST), рефлексия и запуск тестового harness.
 * Десериализуется из JSON-конфигурации упражнения.
 */
@Data
@NoArgsConstructor
public class Module3ValidationRules {

    /** Правила проверки структуры кода через AST */
    @JsonProperty("astRules")
    private AstRules astRules;

    /** Правила проверки скомпилированных классов через рефлексию */
    @JsonProperty("reflectionRules")
    private ReflectionRules reflectionRules;

    /** Правила компиляции и запуска тестового harness */
    @JsonProperty("harnessRules")
    private HarnessRules harnessRules;

    /** Общее сообщение об ошибке при нарушении правил верхнего уровня */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
