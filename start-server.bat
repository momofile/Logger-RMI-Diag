@echo off
:: Co-creator: momofile
setlocal
cd /d "%~dp0"
if not exist "out\LoggerServer.class" (
  echo Run build.bat first.
  pause
  exit /b 1
)
call :findjava
if "%JAVA%"=="" goto nojdk
echo Starting RMI diagnostic service on 127.0.0.1:1099 ...
echo Payload dir: %CD%\payload   (see payload\README.md)
echo Keep this window open. Ctrl+C to stop.
"%JAVA%" -Djava.rmi.server.hostname=127.0.0.1 ^
  -Djava.rmi.server.useCodebaseOnly=true ^
  -cp "%CD%\out" LoggerServer
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
