@echo off
setlocal
where mvn >nul 2>nul
if %ERRORLEVEL% equ 0 (
    mvn %*
    exit /b %ERRORLEVEL%
)

if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
    "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.3\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
    exit /b %ERRORLEVEL%
)

for /d %%I in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do (
    if exist "%%I\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
        "%%I\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
        exit /b %ERRORLEVEL%
    )
)

echo [ERROR] Maven not found. Please install Maven or add it to PATH.
exit /b 1
