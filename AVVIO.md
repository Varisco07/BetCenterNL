# 🚀 Come Avviare BetCenterNL

## Installazione (prima volta)

```bash
npm run install-all
```

## Avvio

```bash
npm start
```

## Risultato

- **Frontend**: http://localhost:3000 ← Apri questo nel browser
- **Backend API**: http://localhost:3001 ← Funziona automaticamente

## Cosa Succede

1. Il **frontend JavaScript** (porta 3000) mostra l'interfaccia del casino
2. Il **backend API Node.js** (porta 3001) gestisce login, saldi, giochi
3. Il **backend Java** contiene la logica avanzata dei giochi (opzionale)

## Se Qualcosa Non Funziona

### Porta già in uso
```bash
# Ferma tutti i processi Node.js
taskkill /f /im node.exe
# Poi riprova
npm start
```

### Dipendenze mancanti
```bash
npm run install-all
```

### Reset completo
```bash
# Elimina node_modules
rmdir /s BackEnd\node_modules
rmdir /s FrontEnd\node_modules
# Reinstalla
npm run install-all
```

## Test Rapido

1. Apri http://localhost:3000
2. Clicca "Registrati"
3. Compila il form
4. Vai ai giochi (es. Slot Machine)
5. Piazza una scommessa

Se funziona, tutto è collegato correttamente! 🎰

---

**Problemi?** Controlla che Node.js sia installato: `node --version`