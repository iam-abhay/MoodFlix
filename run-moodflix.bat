@echo off
rem run-moodflix.bat — build and run MoodFlix (Windows)

setlocal

rem --- Configuration ---
rem Default password if not already set in environment variables
if "%MOODFLIX_DB_PASSWORD%"=="" (
    set MOODFLIX_DB_PASSWORD=Pass@1234
)

echo Using database password configured in environment.

rem --- Build ---
echo Building project with Maven...
call tools\apache-maven-3.9.9\bin\mvn.cmd -DskipTests=true clean package
if errorlevel 1 (
  echo Maven build failed or returned non-zero exit code.
  echo Will attempt to run existing jar if present.
)

rem --- Choose jar to run ---
set JAR=target\moodflix-1.0-SNAPSHOT.jar
if not exist "%JAR%" (
  if exist "target\moodflix-1.0-SNAPSHOT-shaded.jar" (
    set JAR=target\moodflix-1.0-SNAPSHOT-shaded.jar
  ) else (
    echo No jar found in target\ — run failed. Exiting.
    endlocal
    exit /b 1
  )
)

echo Running %JAR% ...
java -jar "%JAR%"

endlocal
pause
