package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Модель для правил синтаксиса XPath-упражнений
 * Определяет ограничения и требования к XPath-выражениям в конкретных упражнениях модуля 1.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseSyntaxRules {

    /** Обязательные паттерны, которые должны присутствовать в XPath */
    @JsonProperty("requiredPatterns")
    private List<String> requiredPatterns;

    /** Запрещенные паттерны, которые не должны использоваться */
    @JsonProperty("forbiddenPatterns")
    private List<String> forbiddenPatterns;

    /** Разрешенные оси XPath (например, child, descendant, following-sibling) */
    @JsonProperty("allowedAxes")
    private List<String> allowedAxes;

    /** Максимальная сложность XPath-выражения */
    @JsonProperty("maxComplexity")
    private Integer maxComplexity;

    /** Флаг, требующий нахождения всех целевых элементов */
    @JsonProperty("requireAllTargets")
    private Boolean requireAllTargets;

    /** Правила для точного соответствия */
    @JsonProperty("exactOnly")
    private ExactOnlyRules exactOnly;

    /** Требуемые значения атрибутов (атрибут -> значение) */
    @JsonProperty("requireAttrValue")
    private Map<String, String> requireAttrValue;

    /**
     * Вложенный класс для правил точного соответствия
     * Определяет элементы, которые должны использоваться
     * только в точном соответствии с указанными списками.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExactOnlyRules {
        /** Атрибуты, которые должны использоваться только из этого списка */
        @JsonProperty("attributes")
        private List<String> attributes;

        /** Функции, которые должны использоваться только из этого списка */
        @JsonProperty("functions")
        private List<String> functions;

        /** Оси, которые должны использоваться только из этого списка */
        @JsonProperty("axes")
        private List<String> axes;
    }
}
