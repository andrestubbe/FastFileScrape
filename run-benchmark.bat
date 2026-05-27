@echo off
echo ⚡ Building Main Project (Quiet Mode)...
call mvn -q clean package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Maven build failed.
    pause
    exit /b %ERRORLEVEL%
)
echo.
echo 🚀 Running Benchmark...
call mvn -q -f examples/Benchmark/pom.xml compile
call java --enable-native-access=ALL-UNNAMED -cp "target\classes;examples\Benchmark\target\classes;C:\Users\andre\.m2\repository\com\github\andrestubbe\FastGlob\v0.1.0\FastGlob-v0.1.0.jar;C:\Users\andre\.m2\repository\com\github\andrestubbe\fastcore\v1.0.0\fastcore-v1.0.0.jar" fastfilescrape.Benchmark
pause
