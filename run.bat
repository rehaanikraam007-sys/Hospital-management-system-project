@echo off
setlocal
echo ====================================================================
echo      MediCore HMS v3.4 - Clinical Desktop Client & Suite
echo ====================================================================

REM Set Java Home to bundled JetBrains JBR if available
if exist "C:\Users\rehaa\AppData\Local\Programs\IntelliJ IDEA 2026.2.0.1\jbr\bin\java.exe" (
    set "JAVA_HOME=C:\Users\rehaa\AppData\Local\Programs\IntelliJ IDEA 2026.2.0.1\jbr"
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

set "JAR_FILE=target\medicore-hms-3.4.0-jar-with-dependencies.jar"

if not exist "%JAR_FILE%" (
    echo Building MediCore HMS with Maven...
    call "C:\Users\rehaa\AppData\Local\Programs\IntelliJ IDEA 2026.2.0.1\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" clean package -DskipTests
    if errorlevel 1 (
        echo [ERROR] Maven build failed!
        pause
        exit /b 1
    )
)

echo Starting MediCore HMS Desktop Client...
java --enable-native-access=ALL-UNNAMED -jar "%JAR_FILE%" %*
pause
