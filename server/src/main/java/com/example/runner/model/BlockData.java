package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Модель блока упражнений внутри модуля
 * Группирует упражнения по темам и сложности.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlockData {
    /** Идентификатор блока */
    @JsonProperty("blockId")
    private String blockId;
    
    /** Название блока */
    private String title;
    
    /** Описание блока */
    private String description;
    
    /** Общий уровень сложности блока */
    private Difficulty difficulty;
    
    /** Количество упражнений в блоке */
    private Integer exerciseCount;
    
    /** Список упражнений блока */
    private List<ExerciseData> exercises;
}
