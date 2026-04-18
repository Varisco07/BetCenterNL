#!/bin/bash

echo "🔧 Compilazione Backend Java..."

# Crea cartella output se non esiste
mkdir -p out

# Compila tutti i file Java
javac -d out -cp src src/server/WebServer.java src/core/*.java src/games/*/*.java src/games/*/*/*.java

if [ $? -ne 0 ]; then
    echo "❌ Errore di compilazione!"
    exit 1
fi

echo "✅ Compilazione completata!"
echo "🚀 Avvio server web..."

# Avvia il server
java -cp out server.WebServer