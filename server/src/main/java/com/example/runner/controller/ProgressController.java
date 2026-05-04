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

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProgressController {

    private final ProgressService progressService;

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

    public record CompleteExerciseRequest(
            String codeSnapshot,
            Boolean isSuccess,
            String errorMessage
    ) {}
}
