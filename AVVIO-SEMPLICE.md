# 🚀 Avvio BetCenterNL - Versione Semplice

## Passo 1: Avvia Backend Java

### Su Windows:
```bash
cd BackEnd
start-java-server.bat
```

### Su Linux/Mac:
```bash
cd BackEnd
chmod +x start-java-server.sh
./start-java-server.sh
```

**Vedrai:**
```
🔧 Compilazione Backend Java...
✅ Compilazione completata!
🚀 Avvio server web...
📡 Porta: 8080
🌐 URL: http://localhost:8080
```

## Passo 2: Avvia Frontend

### Opzione A - Live Server (VSCode)
1. Apri VSCode
2. Vai in `FrontEnd/index.html`
3. Tasto destro → "Open with Live Server"

### Opzione B - Server Node.js
```bash
cd FrontEnd
node server.js
```

## Passo 3: Gioca!

Apri il browser su: **http://localhost:3000**

## ✅ Test Rapido

1. **Backend**: http://localhost:8080 → Dovrebbe mostrare `{"status":"BetCenterNL Backend Running"}`
2. **Frontend**: http://localhost:3000 → Dovrebbe aprire il casino
3. **Registrati** e prova i giochi!

## 🚨 Se Non Funziona

### Errore Java
- Installa Java JDK 8 o superiore
- Controlla: `java -version`

### Porta occupata
- Cambia porta in `WebServer.java` (riga 15): `PORT = 8081`
- Ricompila e riprova

### Frontend non si collega
- Controlla che il backend sia su porta 8080
- Verifica in `FrontEnd/js/api.js` che l'URL sia `http://localhost:8080/api`

---

**Tutto qui! Niente Node.js, niente npm, solo Java + HTML! 🎰**