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

@RestController
@RequestMapping("/api/exercises")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ExerciseController {
    private final ExerciseService exerciseService;

    public ExerciseController(ExerciseService exerciseService) {
        this.exerciseService = exerciseService;
    }

    @GetMapping
    public List<ModuleData> getAllModules() {
        return exerciseService.getAllModules();
    }

    @GetMapping("/{moduleId}")
    public ResponseEntity<ModuleData> getModule(@PathVariable("moduleId") String moduleId) {
        ModuleData module = exerciseService.getModule(moduleId);
        return module != null
            ? ResponseEntity.ok(module)
            : ResponseEntity.notFound().build();
    }

    @GetMapping("/{moduleId}/blocks")
    public List<BlockData> getBlocks(@PathVariable("moduleId") String moduleId) {
        return exerciseService.getBlocks(moduleId);
    }

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

    @GetMapping("/{moduleId}/exercises")
    public List<ExerciseData> getExercises(@PathVariable("moduleId") String moduleId) {
        return exerciseService.getExercises(moduleId);
    }

    @GetMapping("/{moduleId}/blocks/{blockId}/exercises")
    public List<ExerciseData> getExercisesByBlock(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId
    ) {
        return exerciseService.getExercisesByBlock(moduleId, blockId);
    }

    @GetMapping("/{moduleId}/exercises/difficulty/{difficulty}")
    public List<ExerciseData> getExercisesByDifficulty(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("difficulty") Difficulty difficulty
    ) {
        return exerciseService.getExercisesByDifficulty(moduleId, difficulty);
    }

    @GetMapping("/{moduleId}/blocks/{blockId}/exercises/difficulty/{difficulty}")
    public List<ExerciseData> getExercises(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId,
        @PathVariable("difficulty") Difficulty difficulty
    ) {
        return exerciseService.getExercises(moduleId, blockId, difficulty);
    }

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

    @GetMapping("/{moduleId}/blocks/{blockId}/difficulties")
    public Set<Difficulty> getAvailableDifficulties(
        @PathVariable("moduleId") String moduleId,
        @PathVariable("blockId") String blockId
    ) {
        return exerciseService.getAvailableDifficulties(moduleId, blockId);
    }
}
