package com.example.runner.controller;

import com.example.runner.model.BlockData;
import com.example.runner.model.Difficulty;
import com.example.runner.model.ExerciseData;
import com.example.runner.model.ModuleData;
import com.example.runner.service.ExerciseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST контроллер для управления упражнениями и модулями тренажера
 *
 * Предоставляет API для:
 * - Получения списка всех модулей тренажера
 * - Получения информации о конкретном модуле
 * - Получения блоков упражнений внутри модуля
 * - Получения упражнений с фильтрацией по сложности
 * - Получения доступных уровней сложности
 */
@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ExerciseController {
    /** Сервис для работы с упражнениями */
    private final ExerciseService exerciseService;

    /**
     * Конструктор контроллера
     * @param exerciseService сервис для работы с упражнениями
     */
    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    /**
     * Получает список всех доступных модулей тренажера
     * @return список модулей с их метаданными
     */
    @GetMapping
    public List<ModuleData> getAllModules() {
        return exerciseService.getAllModules();
    }

    /**
     * Получает информацию о конкретном модуле
     * @param moduleId идентификатор модуля (module-1, module-2, module-3)
     * @return данные модуля или 404 если модуль не найден
     */
    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleData> getModule(@PathVariable("moduleId") String moduleId) {
        ModuleData module = exerciseService.getModule(moduleId);
        return module != null
            ? ResponseEntity.ok(module)
            : ResponseEntity.notFound().build();
    }

    /**
     * Получает все блоки упражнений для указанного модуля
     * @param moduleId идентификатор модуля
     * @return список блоков упражнений
     */
    @GetMapping("/{moduleId}/blocks")
    public List<BlockData> getBlocks(@PathVariable("moduleId") String moduleId) {
        return exerciseService.getBlocks(moduleId);
    }

    /**
     * Получает информацию о конкретном блоке упражнений
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @return данные блока или 404 если блок не найден
     */
    @GetMapping("/{moduleId}/blocks/{blockId}")
    public ResponseEntity<BlockData> getBlock(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId
    ) {
        BlockData block = exerciseService.getBlock(moduleId, blockId);
        return block != null
            ? ResponseEntity.ok(block)
            : ResponseEntity.notFound().build();
    }

    /**
     * Получает все упражнения для указанного модуля
     * @param moduleId идентификатор модуля
     * @return список всех упражнений модуля
     */
    @GetMapping("/{moduleId}/exercises")
    public List<ExerciseData> getExercises(@PathVariable("moduleId") String moduleId) {
        return exerciseService.getExercises(moduleId);
    }

    /**
     * Получает упражнения для конкретного блока
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @return список упражнений блока
     */
    @GetMapping("/{moduleId}/blocks/{blockId}/exercises")
    public List<ExerciseData> getExercisesByBlock(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId
    ) {
        return exerciseService.getExercisesByBlock(moduleId, blockId);
    }

    /**
     * Получает упражнения модуля с фильтрацией по уровню сложности
     * @param moduleId идентификатор модуля
     * @param difficulty уровень сложности (EASY, MEDIUM, HARD)
     * @return список упражнений указанной сложности
     */
    @GetMapping("/{moduleId}/exercises/difficulty/{difficulty}")
    public List<ExerciseData> getExercisesByDifficulty(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("difficulty") Difficulty difficulty
    ) {
        return exerciseService.getExercisesByDifficulty(moduleId, difficulty);
    }

    /**
     * Получает упражнения блока с фильтрацией по уровню сложности
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @param difficulty уровень сложности (EASY, MEDIUM, HARD)
     * @return список упражнений блока указанной сложности
     */
    @GetMapping("/{moduleId}/blocks/{blockId}/exercises/difficulty/{difficulty}")
    public List<ExerciseData> getExercises(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId,
        @PathVariable("difficulty") Difficulty difficulty
    ) {
        return exerciseService.getExercises(moduleId, blockId, difficulty);
    }

    /**
     * Получает конкретное упражнение по его идентификатору
     * @param moduleId идентификатор модуля
     * @param exerciseId идентификатор упражнения
     * @return данные упражнения или 404 если упражнение не найдено
     */
    @GetMapping("/{moduleId}/exercises/{exerciseId}")
    public ResponseEntity<ExerciseData> getExercise(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("exerciseId") String exerciseId
    ) {
        ExerciseData exercise = exerciseService.getExercise(moduleId, exerciseId);
        return exercise != null
            ? ResponseEntity.ok(exercise)
            : ResponseEntity.notFound().build();
    }

    /**
     * Получает доступные уровни сложности для блока упражнений
     * @param moduleId идентификатор модуля
     * @param blockId идентификатор блока
     * @return множество доступных уровней сложности
     */
    @GetMapping("/{moduleId}/blocks/{blockId}/difficulties")
    public Set<Difficulty> getAvailableDifficulties(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId
    ) {
        return exerciseService.getAvailableDifficulties(moduleId, blockId);
    }
}
