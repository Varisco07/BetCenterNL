@echo off
echo Compilazione Backend Java...

if not exist "out" mkdir out

:: Trova tutti i file .java ricorsivamente e salvali in un file temporaneo
dir /s /b src\*.java > sources.txt

:: Compila tutti i file trovati
javac -encoding UTF-8 -d out -cp src @sources.txt

if %errorlevel% neq 0 (
    echo Errore di compilazione!
    del sources.txt
    pause
    exit /b 1
)

del sources.txt
echo Compilazione completata!
echo Avvio server Java su porta 8080...

java -Dfile.encoding=UTF-8 -cp out server.WebServer

pause
