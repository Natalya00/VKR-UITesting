package com.example.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Главный класс Spring Boot приложения UI Testing Trainer
 * 
 * Это серверная часть тренажера для изучения UI-тестирования, которая предоставляет:
 * - REST API для управления упражнениями и прогрессом
 * - Систему авторизации с JWT токенами
 * - Компиляцию и выполнение Java/Selenide кода
 * - Валидацию Page Object Model структур
 * - Тестовые страницы для отработки навыков
 * 
 * Основные модули:
 * - Модуль 1: XPath тренажер
 * - Модуль 2: Тренажер взаимодействия с элементами (Selenide)
 * - Модуль 3: Page Object Model тренажер
 * 
 * Технологический стек:
 * - Spring Boot 3.x
 * - Spring Security с JWT
 * - PostgreSQL с Liquibase миграциями
 * - Docker для контейнеризации
 * - Selenide для выполнения тестового кода
 */
@SpringBootApplication
public class RunnerApplication {

    /**
     * Точка входа в приложение
     * Запускает Spring Boot контекст и инициализирует все необходимые компоненты
     * 
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        SpringApplication.run(RunnerApplication.class, args);
    }
}


