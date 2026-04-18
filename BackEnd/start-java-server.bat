@echo off
echo 🔧 Compilazione Backend Java...

:: Crea cartella output se non esiste
if not exist "out" mkdir out

:: Compila tutti i file Java
javac -d out -cp src src/server/WebServer.java src/core/*.java src/games/*/*.java src/games/*/*/*.java

if %errorlevel% neq 0 (
    echo ❌ Errore di compilazione!
    pause
    exit /b 1
)

echo ✅ Compilazione completata!
echo 🚀 Avvio server web...

:: Avvia il server
java -cp out server.WebServer

pause