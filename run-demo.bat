@echo off
chcp 65001 >nul

echo ⚡ Building FastFileScrape...
call mvn clean package -q -DskipTests
if %ERRORLEVEL% NEQ 0 ( echo ❌ Build failed. & pause & exit /b 1 )

echo 🔨 Compiling Demo...
if not exist "examples\Demo\target\classes" mkdir "examples\Demo\target\classes"
call javac -cp "target\FastFileScrape-0.1.0.jar" ^
     -d "examples\Demo\target\classes" ^
     "examples\Demo\src\main\java\fastfilescrape\Demo.java" 2>nul
if %ERRORLEVEL% NEQ 0 ( echo ❌ Demo compile failed. & pause & exit /b 1 )

echo 🚀 Running Demo...
echo.
java --enable-native-access=ALL-UNNAMED ^
     -Dfile.encoding=UTF-8 ^
     -Dstdout.encoding=UTF-8 ^
     -cp "target\FastFileScrape-0.1.0.jar;examples\Demo\target\classes" ^
     fastfilescrape.Demo
if %ERRORLEVEL% NEQ 0 ( echo. & echo ❌ Demo failed. & pause & exit /b 1 )
pause
