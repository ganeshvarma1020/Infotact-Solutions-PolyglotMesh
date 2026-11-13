package com.infotact.polyglotmesh;

import com.infotact.polyglotmesh.dto.ExecutionRequest;
import com.infotact.polyglotmesh.dto.ExecutionResponse;
import com.infotact.polyglotmesh.service.SandboxExecutionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class SandboxExecutionServiceTest {

    @Autowired
    private SandboxExecutionService sandboxService;

    @Test
    @DisplayName("Verify JavaScript execution with host bindings and stdout capture")
    void shouldExecuteJavaScriptWithBindingsAndCaptureStdout() {
        ExecutionRequest req = new ExecutionRequest("js",
                "console.log('Hello ' + user); base * 5;",
                Map.of("user", "Ganesh", "base", 9),
                5000L, 10000L);
        ExecutionResponse res = sandboxService.executeSandboxed(req);
        assertEquals("SUCCESS", res.status());
        assertEquals("45", res.returnValue());
        assertTrue(res.stdout().contains("Hello Ganesh"));
        assertNotNull(res.executionId());
    }

    @Test
    @DisplayName("Verify statement limit enforcement on runaway infinite loops")
    void shouldEnforceStatementLimitOnInfiniteLoop() {
        ExecutionRequest req = new ExecutionRequest("js",
                "let c = 0; while (true) { c++; }",
                null, 5000L, 500L);
        ExecutionResponse res = sandboxService.executeSandboxed(req);
        assertEquals("RESOURCE_LIMIT", res.status());
        assertNotNull(res.errorMessage());
    }

    @Test
    @DisplayName("Verify host class reflection blocking (Java.type escape prevention)")
    void shouldBlockHostJavaClassAccess() {
        ExecutionRequest req = new ExecutionRequest("js",
                "Java.type('java.io.File')",
                null, 5000L, 5000L);
        ExecutionResponse res = sandboxService.executeSandboxed(req);
        assertNotEquals("SUCCESS", res.status());
    }
}
