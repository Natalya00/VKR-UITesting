package com.example.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс, который предоставляет:
 * - REST API для управления упражнениями и прогрессом
 * - Систему авторизации с JWT токенами
 * - Компиляцию и выполнение Java/Selenide кода
 * - Валидацию Page Object Model структур
 * - Тестовые страницы для отработки навыков
 */
@SpringBootApplication
public class RunnerApplication {

    /**
     * Точка входа в приложение
     * Запускает Spring Boot контекст и инициализирует все необходимые компоненты
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RunnerApplication.class, args);
    }
}


