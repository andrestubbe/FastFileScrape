@echo off
echo 🚀 Running Benchmark (via JitPack v0.1.0)...
call mvn -q -f examples/Benchmark/pom.xml compile exec:java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Benchmark failed.
    pause
    exit /b %ERRORLEVEL%
)
pause