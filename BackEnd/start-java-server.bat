@echo off
echo.
echo ╔════════════════════════════════════════╗
echo ║      BetCenterNL — Avvio Backend       ║
echo ╚════════════════════════════════════════╝
echo.

:: Verifica che javac sia disponibile
where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ERRORE: Java JDK non trovato nel PATH!
    echo.
    echo Per risolvere:
    echo 1. Scarica JDK 17 da: https://adoptium.net/temurin/releases/?version=17
    echo 2. Installa con le opzioni di default
    echo 3. Riapri questo terminale
    echo 4. Verifica con: javac -version
    echo.
    pause
    exit /b 1
)

:: Mostra versione Java
for /f "tokens=*" %%i in ('javac -version 2^>^&1') do echo Java: %%i
echo.

:: Crea cartella output
if not exist "out" mkdir out

:: Trova tutti i file .java
echo Compilazione in corso...
dir /s /b src\*.java > sources.txt 2>nul

if not exist sources.txt (
    echo ❌ Nessun file .java trovato in src\
    pause
    exit /b 1
)

:: Compila
javac -encoding UTF-8 -d out -cp src @sources.txt 2>compile_errors.txt

if %errorlevel% neq 0 (
    echo ❌ Errore di compilazione!
    echo.
    type compile_errors.txt
    del sources.txt
    del compile_errors.txt 2>nul
    pause
    exit /b 1
)

del sources.txt
del compile_errors.txt 2>nul
echo ✅ Compilazione completata!
echo.
echo Avvio server su porta 8080...
echo Premi Ctrl+C per fermare.
echo.

java -Dfile.encoding=UTF-8 -cp out server.WebServer

pause
