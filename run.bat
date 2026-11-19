@echo off
cd /d "%~dp0"
echo ==============================================
echo  CAR RENTAL ^& REPAIR MANAGEMENT SYSTEM
 echo ==============================================
where javac >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java JDK is not installed or not added to PATH.
    echo Install JDK 17 or later and reopen this window.
    pause
    exit /b 1
)
echo Compiling the ONE Java source file...
javac -encoding UTF-8 -cp "lib/*" CarRentalRepairSystem.java
if errorlevel 1 (
    echo Compilation failed. Read the error shown above.
    pause
    exit /b 1
)
echo.
echo Open http://localhost:8080 in Chrome or Edge.
echo Stop: press Ctrl+C in this window.
echo.
java -cp ".;lib/*" CarRentalRepairSystem
if errorlevel 1 echo Java exited with an error. Check text above.
pause
