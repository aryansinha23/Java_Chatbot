@echo off
setlocal
echo =======================================================
echo Building Java Concept & Code Mentor (Fat-JAR)...
echo =======================================================

if defined JAVA_HOME goto check_mvn
if exist "C:\Users\HP\.jdks\jbr-21.0.11" set "JAVA_HOME=C:\Users\HP\.jdks\jbr-21.0.11"

:check_mvn
if exist "C:\Users\HP\.maven\apache-maven-3.9.6\bin\mvn.cmd" (
    call "C:\Users\HP\.maven\apache-maven-3.9.6\bin\mvn.cmd" clean package
) else (
    mvn clean package
)

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Fat-JAR compiled and packaged successfully in target/
    echo Run it via: run.bat
) else (
    echo.
    echo [ERROR] Build failed. Check compiler output above.
)
endlocal
