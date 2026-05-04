package com.example.runner.repository;

import com.example.runner.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserIdAndExerciseId(Long userId, String exerciseId);

    Optional<UserProgress> findByUserIdAndModuleIdAndExerciseId(Long userId, String moduleId, String exerciseId);

    List<UserProgress> findByUserId(Long userId);

    List<UserProgress> findByUserIdAndModuleId(Long userId, String moduleId);
}
