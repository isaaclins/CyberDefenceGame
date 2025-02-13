@echo off
setlocal

REM Set the path to the JDK
set "JAVA_HOME=C:\Program Files\Java\jdk-17"

REM Set the path to the project directory
set "PROJECT_DIR=%~dp0"

REM Create the output directory if it doesn't exist
if not exist "%PROJECT_DIR%\out" mkdir "%PROJECT_DIR%\out"

REM Compile the Java files
echo Compiling Java files...
"%JAVA_HOME%\bin\javac" -d "%PROJECT_DIR%\out" "%PROJECT_DIR%\src\main\Game.java" "%PROJECT_DIR%\src\utils\Constants.java"

REM Run the DungeonGame
echo Running DungeonGame...
"%JAVA_HOME%\bin\java" -cp "%PROJECT_DIR%\out" src.main.Game

endlocal
pause
