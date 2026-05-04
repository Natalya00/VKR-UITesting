package com.example.runner.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test-harness")
public class TestHarnessController {

    private static final String FRONTEND_BASE_URL = "http://localhost:5173";

    @GetMapping("/module3/{page}")
    public ResponseEntity<Void> redirectToTestPage(@PathVariable String page) {
        String redirectUrl = FRONTEND_BASE_URL + "/test-harness/module3/" + page;
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @GetMapping("/module3/block1/{page}")
    public ResponseEntity<Void> redirectToBlock1Page(@PathVariable String page) {
        String redirectUrl = FRONTEND_BASE_URL + "/test-harness/module3/block1/" + page;
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }

    @GetMapping("/**")
    public ResponseEntity<Void> redirectToFrontendTestHarness() {
        String redirectUrl = FRONTEND_BASE_URL + "/test-harness/module3/elements";
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, redirectUrl)
                .build();
    }
}