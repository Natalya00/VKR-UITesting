package com.example.runner.controller;

import com.example.runner.dto.RunCodeRequest;
import com.example.runner.dto.RunCodeResponse;
import com.example.runner.security.UserDetailsImpl;
import com.example.runner.service.CodeExecutionService;
import com.example.runner.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/code")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class CodeExecutionController {

    private final CodeExecutionService codeExecutionService;
    private final ProgressService progressService;

    public CodeExecutionController(CodeExecutionService codeExecutionService,
                                   ProgressService progressService) {
        this.codeExecutionService = codeExecutionService;
        this.progressService = progressService;
    }

    @PostMapping("/run")
    public ResponseEntity<RunCodeResponse> runCode(
            @RequestBody RunCodeRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        RunCodeResponse response = codeExecutionService.runCode(request);
        return ResponseEntity.ok(response);
    }

    private String determineModuleId(int exerciseNum) {
        if (exerciseNum >= 101 && exerciseNum <= 157) {
            return "module-3";
        } else if (exerciseNum >= 50 && exerciseNum <= 139) {
            return "module-2";
        } else if (exerciseNum >= 1 && exerciseNum <= 49) {
            return "module-1";
        }
        return "module-1";
    }
}
