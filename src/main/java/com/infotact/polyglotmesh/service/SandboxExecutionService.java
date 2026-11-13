package com.infotact.polyglotmesh.service;

import com.infotact.polyglotmesh.dto.*;
import com.infotact.polyglotmesh.entity.ExecutionAuditLog;
import com.infotact.polyglotmesh.repository.ExecutionAuditRepository;
import org.graalvm.polyglot.*;
import org.graalvm.polyglot.io.IOAccess;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Predicate;

@Service
public class SandboxExecutionService {

    private static final Predicate<Source> USER_SOURCE_FILTER = s -> !s.isInternal();

    private final Engine sharedEngine;
    private final HostAccess sandboxHostAccess;
    private final ExecutionAuditRepository auditRepository;
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "polyglot-worker");
        t.setDaemon(true);
        return t;
    });

    @jakarta.annotation.PreDestroy
    public void shutdown() {
        executor.shutdownNow();
    }

    public SandboxExecutionService(Engine sharedEngine,
                                   HostAccess sandboxHostAccess,
                                   ExecutionAuditRepository auditRepository) {
        this.sharedEngine = sharedEngine;
        this.sandboxHostAccess = sandboxHostAccess;
        this.auditRepository = auditRepository;
    }

    public ExecutionResponse executeSandboxed(ExecutionRequest request) {
        SupportedLanguage lang = SupportedLanguage.fromString(request.language());
        long timeoutMs = request.resolvedTimeoutMs();
        long maxStatements = request.resolvedMaxStatements();
        Instant startInstant = Instant.now();
        long startNanos = System.nanoTime();

        ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
        ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

        ResourceLimits limits = ResourceLimits.newBuilder()
                .statementLimit(maxStatements, USER_SOURCE_FILTER).build();

        String status = "SUCCESS", returnVal = null, errorMsg = null;

        try (Context context = Context.newBuilder(lang.getGraalId())
                .engine(sharedEngine)
                .allowHostAccess(sandboxHostAccess)
                .allowHostClassLookup(className -> false)
                .allowIO(IOAccess.NONE)
                .allowCreateThread(false)
                .allowCreateProcess(false)
                .allowNativeAccess(false)
                .allowEnvironmentAccess(EnvironmentAccess.NONE)
                .resourceLimits(limits)
                .out(stdoutStream).err(stderrStream).build()) {

            Future<String> future = executor.submit(() -> {
                if (request.bindings() != null && !request.bindings().isEmpty()) {
                    Value bindings = context.getBindings(lang.getGraalId());
                    request.bindings().forEach(bindings::putMember);
                }
                Source src = Source.newBuilder(lang.getGraalId(), request.code(),
                        "script." + lang.getGraalId()).build();
                Value result = context.eval(src);
                return result != null ? result.toString() : "null";
            });

            try {
                returnVal = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException te) {
                context.close(true);
                status = "TIMEOUT";
                errorMsg = "Execution timed out after " + timeoutMs + " ms (terminated by watchdog)";
            } catch (ExecutionException ee) {
                Throwable cause = ee.getCause();
                if (cause instanceof PolyglotException pe) {
                    if (pe.isResourceExhausted() || pe.isCancelled()) {
                        status = "RESOURCE_LIMIT";
                        errorMsg = "Resource limit exceeded (maxStatements=" + maxStatements + "): " + pe.getMessage();
                    } else if (pe.isSyntaxError()) {
                        status = "SYNTAX_ERROR";
                        errorMsg = "Syntax error: " + pe.getMessage();
                    } else {
                        status = "SECURITY_OR_RUNTIME_ERROR";
                        errorMsg = "Guest execution error: " + pe.getMessage();
                    }
                } else {
                    status = "INTERNAL_ERROR";
                    errorMsg = cause != null ? cause.getMessage() : ee.getMessage();
                }
            }
        } catch (PolyglotException pe) {
            if ("SUCCESS".equals(status)) {
                status = pe.isResourceExhausted() || pe.isCancelled() ?
                        "RESOURCE_LIMIT" : "SECURITY_OR_RUNTIME_ERROR";
                errorMsg = pe.getMessage();
            }
        } catch (Exception e) {
            if ("SUCCESS".equals(status)) {
                status = "INTERNAL_ERROR";
                errorMsg = e.getMessage();
            }
        }

        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
        String stdout = truncate(stdoutStream.toString(StandardCharsets.UTF_8), 8000);
        String stderr = truncate(stderrStream.toString(StandardCharsets.UTF_8), 8000);

        ExecutionAuditLog log = new ExecutionAuditLog();
        log.setLanguage(lang.getGraalId());
        log.setSourceCode(request.code());
        log.setStatus(status);
        log.setReturnValue(returnVal);
        log.setStdout(stdout);
        log.setStderr(stderr);
        log.setErrorMessage(errorMsg);
        log.setDurationMs(durationMs);
        log.setMaxStatementsApplied(maxStatements);
        log.setExecutedAt(startInstant);

        ExecutionAuditLog saved = auditRepository.save(log);

        return new ExecutionResponse(saved.getId(), lang.getGraalId(), status, returnVal,
                stdout, stderr, errorMsg, durationMs, maxStatements, startInstant);
    }

    public List<ExecutionAuditLog> getRecentAuditLogs() {
        return auditRepository.findTop25ByOrderByExecutedAtDesc();
    }

    private String truncate(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max) + "\n...[TRUNCATED]...";
    }
}
