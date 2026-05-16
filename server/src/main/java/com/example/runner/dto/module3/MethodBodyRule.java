package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Правило проверки тела метода в исходном коде
 *
 * Задаёт обязательные и запрещённые вызовы, операторы,
 * возврат {@code this} и вызов {@code super}. Используется при AST-валидации.
 */
@Data
@NoArgsConstructor
public class MethodBodyRule {

    /** Имя класса, содержащего проверяемый метод */
    @JsonProperty("className")
    private String className;

    /** Имя проверяемого метода */
    @JsonProperty("methodName")
    private String methodName;

    /** Вызовы методов, которые должны присутствовать в теле */
    @JsonProperty("requiredMethodCalls")
    private List<String> requiredMethodCalls;

    /** Вызовы методов, которые запрещены в теле */
    @JsonProperty("forbiddenMethodCalls")
    private List<String> forbiddenMethodCalls;

    /** Должен ли метод возвращать {@code this} (fluent interface) */
    @JsonProperty("mustReturnThis")
    private Boolean mustReturnThis;

    /** Операторы или фрагменты кода, обязательные в теле метода */
    @JsonProperty("requiredStatements")
    private List<String> requiredStatements;

    /** Имя вызываемого конструктора/метода суперкласса (super) */
    @JsonProperty("mustCallSuper")
    private String mustCallSuper;

    /** Сообщение об ошибке при нарушении правила */
    @JsonProperty("errorMessage")
    private String errorMessage;
}
