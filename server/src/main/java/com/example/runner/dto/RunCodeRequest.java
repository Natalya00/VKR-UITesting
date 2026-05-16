package com.example.runner.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * DTO для запроса на выполнение кода
 * @param code одиночный код для выполнения
 * @param baseUrl базовый URL для тестовых страниц
 * @param exercise номер упражнения (для валидации)
 * @param moduleId идентификатор модуля
 * @param exerciseId идентификатор упражнения
 * @param files мапа файлов (имя -> содержимое) для POM
 */
public record RunCodeRequest(
        @JsonProperty("code")     String code,
        @JsonProperty("baseUrl")  String baseUrl,
        @JsonProperty("exercise") Integer exercise,
        @JsonProperty("moduleId") String moduleId,
        @JsonProperty("exerciseId") String exerciseId,

        @JsonProperty("files") Map<String, String> files
) {}
