package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Правила компиляции и запуска тестового harness для POM-кода
 *
 * Определяет точку входа, набор scaffold-классов, параметры JVM,
 * режим только компиляции или полного прогона JUnit/Selenide-тестов.
 */
@Data
@NoArgsConstructor
public class HarnessRules {

    /** Класс с методом main или точкой входа для запуска */
    @JsonProperty("entryPointClass")
    private String entryPointClass;

    /** Имя метода точки входа (main или иной) */
    @JsonProperty("entryPointMethod")
    private String entryPointMethod;

    /** Классы из scaffold, подключаемые к компиляции */
    @JsonProperty("scaffoldClasses")
    private String[] scaffoldClasses;

    /** Дополнительные классы harness, участвующие в прогоне */
    @JsonProperty("harnessClasses")
    private String[] harnessClasses;

    /** Исключения, которые допустимы при выполнении */
    @JsonProperty("expectedExceptions")
    private String[] expectedExceptions;

    /** Только компиляция без запуска кода */
    @JsonProperty("compileOnly")
    private boolean compileOnly = false;

    /** Таймаут выполнения в миллисекундах */
    @JsonProperty("timeoutMs")
    private long timeoutMs = 30000;

    /** Классы, экземпляры которых запрещено создавать в решении */
    @JsonProperty("forbiddenInstantiations")
    private String[] forbiddenInstantiations;

    /** Вызовы методов, обязательные при выполнении harness */
    @JsonProperty("requiredMethodCalls")
    private String[] requiredMethodCalls;

    /** Включить зависимости и настройку Selenide при запуске */
    @JsonProperty("selenide")
    private boolean selenide = false;

    /** Аргументы командной строки для запускаемого процесса */
    @JsonProperty("processArgs")
    private List<String> processArgs;

    /** Запускать как JUnit-тест вместо обычного main */
    @JsonProperty("jUnitTest")
    private boolean jUnitTest = false;

    /** Полное имя JUnit-тестового класса */
    @JsonProperty("testClass")
    private String testClass;

    /** Список имён тестовых методов для выборочного запуска */
    @JsonProperty("testMethods")
    private List<String> testMethods;
}
