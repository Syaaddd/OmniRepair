@echo off
setlocal

REM Find Java from IntelliJ
set JAVA_HOME=C:\PROGRA~1\JETBRA~1\INTERN~1.1\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo Building with Maven...
    mvn clean package -DskipTests
) else (
    echo Maven not found. Please build using IntelliJ IDEA:
    echo 1. Open project in IntelliJ IDEA
    echo 2. Click Build ^> Build Project
    echo 3. JAR will be in target\ folder
)

endlocal
