# PolyglotMesh Launcher Script
# Intern: Ganesh Chakravarthula | Specification Ref: ITS/JD/1327

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
Clear-Host

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "  PolyglotMesh: GraalVM Multi-Language Runtime Sandbox (Ref: ITS/JD/1327)" -ForegroundColor White
Write-Host "  Intern: Ganesh Chakravarthula (ganeshvarma493@gmail.com)" -ForegroundColor Yellow
Write-Host "  Infotact Solutions, Electronic City, Bengaluru" -ForegroundColor Gray
Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host ""

$javaCmd = Get-Command java -ErrorAction SilentlyContinue
if (-not $javaCmd) {
    Write-Host "[ERROR] Java is not installed or not in PATH!" -ForegroundColor Red
    Write-Host "Please install JDK 21 or higher." -ForegroundColor Red
    exit 1
}

Write-Host "[INFO] Starting PolyglotMesh with Spring Boot (Profile: H2 In-Memory Store)..." -ForegroundColor Green
Write-Host "[INFO] Interactive Web Console: http://localhost:8080" -ForegroundColor Cyan
Write-Host "[INFO] H2 Web Database Console: http://localhost:8080/h2-console" -ForegroundColor Cyan
Write-Host ""
Write-Host "Press Ctrl+C at any time to stop the server." -ForegroundColor Gray
Write-Host "-------------------------------------------------------------------------------" -ForegroundColor Gray

$jarPath = Join-Path $PSScriptRoot "target\polyglotmesh-1.0.0.jar"
$cachePath = Join-Path $PSScriptRoot "target\.graalvm-cache"

if (Test-Path $jarPath) {
    & java --enable-native-access=ALL-UNNAMED -Dpolyglotimpl.AttachLibraryFailureAction=warn "-Dpolyglot.engine.resourcePath=$cachePath" -Dspring.profiles.active=h2 -jar $jarPath
} else {
    & "$PSScriptRoot\mvnw.cmd" spring-boot:run -Dspring-boot.run.profiles=h2
}
