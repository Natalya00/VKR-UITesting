package com.example.runner.dto.module3;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InterfaceRule {

    @JsonProperty("interfaceName")
    private String interfaceName;

    @JsonProperty("modifiers")
    private String[] modifiers;

    @JsonProperty("requiredMethods")
    private String[] requiredMethods;

    @JsonProperty("required")
    private boolean required = true;

    @JsonProperty("errorMessage")
    private String errorMessage;
}
