package com.infotact.polyglotmesh.dto;

import jakarta.validation.constraints.*;
import java.util.Map;

public record ExecutionRequest(
    @NotBlank(message = "Language is required (js or python)") String language,
    @NotBlank(message = "Source code cannot be empty")
    @Size(max = 20000, message = "Source code must not exceed 20,000 characters")
    String code,
    Map<String, Object> bindings,
    @Min(100) @Max(15000) Long timeoutMs,
    @Min(100) @Max(500000) Long maxStatements
) {
    public long resolvedTimeoutMs() { return timeoutMs != null ? timeoutMs : 3000L; }
    public long resolvedMaxStatements() { return maxStatements != null ? maxStatements : 50000L; }
}
