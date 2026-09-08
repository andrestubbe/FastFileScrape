@echo off
chcp 65001 >nul
cd /d "%~dp0"

echo ===============================================================
echo  ⚡ FastFileScrape Benchmark
echo     Ultra-Fast File Tree ^& Content Scraper
echo ===============================================================
echo.

echo 📦 Building FastFileScrape...
call mvn clean package -q -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Build failed.
    pause
    exit /b 1
)
echo ✅ Build OK
echo.

echo 🔨 Compiling Benchmark...
if not exist "examples\Benchmark\target\classes" mkdir "examples\Benchmark\target\classes"
call javac --enable-native-access=ALL-UNNAMED ^
     -cp "target\FastFileScrape-0.1.0.jar" ^
     -d "examples\Benchmark\target\classes" ^
     "examples\Benchmark\src\main\java\fastfilescrape\Benchmark.java" 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Benchmark compile failed.
    pause
    exit /b 1
)
echo ✅ Compile OK
echo.

echo 🚀 Running Benchmark...
echo.
java --enable-native-access=ALL-UNNAMED ^
     -Dfile.encoding=UTF-8 ^
     -Dstdout.encoding=UTF-8 ^
     -cp "target\FastFileScrape-0.1.0.jar;examples\Benchmark\target\classes" ^
     fastfilescrape.Benchmark
if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ❌ Benchmark failed.
    pause
    exit /b 1
)
pause