# Presentation Deck: PolyglotMesh GraalVM Multi-Language Runtime Sandbox
**Official Specification:** Ref. No.: ITS/JD/1327  
**Candidate:** Ganesh Chakravarthula (`ganeshvarma493@gmail.com` | `+91 9391761912`)  
**Organization & Role:** Infotact Solutions, Electronic City, Bengaluru — Associate L1. Intern (Java Development)  
**Project Window:** 10/11/2026 to 10/12/2026  

---

## Slide 1: Title & Project Overview
- **Title:** PolyglotMesh: High-Performance Multi-Language Runtime Sandbox
- **Subtitle:** Production Engineering Blueprint using GraalVM Truffle Polyglot Engine, Spring Boot 3 & PostgreSQL
- **Speaker:** Ganesh Chakravarthula (Associate L1. Intern, Java Development)
- **Organization:** Infotact Solutions, Electronic City, Bengaluru

---

## Slide 2: Problem Statement & Industry Motivation
- **The Challenge:** Modern platforms (low-code tools, workflow engines, SaaS automations) require running user-defined scripts dynamically.
- **The Risk:**
  - Remote Code Execution (RCE) via Java reflection (`java.lang.System.exit()`, `Runtime.getRuntime().exec()`).
  - Denial of Service (DoS) via runaway infinite loops (`while(true)`).
  - Out of Memory (OOM) heap exhaustion attacks.
  - Historical `java.lang.SecurityManager` has been permanently deprecated and removed.
- **The Container Dilemma:** Spawning Docker containers per execution incurs 300–800 ms cold starts and 64+ MB memory overhead per request.

---

## Slide 3: Architectural Solution — Truffle AST Sandboxing
- **PolyglotMesh Design:** In-process, sub-millisecond execution isolation using Oracle GraalVM Truffle framework.
- **Unified Engine:** Shares a single JIT-compiled AST engine across requests for fast warm-up and zero container overhead.
- **Dual Runtime Support:**
  - ECMAScript 2023 JavaScript (`js`) via GraalJS.
  - Python 3 Runtime (`python`) via GraalPy isolate.
- **Zero OS Syscall Overhead:** Sub-millisecond execution latency instead of spinning up heavy Linux namespaces or VMs.

---

## Slide 4: Multi-Layered Defense-in-Depth Boundary
1. **Layer 1: Zero Class Reflection (`HostAccess.EXPLICIT`)**
   - Guest cannot instantiate Java classes or use `Java.type()`.
   - Blocks `java.lang.Runtime`, `java.io.File`, `java.lang.System`.
2. **Layer 2: Statement Counter Quotas (`ResourceLimits`)**
   - Truffle AST-level deterministic execution statement quota (e.g. 50,000 statements).
   - Terminates `while(true)` runaway loops cleanly without crashing host threads.
3. **Layer 3: Hard Wall-Clock Watchdog Monitor**
   - Bounded thread `Future.get(timeoutMs, TimeUnit.MILLISECONDS)`.
   - Watchdog invokes `context.close(true)` asynchronously if wall-clock timeout expires.
4. **Layer 4: Total I/O & Network Quarantine**
   - `IOAccess.NONE`, `allowCreateProcess(false)`, `allowCreateThread(false)`, `allowNativeAccess(false)`.

---

## Slide 5: Object-Oriented Domain Model & Architecture
- **Layered Clean Architecture:**
  - **Controller:** `SandboxController` — REST API handling `/execute`, `/history`, `/languages`.
  - **Service:** `SandboxExecutionService` — Engine isolation, Future timeouts, AST quota guards, stdout/stderr capture.
  - **Configuration:** `PolyglotConfig` — Shared `Engine` bean and custom `HostAccess.EXPLICIT` bean.
  - **Data Transfer Objects (DTO):** `ExecutionRequest` & `ExecutionResponse` (Immutable Java Records with Jakarta Validation).
  - **Persistence:** `ExecutionAuditLog` (JPA Entity) & `ExecutionAuditRepository` (Spring Data JPA).
  - **Exception Handling:** `GlobalExceptionHandler` (`@RestControllerAdvice`).

---

## Slide 6: Database Persistence & Audit Compliance
- **Compliance Requirement:** Enterprise code execution platforms must maintain immutable forensic audit logs.
- **PostgreSQL Table:** `execution_audit_logs`
  - Stores: `id`, `language`, `source_code`, `status`, `return_value`, `stdout`, `stderr`, `error_message`, `duration_ms`, `max_statements_applied`, `executed_at`.
- **Dual-Profile Flexibility:**
  - Production: PostgreSQL 16 Alpine container.
  - Local / Standalone: In-memory H2 database (`jdbc:h2:mem:polyglotmesh`).

---

## Slide 7: Interactive Developer Console (UI)
- **Modern Cyberpunk / Dark Mode Single-Page Console:**
  - Built-in test presets: JS Array Math, Python Sum of Squares, Host Reflection Attack, Runaway Loop, Host Bindings.
  - Real-time telemetry dashboard: Execution ID, Duration (ms), Status Badge, Quota Enforced.
  - Tabbed output viewer: Return Value, Stdout, Stderr, Raw REST API JSON.
  - Live PostgreSQL audit history table with one-click code re-loader.

---

## Slide 8: Benchmark & Evaluation Results
- **Cold-Start vs. Warm-Engine Latency:**
  - JavaScript: 673 ms cold start $\rightarrow$ **12 ms warm execution** (98.2% latency reduction).
  - Python: 3375 ms cold start $\rightarrow$ **45 ms warm execution** (98.7% latency reduction).
- **Security Benchmark:**
  - 100% of runaway infinite loops intercepted by `ResourceLimits` ($<45\text{ ms}$).
  - 100% of host reflection escape attacks blocked by `HostAccess.EXPLICIT` ($<10\text{ ms}$).
  - Zero JVM crashes or resource leaks across repeated test batches.

---

## Slide 9: Containerization & Live Production Deployment
- **Multi-Stage Dockerfile:**
  - Build stage: `ghcr.io/graalvm/jdk-community:21` with Maven compilation.
  - Runtime stage: Stripped GraalVM JDK 21 base image running as non-root `meshuser:meshgroup`.
- **Docker Compose:**
  - `polyglotmesh-db`: PostgreSQL 16 Alpine with `pg_isready` healthcheck.
  - `polyglotmesh-app`: Spring Boot backend with auto-dependency orchestration.

---

## Slide 10: Conclusion & Deliverables Summary
- **All 6 Deliverables Completed:**
  1. Source Code (Spring Boot 3, GraalVM Polyglot SDK, Clean Architecture)
  2. Project Documentation (`README.md` with cURL / OpenAPI guides)
  3. Technical Project Report (`PROJECT_REPORT.md`)
  4. Slide Presentation Deck (`docs/PRESENTATION.md` & `presentation.html`)
  5. Benchmark & Security Results (`BENCHMARK_RESULTS.md`)
  6. Live Interactive Demonstration Console (`http://localhost:8080`)
