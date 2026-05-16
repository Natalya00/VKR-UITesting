package com.example.runner.dto;

/**
 * DTO для ответа на запрос выполнения кода
 * @param success общий результат выполнения
 * @param uiMode флаг UI-теста (Selenide/Selenium)
 * @param stdout стандартный вывод программы
 * @param stderr вывод ошибок и валидации
 * @param message человекочитаемое сообщение о результате
 */
public record RunCodeResponse(boolean success,
                              boolean uiMode,
                              String stdout,
                              String stderr,
                              String message) {
}


