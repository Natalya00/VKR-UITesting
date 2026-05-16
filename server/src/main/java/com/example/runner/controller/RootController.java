package com.example.runner.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Корневой контроллер приложения
 * Обрабатывает запросы к корневому URL сервера
 * и перенаправляет пользователей на фронтенд-приложение.
 */
@RestController
public class RootController {

    /**
     * Перенаправляет запросы к корневому URL на фронтенд
     * 
     * Обрабатывает GET-запросы к "/" и возвращает HTTP 302 (Found)
     * с заголовком Location, указывающим на фронтенд-приложение.
     * Это позволяет пользователям попасть на главную страницу тренажера,
     * даже если они обращаются к серверному порту.
     * 
     * @return ResponseEntity с кодом 302 и заголовком Location
     */
    @GetMapping("/")
    public ResponseEntity<Void> redirectToFrontend() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "http://localhost:5173")
                .build();
    }
}
