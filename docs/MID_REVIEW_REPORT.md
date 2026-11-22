# PolyglotMesh: GraalVM Multi-Language Runtime Sandbox
## Mid-Term Technical Review & Project Management Briefing
**Specification Ref:** ITS/JD/1327 | **Organization:** Infotact Solutions, Electronic City, Bengaluru

---

### Project & Candidate Metadata
- **Intern Name:** Ganesh Chakravarthula
- **Email:** [ganeshvarma493@gmail.com](mailto:ganeshvarma493@gmail.com) | **Phone:** +91 9391761912
- **Role:** Associate L1. Intern (Java Development)
- **Review Period:** Weeks 1 & 2 (11 Nov – 25 Nov 2026)
- **Project Repository:** [https://github.com/ganeshvarma1020/Infotact-Solutions-PolyglotMesh](https://github.com/ganeshvarma1020/Infotact-Solutions-PolyglotMesh)
- **Status:** **All Mid-Review Milestones Complete (Eligible for Sign-Off)**

---

## 1. Executive Summary for Project Manager

PolyglotMesh is an enterprise multi-language execution platform built on **Java 21 / Spring Boot 3.3.5** and **Oracle GraalVM Truffle Polyglot Engine 24.1.1**. It allows host applications to evaluate untrusted guest scripts (**JavaScript ECMAScript 2023** and **Python 3.11+ via GraalPy**) in-process within a shared Java Virtual Machine while enforcing absolute hardware-level and statement-level sandboxing.

Every guest script evaluation is isolated, metered against statement execution quotas, monitored by daemon thread watchdog timers, and audited into a persistent PostgreSQL / JPA database.

---

## 2. 4-Layer Defensive Security Architecture

| Security Layer | Truffle API Mechanism | Attack Mitigation |
| :--- | :--- | :--- |
| **Layer 1: Host Access Lockdown** | `HostAccess.EXPLICIT` + `allowHostClassLookup(c -> false)` | Prevents reflection escapes, access to `java.lang.System`, `Runtime.getRuntime()`, and classloaders. |
| **Layer 2: I/O & Socket Virtualization** | `IOAccess.NONE` + `allowEnvironmentAccess(NONE)` | Completely blocks disk reads/writes, network sockets, port binding, and OS environment inspection. |
| **Layer 3: AST Statement Quota** | `ResourceLimits.statementLimit(maxStatements)` | Hard limit on Truffle AST statements executed. Halts runaway infinite loops and algorithmic complexity attacks within milliseconds. |
| **Layer 4: Daemon Watchdog Timers** | `CompletableFuture` / `Future.get(timeout, TimeUnit)` | Thread-level watchdog that forcefully cancels and tears down contexts exceeding deadlines. |

---

## 3. Two-Week Milestone Progression (Day-by-Day Commit Audit)

### Week 1: Core Architecture & In-Process Sandboxing (11 Nov – 17 Nov)
- **Day 1 (Nov 11):** Initialized Spring Boot 3 architecture, Maven dependencies, H2 and PostgreSQL configuration profiles.
- **Day 2 (Nov 12):** Configured shared `Engine` bean and `HostAccess.EXPLICIT` security boundaries; defined immutable Record DTOs (`ExecutionRequest`, `ExecutionResponse`).
- **Day 3 (Nov 13):** Implemented `SandboxExecutionService` with AST statement limits, daemon thread pooling, and watchdog timeouts.
- **Day 4 (Nov 14):** Integrated Spring Data JPA `ExecutionAuditLog` entity, repository, and `/api/v1/sandbox/execute` REST endpoints.
- **Day 5 (Nov 15):** Designed modern single-page developer console (`index.html`) with live telemetry and test presets.
- **Day 6 (Nov 16):** Built automated endpoint verification suite (`verify-endpoints.ps1`) and conducted multi-run empirical benchmarks.
- **Day 7 (Nov 17):** Authored comprehensive `README.md` with system prerequisites, setup guides, and API documentation.

### Week 2: Hardening, Containerization & Mid-Review Readiness (18 Nov – 25 Nov)
- **Day 8 (Nov 18):** Implemented multi-stage containerization with `Dockerfile` and `docker-compose.yml` (PostgreSQL 16 Alpine + PolyglotMesh App).
- **Day 9 (Nov 19):** Formalized technical internship report (`docs/PROJECT_REPORT.md`) detailing Truffle AST execution internals.
- **Day 10 (Nov 20):** Developed 10-slide presentation deck (`docs/PRESENTATION.md` and `docs/presentation.html`).
- **Day 11 (Nov 21):** Hardened exception advice, added automated JVM resource caching, and created one-click Windows launcher scripts (`run.bat`, `run.ps1`).
- **Day 12 (Nov 22):** Finalized Mid-Review Project Manager Executive Briefing and verification reports.

---

## 4. Key Performance Benchmarks & Security Audit

### 4.1 Latency Benchmarks (Averaged Over 10 Iterations)
- **JavaScript Functional Pipeline (Warm):** `42 ms` (Cold: `655 ms` due to initial Truffle context compilation).
- **Python 3 GraalPy In-Memory Computation:** `85 ms` (Cold: `3,375 ms` due to Python C-API & runtime AST initialization).
- **Infinite Loop Statement Quota Interception:** `35 ms` (Intercepted at statement 1,001 with quota 1,000).
- **Host Reflection Escape Attack Interception:** `8 ms` (Blocked with `SECURITY_OR_RUNTIME_ERROR`).

### 4.2 Security Penetration Test Results (10 Attack Vectors Evaluated)
1. `java.lang.System.exit(1)`: **BLOCKED** (`HostAccess.EXPLICIT`)
2. `Runtime.getRuntime().exec("calc")`: **BLOCKED** (Class lookup rejected)
3. `new java.io.File("/").listFiles()`: **BLOCKED** (`IOAccess.NONE`)
4. Network Socket Connection (`java.net.Socket`): **BLOCKED** (Zero socket privileges)
5. Environment Variable Snoop (`System.getenv()`): **BLOCKED** (`EnvironmentAccess.NONE`)
6. Thread Spawning (`new Thread(...)`): **BLOCKED** (`allowCreateThread(false)`)
7. Native Memory / JNI Access: **BLOCKED** (`allowNativeAccess(false)`)
8. CPU Infinite Loop (`while(true)`): **INTERCEPTED** (Statement limit triggered)
9. Memory Bomb Array Allocation: **MITIGATED** (JVM heap ceiling & statement quota)
10. Uncaught Guest Syntax Errors: **INTERCEPTED** (Captured in `ExecutionResponse` without crashing host JVM)

---

## 5. Deliverables Verification Checklist for Mid Review

| Deliverable | Specification Ref | Status | Evidence Location |
| :--- | :--- | :--- | :--- |
| **1. Source Code** | ITS/JD/1327 §1 | **COMPLETE** | `src/main/java/` & GitHub repo |
| **2. Project Documentation** | ITS/JD/1327 §2 | **COMPLETE** | `README.md` |
| **3. Technical Report** | ITS/JD/1327 §3 | **COMPLETE** | `docs/PROJECT_REPORT.md` |
| **4. Presentation Deck** | ITS/JD/1327 §4 | **COMPLETE** | `docs/PRESENTATION.md` & `docs/presentation.html` |
| **5. Benchmark Evaluation** | ITS/JD/1327 §5 | **COMPLETE** | `docs/BENCHMARK_RESULTS.md` |
| **6. Demonstration Guide** | ITS/JD/1327 §6 | **COMPLETE** | Interactive Web UI (`http://localhost:8080`) |

---

## 6. Recommendations & Next Steps for Final Review
1. Multi-Node Deployment: Scale engine across stateless worker pods managed by Kubernetes.
2. Advanced Memory Quotas: Upgrade from statement limits to GraalVM Enterprise heap memory isolation when licensed.
3. Expanded Language Catalog: Integrate WebAssembly (`wasm`) and Ruby (`truffleruby`) guest targets.
