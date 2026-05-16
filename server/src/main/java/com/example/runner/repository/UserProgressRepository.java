package com.example.runner.repository;

import com.example.runner.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с прогрессом пользователей 
 * Предоставляет методы для поиска записей прогресса
 * по пользователям, модулям и упражнениям.
 */
@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    /** Находит прогресс по пользователю и упражнению */
    Optional<UserProgress> findByUserIdAndExerciseId(Long userId, String exerciseId);

    /** Находит прогресс по пользователю, модулю и упражнению */
    Optional<UserProgress> findByUserIdAndModuleIdAndExerciseId(Long userId, String moduleId, String exerciseId);

    /** Получает весь прогресс пользователя */
    List<UserProgress> findByUserId(Long userId);

    /** Получает прогресс пользователя по конкретному модулю */
    List<UserProgress> findByUserIdAndModuleId(Long userId, String moduleId);
}
