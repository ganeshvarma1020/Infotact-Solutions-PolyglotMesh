@echo off
title PolyglotMesh - GraalVM Multi-Language Runtime Sandbox
chcp 65001 >nul
cls

echo ===============================================================================
echo   PolyglotMesh: GraalVM Multi-Language Runtime Sandbox (Ref: ITS/JD/1327)
echo   Intern: Ganesh Chakravarthula (ganeshvarma493@gmail.com)
echo   Infotact Solutions, Electronic City, Bengaluru
echo ===============================================================================
echo.

where java >nul 2>nul
if %ERRORLEVEL% neq 0 (
    echo [ERROR] Java is not installed or not in PATH!
    echo Please install JDK 21 or higher (e.g. GraalVM JDK 21 or Eclipse Temurin 21+).
    pause
    exit /b 1
)

echo [INFO] Detected Java Runtime:
java -version
echo.

echo [INFO] Starting PolyglotMesh with Spring Boot (Profile: H2 In-Memory Store)...
echo [INFO] Interactive Web Console: http://localhost:8080
echo [INFO] H2 Web Database Console: http://localhost:8080/h2-console
echo.
echo Press Ctrl+C at any time to terminate the sandbox.
echo -------------------------------------------------------------------------------
echo.

if exist "%~dp0target\polyglotmesh-1.0.0.jar" (
    java --enable-native-access=ALL-UNNAMED -Dpolyglotimpl.AttachLibraryFailureAction=warn -Dpolyglot.engine.resourcePath="%~dp0target\.graalvm-cache" -Dspring.profiles.active=h2 -jar "%~dp0target\polyglotmesh-1.0.0.jar"
) else (
    call "%~dp0mvnw.cmd" spring-boot:run -Dspring-boot.run.profiles=h2
)

if %ERRORLEVEL% neq 0 (
    echo.
    echo [ERROR] Server encountered an error on shutdown or startup.
    pause
)
