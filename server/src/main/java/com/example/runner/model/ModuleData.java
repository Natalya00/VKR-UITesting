package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * Модель данных модуля тренажера
 * Представляет один из трех модулей:
 * XPath, Selenide или Page Object Model.
 * Содержит список блоков упражнений.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleData {
    /** Идентификатор модуля (module-1, module-2, module-3) */
    @JsonProperty("moduleId")
    private String moduleId;
    
    /** Название модуля */
    private String title;
    
    /** Список блоков упражнений */
    private List<BlockData> blocks;
}
