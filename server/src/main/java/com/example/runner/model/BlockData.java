package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockData {
    @JsonProperty("blockId")
    private String blockId;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Integer exerciseCount;
    private List<ExerciseData> exercises;
}
