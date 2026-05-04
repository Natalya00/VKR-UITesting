package com.example.runner.dto.progress;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleProgressResponse {

    private String moduleId;
    private String moduleTitle;
    private Integer totalExercises;
    private Integer completedExercises;
    private Double percentage;
    private List<ExerciseProgressResponse> exercises;
}
