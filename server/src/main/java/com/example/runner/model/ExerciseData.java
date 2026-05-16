package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Модель данных упражнения
 * Содержит всю информацию об упражнении:
 * описание, код, правила валидации.
 * Загружается из JSON файлов.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseData {
    /** Уникальный идентификатор упражнения */
    private String id;
    
    /** Название упражнения */
    private String title;
    
    /** Описание задания */
    private String description;
    
    /** Подсказка для выполнения */
    private String hint;

    /** Компонент для отображения упражнения */
    @JsonProperty("trainingComponent")
    private String trainingComponent;

    /** Конфигурация компонента */
    @JsonProperty("componentConfig")
    private Map<String, Object> componentConfig;

    /** Начальный код для упражнения */
    @JsonProperty("initialCode")
    private String initialCode;
    
    /** Порядковый номер в блоке */
    private Integer order;
    
    /** Уровень сложности */
    private Difficulty difficulty;

    /** Правила валидации кода */
    @JsonProperty("validationRules")
    private ValidationRules validationRules;

    /** Ожидаемый синтаксис кода */
    @JsonProperty("expectedSyntax")
    private ExerciseSyntaxRules expectedSyntax;
}
