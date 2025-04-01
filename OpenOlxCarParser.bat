@echo off
echo Starting OLX Car Parser...

start http://localhost:8080/

java -version >nul 2>&1 || (
    echo Error: Java not found
    pause
    exit /b 1
)

set "jarfile="
for /r "OlxCarParser\target" %%f in (*.jar) do set "jarfile=%%f"

if not defined jarfile (
    echo Building project...
    mvn clean package -DskipTests || (
        echo Build failed
        pause
        exit /b 1
    )
    for /r "OlxCarParser\target" %%f in (*.jar) do set "jarfile=%%f"
)

if not defined jarfile (
    echo No JAR file found
    pause
    exit /b 1
)

echo Running: %jarfile%
java -jar "%jarfile%"
pause