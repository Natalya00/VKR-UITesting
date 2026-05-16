package com.example.runner.repository;

import com.example.runner.model.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с попытками выполнения упражнений
 * Предоставляет методы для поиска и анализа
 * детальных записей о попытках выполнения.
 */
@Repository
public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long> {
    
    /** Получает все попытки пользователя, отсортированные по времени (новые сверху) */
    List<ExerciseAttempt> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /** Получает все попытки пользователя по конкретному упражнению */
    List<ExerciseAttempt> findByUserIdAndExerciseIdOrderByCreatedAtDesc(Long userId, String exerciseId);
    
    /** Получает все попытки пользователя по конкретному модулю */
    List<ExerciseAttempt> findByUserIdAndModuleIdOrderByCreatedAtDesc(Long userId, String moduleId);
    
    /** Подсчитывает количество попыток пользователя по упражнению */
    int countByUserIdAndExerciseId(Long userId, String exerciseId);
    
    /** Находит попытки по частичному совпадению ID упражнения */
    List<ExerciseAttempt> findByExerciseIdContaining(String exerciseIdPattern);
    
    /** Находит попытки по паттерну ID упражнения */
    @Query("SELECT ea FROM ExerciseAttempt ea WHERE ea.exerciseId LIKE %:exerciseId%")
    List<ExerciseAttempt> findByExerciseIdPattern(@Param("exerciseId") String exerciseId);
    
    /** Находит попытки по статусу успешности */
    List<ExerciseAttempt> findByIsSuccess(Boolean isSuccess);
    
    /** Находит попытки по статусу успешности, отсортированные по времени */
    List<ExerciseAttempt> findByIsSuccessOrderByCreatedAtDesc(Boolean isSuccess);
}