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
public class UserProgressResponse {

    private Long userId;
    private Integer totalCompleted;
    private Integer totalExercises;
    private Double totalPercentage;
    private List<ModuleProgressResponse> modules;
}
