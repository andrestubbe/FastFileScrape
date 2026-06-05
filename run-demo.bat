@echo off

echo [FastFileScrape] Running Demo (via JitPack v0.1.0)...
call mvn -f examples/Demo/pom.xml compile exec:java
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Demo failed.
    pause
    exit /b %ERRORLEVEL%
)
pause
