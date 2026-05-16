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

/**
 * Сервис для управления прогрессом пользователей
 * 
 * Предоставляет функциональность для:
 * - Отслеживания выполнения упражнений пользователями
 * - Сохранения попыток выполнения с кодом и результатами
 * - Подсчета статистики по модулям и упражнениям
 * - Анализа успешности и неуспешности попыток
 * - Отслеживания временных меток выполнения
 * - Вычисления процентов завершения по модулям
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProgressService {

    /** Константы с количеством упражнений в каждом модуле */
    private static final Map<String, Integer> MODULE_EXERCISE_COUNTS = Map.of(
            "module-1", 49,  // XPath тренажер
            "module-2", 90,  // Selenide тренажер  
            "module-3", 57   // Page Object Model тренажер
    );

    /** Репозиторий для работы с основными записями прогресса пользователей */
    private final UserProgressRepository userProgressRepository;
    
    /** Репозиторий для работы с детальными записями попыток выполнения */
    private final ExerciseAttemptRepository exerciseAttemptRepository;
    
    /** Репозиторий для работы с пользователями */
    private final UserRepository userRepository;

    /**
     * Отмечает упражнение как выполненное и сохраняет детальную информацию о попытке
     * @param userId идентификатор пользователя
     * @param moduleId идентификатор модуля (module-1, module-2, module-3)
     * @param exerciseId идентификатор упражнения
     * @param codeSnapshot снимок кода пользователя на момент выполнения
     * @param isSuccess флаг успешности выполнения упражнения
     * @param errorMessage сообщение об ошибке (если есть)
     * @return информация о прогрессе по упражнению после обновления
     * @throws IllegalArgumentException если пользователь не найден
     */
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

    /**
     * Получает общий прогресс пользователя по всем модулям тренажера
     * 
     * Вычисляет:
     * - Общее количество выполненных упражнений
     * - Общее количество упражнений во всех модулях
     * - Общий процент выполнения
     * - Детальный прогресс по каждому модулю
     * 
     * @param userId идентификатор пользователя
     * @return сводная информация о прогрессе пользователя
     */
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

    /**
     * Получает детальный прогресс пользователя по конкретному модулю 
     * @param userId идентификатор пользователя
     * @param moduleId идентификатор модуля
     * @return детальная информация о прогрессе по модулю
     */
    @Transactional(readOnly = true)
    public ModuleProgressResponse getModuleProgress(Long userId, String moduleId) {
        int totalExercises = MODULE_EXERCISE_COUNTS.getOrDefault(moduleId, 0);
        List<UserProgress> moduleProgress = userProgressRepository.findByUserIdAndModuleId(userId, moduleId);

        return createModuleProgressResponse(moduleId, totalExercises, moduleProgress);
    }

    /**
     * Получает детальную историю всех попыток выполнения упражнений модуля
     * 
     * Включает:
     * - Основную статистику по модулю (выполнено/всего, процент)
     * - Детальную информацию по каждому упражнению
     * - Историю всех попыток с кодом, результатами и временными метками
     * - Статистику успешных и неуспешных попыток
     * - Временные метки первой и последней попытки по каждому упражнению
     * 
     * @param userId идентификатор пользователя
     * @param moduleId идентификатор модуля
     * @return детальная информация о попытках выполнения упражнений модуля
     */
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

    /**
     * Создает объект ответа с прогрессом по модулю на основе списка записей прогресса
     * @param moduleId идентификатор модуля
     * @param totalExercises общее количество упражнений в модуле
     * @param progressList список записей прогресса пользователя по модулю
     * @return объект с информацией о прогрессе по модулю
     */
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

    /**
     * Возвращает название модуля по его идентификатору
     * @param moduleId идентификатор модуля (module-1, module-2, module-3)
     * @return название модуля на русском языке
     */
    private String getModuleTitle(String moduleId) {
        return switch (moduleId) {
            case "module-1" -> "XPath";
            case "module-2" -> "Взаимодействие с элементами";
            case "module-3" -> "Page Object Model (POM)";
            default -> moduleId;
        };
    }
}