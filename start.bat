@echo off
chcp 65001 >nul
echo.
echo ╔════════════════════════════════════════╗
echo ║         BetCenterNL — Avvio            ║
echo ╚════════════════════════════════════════╝
echo.

:: ── Verifica Java ────────────────────────────────────────────────────────────
where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Java JDK non trovato!
    echo Scarica da: https://adoptium.net/temurin/releases/?version=17
    pause
    exit /b 1
)

:: ── Verifica Node.js ──────────────────────────────────────────────────────────
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Node.js non trovato!
    echo Scarica da: https://nodejs.org
    pause
    exit /b 1
)

:: ── Compilazione Java ─────────────────────────────────────────────────────────
echo [1/3] Compilazione Java...
if not exist "BackEnd\out" mkdir BackEnd\out
dir /s /b BackEnd\src\*.java > BackEnd\sources.txt 2>nul
javac -encoding UTF-8 -d BackEnd\out -cp BackEnd\src @BackEnd\sources.txt 2>BackEnd\compile_errors.txt
if %errorlevel% neq 0 (
    echo ❌ Errore di compilazione!
    type BackEnd\compile_errors.txt
    del BackEnd\sources.txt 2>nul
    del BackEnd\compile_errors.txt 2>nul
    pause
    exit /b 1
)
del BackEnd\sources.txt 2>nul
del BackEnd\compile_errors.txt 2>nul
echo ✅ Compilazione completata!

:: ── Avvio Backend Java (in background, nuova finestra) ───────────────────────
echo [2/3] Avvio Backend Java (porta 8080)...
start "BetCenterNL - Backend Java" cmd /k "java -Dfile.encoding=UTF-8 -cp BackEnd\out server.WebServer"

:: Aspetta 3 secondi che il backend si avvii
timeout /t 3 /nobreak >nul

:: ── Avvio Frontend Node.js (in background, nuova finestra) ───────────────────
echo [3/3] Avvio Frontend (porta 3000)...
start "BetCenterNL - Frontend" cmd /k "node FrontEnd\server.js"

:: Aspetta 2 secondi che il frontend si avvii
timeout /t 2 /nobreak >nul

:: ── Apri il browser ───────────────────────────────────────────────────────────
echo.
echo ✅ Tutto avviato!
echo.
echo   Backend:  http://localhost:8080/api/health
echo   Frontend: http://localhost:3000
echo.
echo Apertura browser...
start http://localhost:3000

echo.
echo Puoi chiudere questa finestra.
echo Per fermare i server chiudi le finestre "Backend Java" e "Frontend".
echo.
pause
