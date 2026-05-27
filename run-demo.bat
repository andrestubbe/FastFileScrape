@echo off
echo 🚀 Running Demo (via JitPack v0.1.0)...
call mvn -q -f examples/Demo/pom.xml compile exec:java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Demo failed.
    pause
    exit /b %ERRORLEVEL%
)
pause