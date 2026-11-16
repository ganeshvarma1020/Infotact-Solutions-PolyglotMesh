# PolyglotMesh End-to-End Live Verification Script
$cp = (Get-Content classpath.txt) + ";target\classes"
$java = "C:\Program Files\Java\jdk-26\bin\java.exe"
$jvmArgs = @(
    "--enable-native-access=ALL-UNNAMED",
    "-Dpolyglotimpl.AttachLibraryFailureAction=warn",
    "-Dpolyglot.engine.resourcePath=target\.graalvm-cache",
    "-Dspring.profiles.active=h2",
    "-cp", $cp,
    "com.infotact.polyglotmesh.PolyglotMeshApplication"
)

Write-Host ">>> Starting PolyglotMesh Spring Boot Server on port 8080 (Profile: h2)..."
$proc = Start-Process -FilePath $java -ArgumentList $jvmArgs -PassThru -NoNewWindow

# Wait for server health endpoint
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 1
    try {
        $res = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sandbox/languages" -Method Get -TimeoutSec 2 -ErrorAction Stop
        if ($res) {
            $ready = $true
            Write-Host ">>> Server is UP and healthy!"
            break
        }
    } catch {
        Write-Host "Waiting for server to listen on port 8080 ($($i+1)/30)..."
    }
}

if (-not $ready) {
    Write-Host "Server failed to start in 30 seconds."
    if ($proc -and -not $proc.HasExited) { Stop-Process -Id $proc.Id -Force }
    exit 1
}

Write-Host "`n======================================================="
Write-Host "TEST 1: JavaScript Execution with Host Bindings"
Write-Host "======================================================="
$body1 = @{
    language = "js"
    code = "console.log('Multiplier:', factor); [1,2,3,4].reduce((a,b)=>a+b, 0) * factor;"
    bindings = @{ factor = 22 }
} | ConvertTo-Json
$res1 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sandbox/execute" -Method Post -Body $body1 -ContentType "application/json"
$res1 | ConvertTo-Json -Depth 5

Write-Host "`n======================================================="
Write-Host "TEST 2: Python 3 Execution"
Write-Host "======================================================="
$body2 = @{
    language = "python"
    code = "print('Running GraalPy')`nsum([x*x for x in range(10)])"
    timeoutMs = 15000
} | ConvertTo-Json
$res2 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sandbox/execute" -Method Post -Body $body2 -ContentType "application/json"
$res2 | ConvertTo-Json -Depth 5

Write-Host "`n======================================================="
Write-Host "TEST 3: Statement Limit Protection against Infinite Loop"
Write-Host "======================================================="
$body3 = @{
    language = "js"
    code = "let i = 0; while(true) { i++; }"
    maxStatements = 1000
} | ConvertTo-Json
$res3 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sandbox/execute" -Method Post -Body $body3 -ContentType "application/json"
$res3 | ConvertTo-Json -Depth 5

Write-Host "`n======================================================="
Write-Host "TEST 4: Host Escape Prevention (Java.type reflection)"
Write-Host "======================================================="
$body4 = @{
    language = "js"
    code = "Java.type('java.lang.System').exit(1);"
} | ConvertTo-Json
$res4 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sandbox/execute" -Method Post -Body $body4 -ContentType "application/json"
$res4 | ConvertTo-Json -Depth 5

Write-Host "`n======================================================="
Write-Host "TEST 5: Check Audit History Endpoint"
Write-Host "======================================================="
$res5 = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sandbox/history" -Method Get
Write-Host "Audit log entries saved: $($res5.Count)"
$res5 | ConvertTo-Json -Depth 5

# Clean shutdown
Write-Host "`n>>> Stopping test server..."
if ($proc -and -not $proc.HasExited) { Stop-Process -Id $proc.Id -Force }
Write-Host ">>> Verification completed successfully!"
