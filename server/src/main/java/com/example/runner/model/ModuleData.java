package com.example.runner.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleData {
    @JsonProperty("moduleId")
    private String moduleId;
    private String title;
    private List<BlockData> blocks;
}
