@echo off
:: Co-creator: momofile
setlocal
cd /d "%~dp0"
echo --- TCP listener on 1099 ---
netstat -ano | findstr ":1099"
echo --- RMI lookup + method probe ---
call :findjava
if "%JAVA%"=="" goto nojdk
"%JAVA%" -cp "%CD%\out" LoggerProbe
echo --- optional WebSocket listener on 10234 (NOT served by this tool) ---
netstat -ano | findstr ":10234"
echo --- recent requests ---
if exist "requests.log" (
  powershell -NoProfile -Command "Get-Content requests.log -Tail 12"
) else (
  echo no requests.log yet
)
pause
exit /b 0

:findjava
set "JAVA="
where java >nul 2>nul
if not errorlevel 1 (
  set "JAVA=java"
  goto :eof
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" (
  set "JAVA=%JAVA_HOME%\bin\java.exe"
  goto :eof
)
if exist "D:\jdk8\bin\java.exe" set "JAVA=D:\jdk8\bin\java.exe"
goto :eof

:nojdk
echo [ERROR] java not found. Install a JRE/JDK 8+ and/or set JAVA_HOME.
pause
exit /b 1
