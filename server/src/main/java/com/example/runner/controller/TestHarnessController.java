package com.example.runner.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для перенаправления на тестовые страницы
 * 
 * Предоставляет эндпоинты для:
 * - Перенаправления на тестовые страницы модуля 3 (POM)
 * - Обеспечения совместимости с системой тестирования Selenide
 * - Маршрутизации запросов к тестовым страницам на фронтенд
 */
@RestController
@RequestMapping("/test-harness")
public class TestHarnessController {

    /** Базовый URL фронтенда для перенаправления */
    private static final String FRONTEND_BASE_URL = "http://localhost:5173";

    /**
     * Перенаправляет на тестовую страницу модуля 3
     * @param page название страницы (например, "elements", "forms", "tables")
     * @return HTTP 302 перенаправление на соответствующую страницу фронтенда
     */
    @GetMapping("/module3/{page}")
    public ResponseEntity<Void> redirectToTestPage(@PathVariable String page) {
        String redirectUrl = FRONTEND_BASE_URL + "/test-harness/module3/" + page;
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    /**
     * Перенаправляет на тестовую страницу блока 1 модуля 3
     * @param page название страницы в блоке 1
     * @return HTTP 302 перенаправление на соответствующую страницу фронтенда
     */
    @GetMapping("/module3/block1/{page}")
    public ResponseEntity<Void> redirectToBlock1Page(@PathVariable String page) {
        String redirectUrl = FRONTEND_BASE_URL + "/test-harness/module3/block1/" + page;
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    /**
     * Обрабатывает все остальные запросы к test-harness и перенаправляет на страницу по умолчанию
     * Используется как fallback для любых неопределенных маршрутов
     * в пределах /test-harness/**
     * @return HTTP 302 перенаправление на страницу элементов модуля 3
     */
    @GetMapping("/**")
    public ResponseEntity<Void> redirectToFrontendTestHarness() {
        String redirectUrl = FRONTEND_BASE_URL + "/test-harness/module3/elements";
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }
}