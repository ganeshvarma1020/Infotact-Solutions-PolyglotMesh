package com.infotact.polyglotmesh.dto;

import java.time.Instant;

public record ExecutionResponse(
    Long executionId,
    String language,
    String status,
    String returnValue,
    String stdout,
    String stderr,
    String errorMessage,
    long durationMs,
    long maxStatementsApplied,
    Instant executedAt
) {}
