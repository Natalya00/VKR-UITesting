package com.example.runner.service;

import com.example.runner.dto.progress.ExerciseProgressResponse;
import com.example.runner.dto.progress.ModuleProgressResponse;
import com.example.runner.dto.progress.ModuleAttemptsResponse;
import com.example.runner.dto.progress.DetailedExerciseProgressResponse;
import com.example.runner.dto.progress.ExerciseAttemptResponse;
import com.example.runner.dto.progress.UserProgressResponse;
import com.example.runner.model.User;
import com.example.runner.model.UserProgress;
import com.example.runner.repository.ExerciseAttemptRepository;
import com.example.runner.repository.UserProgressRepository;
import com.example.runner.model.ExerciseAttempt;
import com.example.runner.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    private static final Map<String, Integer> MODULE_EXERCISE_COUNTS = Map.of(
            "module-1", 49,
            "module-2", 90,
            "module-3", 57
    );

    private final UserProgressRepository userProgressRepository;
    private final ExerciseAttemptRepository exerciseAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExerciseProgressResponse markExerciseComplete(
            Long userId,
            String moduleId,
            String exerciseId,
            String codeSnapshot,
            Boolean isSuccess,
            String errorMessage
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        ExerciseAttempt attempt = ExerciseAttempt.builder()
                .user(user)
                .moduleId(moduleId)
                .exerciseId(exerciseId)
                .codeSnapshot(codeSnapshot)
                .isSuccess(isSuccess)
                .errorMessage(errorMessage)
                .attemptsCount(1)
                .build();
        exerciseAttemptRepository.save(attempt);

        UserProgress progress = userProgressRepository
                .findByUserIdAndModuleIdAndExerciseId(userId, moduleId, exerciseId)
                .orElse(null);
        
        if (progress == null) {
            progress = UserProgress.builder()
                    .user(user)
                    .moduleId(moduleId)
                    .exerciseId(exerciseId)
                    .isCompleted(false)
                    .attemptsCount(0)
                    .build();
        }
        
        progress.incrementAttempts();

        if (Boolean.TRUE.equals(isSuccess) && !progress.getIsCompleted()) {
            progress.markCompleted();
            log.info("Exercise completed: user={}, module={}, exercise={}", userId, moduleId, exerciseId);
        }

        UserProgress savedProgress = userProgressRepository.save(progress);

        return ExerciseProgressResponse.builder()
                .exerciseId(exerciseId)
                .moduleId(moduleId)
                .isCompleted(savedProgress.getIsCompleted())
                .completedAt(savedProgress.getCompletedAt())
                .attemptsCount(savedProgress.getAttemptsCount())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProgressResponse getUserProgress(Long userId) {
        List<UserProgress> allProgress = userProgressRepository.findByUserId(userId);

        Map<String, List<UserProgress>> byModule = allProgress.stream()
                .collect(Collectors.groupingBy(UserProgress::getModuleId));

        List<ModuleProgressResponse> moduleResponses = MODULE_EXERCISE_COUNTS.entrySet().stream()
                .map(entry -> createModuleProgressResponse(entry.getKey(), entry.getValue(), byModule.getOrDefault(entry.getKey(), List.of())))
                .collect(Collectors.toList());

        int totalCompleted = moduleResponses.stream()
                .mapToInt(ModuleProgressResponse::getCompletedExercises)
                .sum();

        int totalExercises = moduleResponses.stream()
                .mapToInt(ModuleProgressResponse::getTotalExercises)
                .sum();

        double totalPercentage = totalExercises > 0
                ? (double) totalCompleted / totalExercises * 100.0
                : 0.0;

        return UserProgressResponse.builder()
                .userId(userId)
                .totalCompleted(totalCompleted)
                .totalExercises(totalExercises)
                .totalPercentage(totalPercentage)
                .modules(moduleResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public ModuleProgressResponse getModuleProgress(Long userId, String moduleId) {
        int totalExercises = MODULE_EXERCISE_COUNTS.getOrDefault(moduleId, 0);
        List<UserProgress> moduleProgress = userProgressRepository.findByUserIdAndModuleId(userId, moduleId);

        return createModuleProgressResponse(moduleId, totalExercises, moduleProgress);
    }

    @Transactional(readOnly = true)
    public ModuleAttemptsResponse getModuleAttempts(Long userId, String moduleId) {
        int totalExercises = MODULE_EXERCISE_COUNTS.getOrDefault(moduleId, 0);
        List<UserProgress> moduleProgress = userProgressRepository.findByUserIdAndModuleId(userId, moduleId);
        List<ExerciseAttempt> moduleAttempts = exerciseAttemptRepository.findByUserIdAndModuleIdOrderByCreatedAtDesc(userId, moduleId);

        Map<String, List<ExerciseAttempt>> attemptsByExercise = moduleAttempts.stream()
                .collect(Collectors.groupingBy(ExerciseAttempt::getExerciseId));

        List<DetailedExerciseProgressResponse> detailedExercises = moduleProgress.stream()
                .map(progress -> {
                    List<ExerciseAttempt> exerciseAttempts = attemptsByExercise.getOrDefault(progress.getExerciseId(), List.of());
                    
                    List<ExerciseAttemptResponse> attemptResponses = exerciseAttempts.stream()
                            .map(attempt -> ExerciseAttemptResponse.builder()
                                    .id(attempt.getId())
                                    .exerciseId(attempt.getExerciseId())
                                    .moduleId(attempt.getModuleId())
                                    .codeSnapshot(attempt.getCodeSnapshot())
                                    .isSuccess(attempt.getIsSuccess())
                                    .errorMessage(attempt.getErrorMessage())
                                    .createdAt(attempt.getCreatedAt())
                                    .exerciseNumber(attempt.getExerciseNumber())
                                    .build())
                            .collect(Collectors.toList());

                    int successfulAttempts = (int) exerciseAttempts.stream().filter(ExerciseAttempt::getIsSuccess).count();
                    int failedAttempts = exerciseAttempts.size() - successfulAttempts;

                    return DetailedExerciseProgressResponse.builder()
                            .exerciseId(progress.getExerciseId())
                            .moduleId(progress.getModuleId())
                            .isCompleted(progress.getIsCompleted())
                            .completedAt(progress.getCompletedAt())
                            .attemptsCount(progress.getAttemptsCount())
                            .successfulAttempts(successfulAttempts)
                            .failedAttempts(failedAttempts)
                            .firstAttemptAt(exerciseAttempts.stream()
                                    .map(ExerciseAttempt::getCreatedAt)
                                    .min(LocalDateTime::compareTo)
                                    .orElse(null))
                            .lastAttemptAt(exerciseAttempts.stream()
                                    .map(ExerciseAttempt::getCreatedAt)
                                    .max(LocalDateTime::compareTo)
                                    .orElse(null))
                            .attempts(attemptResponses)
                            .build();
                })
                .collect(Collectors.toList());

        int completed = (int) moduleProgress.stream().filter(UserProgress::getIsCompleted).count();
        double percentage = totalExercises > 0 ? (double) completed / totalExercises * 100.0 : 0.0;
        
        int totalAttempts = moduleAttempts.size();
        int successfulAttempts = (int) moduleAttempts.stream().filter(ExerciseAttempt::getIsSuccess).count();
        int failedAttempts = totalAttempts - successfulAttempts;

        return ModuleAttemptsResponse.builder()
                .moduleId(moduleId)
                .moduleTitle(getModuleTitle(moduleId))
                .totalExercises(totalExercises)
                .completedExercises(completed)
                .percentage(percentage)
                .totalAttempts(totalAttempts)
                .successfulAttempts(successfulAttempts)
                .failedAttempts(failedAttempts)
                .exercises(detailedExercises)
                .build();
    }

    private ModuleProgressResponse createModuleProgressResponse(
            String moduleId,
            int totalExercises,
            List<UserProgress> progressList
    ) {
        int completed = (int) progressList.stream()
                .filter(UserProgress::getIsCompleted)
                .count();

        double percentage = totalExercises > 0
                ? (double) completed / totalExercises * 100.0
                : 0.0;

        List<ExerciseProgressResponse> exerciseResponses = progressList.stream()
                .map(p -> ExerciseProgressResponse.builder()
                        .exerciseId(p.getExerciseId())
                        .moduleId(p.getModuleId())
                        .isCompleted(p.getIsCompleted())
                        .completedAt(p.getCompletedAt())
                        .attemptsCount(p.getAttemptsCount())
                        .build())
                .collect(Collectors.toList());

        return ModuleProgressResponse.builder()
                .moduleId(moduleId)
                .moduleTitle(getModuleTitle(moduleId))
                .totalExercises(totalExercises)
                .completedExercises(completed)
                .percentage(percentage)
                .exercises(exerciseResponses)
                .build();
    }

    private String getModuleTitle(String moduleId) {
        return switch (moduleId) {
            case "module-1" -> "XPath";
            case "module-2" -> "Взаимодействие с элементами";
            case "module-3" -> "Page Object Model (POM)";
            default -> moduleId;
        };
    }
}