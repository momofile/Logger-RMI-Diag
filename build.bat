@echo off
:: Co-creator: momofile
setlocal
cd /d "%~dp0"
call :findjavac
if "%JAVAC%"=="" goto nojdk
if not exist "out" mkdir out
"%JAVAC%" -encoding UTF-8 -d out ^
  src\reflect\nya\al\protocol\LoggerProtocol.java ^
  src\StubDll.java ^
  src\LoggerServer.java ^
  src\LoggerProbe.java
if errorlevel 1 (
  echo BUILD FAILED
  pause
  exit /b 1
)
echo BUILD OK - classes in .\out
pause
exit /b 0

:findjavac
set "JAVAC="
where javac >nul 2>nul
if not errorlevel 1 (
  set "JAVAC=javac"
  goto :eof
)
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" (
  set "JAVAC=%JAVA_HOME%\bin\javac.exe"
  goto :eof
)
if exist "D:\jdk8\bin\javac.exe" set "JAVAC=D:\jdk8\bin\javac.exe"
goto :eof

:nojdk
echo [ERROR] javac not found. Install a JDK 8+ and/or set JAVA_HOME.
echo         A Java 8 runtime is required on the game side; any JDK 8+ can compile this.
pause
exit /b 1
