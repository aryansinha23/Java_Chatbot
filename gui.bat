@echo off
setlocal
echo =======================================================
echo Launching Java Concept & Code Mentor GUI...
echo =======================================================

if defined JAVA_HOME goto find_jar
if exist "C:\Users\HP\.jdks\jbr-21.0.11" set "JAVA_HOME=C:\Users\HP\.jdks\jbr-21.0.11"
set "PATH=%JAVA_HOME%\bin;%PATH%"

:find_jar
for /f "delims=" %%F in ('dir /b /s target\java-concept-mentor-*.jar 2^>nul ^| findstr /v "original-"') do (
    set "JAR_PATH=%%F"
    goto launch
)

echo [Notice] Executable JAR not found in target/. Running build.bat first...
call build.bat
for /f "delims=" %%F in ('dir /b /s target\java-concept-mentor-*.jar 2^>nul ^| findstr /v "original-"') do (
    set "JAR_PATH=%%F"
    goto launch
)

echo [Error] JAR file could not be created.
exit /b 1

:launch
echo Starting GUI: %JAR_PATH%
start javaw -jar "%JAR_PATH%" --gui
endlocal
