package com.infotact.polyglotmesh.controller;

import com.infotact.polyglotmesh.dto.ExecutionRequest;
import com.infotact.polyglotmesh.dto.ExecutionResponse;
import com.infotact.polyglotmesh.entity.ExecutionAuditLog;
import com.infotact.polyglotmesh.service.SandboxExecutionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sandbox")
public class SandboxController {

    private final SandboxExecutionService sandboxService;

    public SandboxController(SandboxExecutionService sandboxService) {
        this.sandboxService = sandboxService;
    }

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeScript(@Valid @RequestBody ExecutionRequest request) {
        return ResponseEntity.ok(sandboxService.executeSandboxed(request));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ExecutionAuditLog>> getRecentExecutions() {
        return ResponseEntity.ok(sandboxService.getRecentAuditLogs());
    }

    @GetMapping("/languages")
    public ResponseEntity<List<Map<String, String>>> getSupportedLanguages() {
        return ResponseEntity.ok(List.of(
                Map.of("id", "js", "name", "JavaScript (GraalJS ECMAScript 2023)"),
                Map.of("id", "python", "name", "Python 3 (GraalPy Sandboxed Isolate)")
        ));
    }
}
