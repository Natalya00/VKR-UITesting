package com.example.runner.dto;

public record RunCodeResponse(boolean success,
                              boolean uiMode,
                              String stdout,
                              String stderr,
                              String message) {
}


