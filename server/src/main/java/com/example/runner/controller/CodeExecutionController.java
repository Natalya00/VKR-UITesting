package com.example.runner.controller;

import com.example.runner.dto.RunCodeRequest;
import com.example.runner.dto.RunCodeResponse;
import com.example.runner.security.UserDetailsImpl;
import com.example.runner.service.CodeExecutionService;
import com.example.runner.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST контроллер для выполнения пользовательского кода
 * 
 * Предоставляет API для:
 * - Компиляции и выполнения Java/Selenide кода пользователя
 * - Запуска тестов в изолированной среде
 * - Получения результатов выполнения с детальной информацией об ошибках
 * - Валидации кода для модулей 2 и 3 (Selenide и POM)
 */
@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CodeExecutionController {

    /** Сервис для выполнения пользовательского кода */
    private final CodeExecutionService codeExecutionService;
    
    /** Сервис для отслеживания прогресса пользователя */
    private final ProgressService progressService;

    /**
     * Конструктор контроллера
     * @param codeExecutionService сервис для выполнения кода
     * @param progressService сервис для работы с прогрессом
     */
    public CodeExecutionController(CodeExecutionService codeExecutionService,
                                   ProgressService progressService) {
        this.codeExecutionService = codeExecutionService;
        this.progressService = progressService;
    }

    /**
     * Выполняет компиляцию и запуск пользовательского кода
     * @param request запрос с кодом для выполнения и метаданными
     * @param userDetails данные аутентифицированного пользователя
     * @return результат выполнения с информацией о компиляции, тестах и ошибках
     */
    @PostMapping("/run")
    public ResponseEntity<RunCodeResponse> runCode(
            @RequestBody RunCodeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        RunCodeResponse response = codeExecutionService.runCode(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Определяет идентификатор модуля по номеру упражнения
     * @param exerciseNum номер упражнения
     * @return идентификатор модуля
     */
    private String determineModuleId(int exerciseNum) {
        if (exerciseNum >= 101 && exerciseNum <= 157) {
            return "module-3";
        } else if (exerciseNum >= 50 && exerciseNum <= 139) {
            return "module-2";
        } else if (exerciseNum >= 1 && exerciseNum <= 49) {
            return "module-1";
        }
        return "module-1";
    }
}
