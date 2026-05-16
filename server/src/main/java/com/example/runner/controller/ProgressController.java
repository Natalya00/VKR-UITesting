package com.example.runner.controller;

import com.example.runner.dto.progress.ExerciseProgressResponse;
import com.example.runner.dto.progress.ModuleProgressResponse;
import com.example.runner.dto.progress.ModuleAttemptsResponse;
import com.example.runner.dto.progress.UserProgressResponse;
import com.example.runner.security.UserDetailsImpl;
import com.example.runner.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для управления прогрессом пользователей
 * 
 * Предоставляет API для:
 * - Отметки выполненных упражнений с сохранением кода и результатов
 * - Получения общего прогресса пользователя по всем модулям
 * - Получения детального прогресса по конкретному модулю
 * - Получения истории попыток выполнения упражнений
 * - Отслеживания статистики успешных и неуспешных решений
 */
@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProgressController {

    /** Сервис для работы с прогрессом пользователей */
    private final ProgressService progressService;

    /**
     * Отмечает упражнение как выполненное и сохраняет результат попытки
     * @param moduleId идентификатор модуля
     * @param exerciseId идентификатор упражнения
     * @param request данные о выполнении (код, успешность, ошибки)
     * @param userDetails данные аутентифицированного пользователя
     * @return информация о прогрессе по упражнению
     */
@PostMapping("/{moduleId}/{exerciseId}/complete")
    public ResponseEntity<ExerciseProgressResponse> markExerciseComplete(
            @PathVariable String moduleId,
            @PathVariable String exerciseId,
            @RequestBody CompleteExerciseRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        ExerciseProgressResponse response = progressService.markExerciseComplete(
                userDetails.getId(),
                moduleId,
                exerciseId,
                request.codeSnapshot(),
                request.isSuccess(),
                request.errorMessage()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Получает общий прогресс пользователя по всем модулям
     * @param userDetails данные аутентифицированного пользователя
     * @return сводная информация о прогрессе по всем модулям
     */
    @GetMapping
    public ResponseEntity<UserProgressResponse> getUserProgress(
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        UserProgressResponse response = progressService.getUserProgress(userDetails.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * Получает детальный прогресс пользователя по конкретному модулю
     * @param moduleId идентификатор модуля
     * @param userDetails данные аутентифицированного пользователя
     * @return детальная информация о прогрессе по модулю
     */
    @GetMapping("/module/{moduleId}")
    public ResponseEntity<ModuleProgressResponse> getModuleProgress(
            @PathVariable String moduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        ModuleProgressResponse response = progressService.getModuleProgress(userDetails.getId(), moduleId);
        return ResponseEntity.ok(response);
    }

    /**
     * Получает историю всех попыток выполнения упражнений модуля
     * @param moduleId идентификатор модуля
     * @param userDetails данные аутентифицированного пользователя
     * @return список всех попыток с кодом, результатами и временными метками
     */
    @GetMapping("/module/{moduleId}/attempts")
    public ResponseEntity<ModuleAttemptsResponse> getModuleAttempts(
            @PathVariable String moduleId,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        ModuleAttemptsResponse response = progressService.getModuleAttempts(userDetails.getId(), moduleId);
        return ResponseEntity.ok(response);
    }

    /**
     * DTO для запроса завершения упражнения
     * @param codeSnapshot снимок кода пользователя на момент выполнения
     * @param isSuccess флаг успешного выполнения упражнения
     * @param errorMessage сообщение об ошибке (если есть)
     */
    public record CompleteExerciseRequest(
            String codeSnapshot,
            Boolean isSuccess,
            String errorMessage
    ) {}
}
