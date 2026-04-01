@echo off
setlocal


echo.
echo ====================================================================================
echo      Falling Bricks Game
echo ====================================================================================
echo.

:: Check if Java is installed
echo --------------------Java Installation-------------------
java -version
echo -------------------------------------------------------
if errorlevel 1 (
    echo Java is not installed or not in PATH. Exiting...
    pause
    exit /b
)

:: Default port
set DEFAULT_PORT=8080

:: Prompt user for port with message
set PORT=
echo.
echo.
echo -------------------------------------------------------
echo Running FallingBricksGame on port %DEFAULT_PORT%.
echo.
echo Press Enter to use default port or type a port number.
echo.
echo Waiting 5 seconds for input...
echo -------------------------------------------------------
:: Wait 5 seconds for user input
set /p PORT=Port number: <nul
for /l %%i in (5,-1,1) do (
    ping -n 2 127.0.0.1 >nul
)

:: Use default port if user didn't enter anything
if "%PORT%"=="" set PORT=%DEFAULT_PORT%

echo.
echo Running FallingBricksGame on port %PORT%...
java -jar -Dserver.port=%PORT% FallingBricksGame-1.0.jar

pause