@echo off
echo ========================================================
echo Compiling Waste Collection ^& Municipal Complaint System
echo ========================================================

if not exist bin mkdir bin

javac -encoding UTF-8 -cp "lib/*" -d bin ^
    src/config/*.java ^
    src/utils/*.java ^
    src/model/*.java ^
    src/dao/*.java ^
    src/service/*.java ^
    src/ui/components/*.java ^
    src/ui/*.java ^
    src/*.java

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Compilation successful! Output in bin/
) else (
    echo.
    echo [ERROR] Compilation failed!
)
