@echo off
echo Compiling AI-Based Resume Screening System (Pure Java 21)...
if not exist out mkdir out
javac -d out src\com\aiscreening\model\*.java src\com\aiscreening\service\*.java src\com\aiscreening\web\*.java src\com\aiscreening\Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    exit /b %errorlevel%
)
echo Starting Web Server on http://localhost:8080 ...
java -cp out com.aiscreening.Main 8080
