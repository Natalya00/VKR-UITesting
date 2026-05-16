package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Правило проверки объявления интерфейса в исходном коде
 *
 * Задаёт имя интерфейса, модификаторы и обязательные методы.
 * Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class InterfaceRule {

    /** Имя проверяемого интерфейса */
    @JsonProperty("interfaceName")
    private String interfaceName;

    /** Ожидаемые модификаторы (public и т.д.) */
    @JsonProperty("modifiers")
    private String[] modifiers;

    /** Список сигнатур или имён методов, обязательных в интерфейсе */
    @JsonProperty("requiredMethods")
    private String[] requiredMethods;

    /** Обязателен ли интерфейс в решении */
    @JsonProperty("required")
    private boolean required = true;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
