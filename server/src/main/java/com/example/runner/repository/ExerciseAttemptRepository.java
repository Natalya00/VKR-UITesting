package com.example.runner.repository;

import com.example.runner.model.ExerciseAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExerciseAttemptRepository extends JpaRepository<ExerciseAttempt, Long> {
    
    List<ExerciseAttempt> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<ExerciseAttempt> findByUserIdAndExerciseIdOrderByCreatedAtDesc(Long userId, String exerciseId);
    
    List<ExerciseAttempt> findByUserIdAndModuleIdOrderByCreatedAtDesc(Long userId, String moduleId);
    
    int countByUserIdAndExerciseId(Long userId, String exerciseId);
    
    List<ExerciseAttempt> findByExerciseIdContaining(String exerciseIdPattern);
    
    @Query("SELECT ea FROM ExerciseAttempt ea WHERE ea.exerciseId LIKE %:exerciseId%")
    List<ExerciseAttempt> findByExerciseIdPattern(@Param("exerciseId") String exerciseId);
    
    List<ExerciseAttempt> findByIsSuccess(Boolean isSuccess);
    
    List<ExerciseAttempt> findByIsSuccessOrderByCreatedAtDesc(Boolean isSuccess);
}