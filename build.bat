@echo off
REM Bravery Alive Build Script
REM Usage: build.bat [clean|compile|package|run]

if "%1"=="run" goto run
if "%1"=="package" goto package
if "%1"=="compile" goto compile
if "%1"=="clean" goto clean

echo Usage: build.bat [clean^|compile^|package^|run]
echo.
echo Examples:
echo   build.bat clean      - Clean project
echo   build.bat compile    - Compile project
echo   build.bat package    - Build JAR file
echo   build.bat run        - Run the application
goto end

:clean
echo Cleaning project...
mvn clean
goto end

:compile
echo Compiling project...
mvn compile
goto end

:package
echo Building JAR file...
mvn clean package -DskipTests
goto end

:run
echo Starting Bravery Alive server...
echo.
echo Server will be available at: http://localhost:8080
echo Press Ctrl+C to stop the server
echo.
java -jar target\BraveryAlive-1.0-SNAPSHOT.jar
goto end

:end