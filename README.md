# 🎰 BetCenterNL - Casino & Virtual Sports

Piattaforma completa di casino online e scommesse virtuali con frontend JavaScript e backend Java.

## 🚀 Avvio Rapido

### Opzione 1: Backend Java + Frontend (Raccomandato)

#### 1. Avvia il Backend Java
```bash
# Su Windows
cd BackEnd
start-java-server.bat

# Su Linux/Mac
cd BackEnd
chmod +x start-java-server.sh
./start-java-server.sh
```

#### 2. Avvia il Frontend
```bash
# In un altro terminale
cd FrontEnd
# Apri index.html con Live Server in VSCode
# oppure
node server.js
```

#### 3. Apri il browser
Vai su **http://localhost:3000** (frontend)
Il backend sarà su **http://localhost:8080**

### Opzione 2: Solo Backend Java (Terminale)
```bash
cd BackEnd
javac -d out src/**/*.java
java -cp out Main
```

## 📡 Porte Utilizzate

- **Frontend**: http://localhost:3000
- **Backend Java API**: http://localhost:8080
- **Backend Java Console**: Solo terminale

## 🎮 Giochi Disponibili

### 🎰 Casino
- **Slot Machine** - 3 rulli, 8 simboli, jackpot
- **Blackjack** - Classico 21, hit/stand/double
- **Roulette** - Europea, tutti i tipi di scommessa
- **Video Poker** - Jacks or Better
- **Dadi/Craps** - Pass Line, Field Bet
- **Baccarat** - Giocatore/Banco/Pareggio

### ⚽ Virtual Sports
- **Calcio Virtuale** - Serie A, Champions
- **Tennis** - Tornei virtuali
- **Basket** - NBA virtuale
- **Corse Cavalli** - 8 cavalli
- **Corse Cani** - Greyhound racing

## 🔧 Avvio Manuale

Se preferisci avviare i server separatamente:

### Backend API
```bash
cd BackEnd
npm install
npm start
```

### Frontend
```bash
cd FrontEnd
npm install
npm start
```

### Backend Java (opzionale)
```bash
cd BackEnd
javac -d out src/**/*.java
java -cp out Main
```

## 🌐 API Endpoints

Il backend Node.js espone queste API REST:

- `POST /api/auth/register` - Registrazione
- `POST /api/auth/login` - Login
- `GET /api/user/profile` - Profilo utente
- `POST /api/games/slots/spin` - Slot machine
- `POST /api/games/blackjack/deal` - Blackjack
- `POST /api/games/roulette/spin` - Roulette
- `GET /api/wallet/balance` - Saldo
- `POST /api/wallet/deposit` - Deposito
- `GET /api/leaderboard` - Classifica

## 💾 Database

Il sistema usa file JSON per semplicità:
- `BackEnd/data/users.json` - Dati utenti
- `BackEnd/data/games.json` - Storico partite

## 🎯 Funzionalità

- ✅ **Sistema di login/registrazione**
- ✅ **Saldo persistente** tra sessioni
- ✅ **6 giochi casino** completi
- ✅ **5 sport virtuali**
- ✅ **Classifica giocatori**
- ✅ **Storico scommesse**
- ✅ **Design responsive**
- ✅ **API REST complete**

## ⚠️ Note

- Questo è un progetto **dimostrativo**
- Nessuna transazione reale
- Saldi virtuali
- Per uso educativo

## 🛠️ Requisiti

- **Node.js** ≥ 14
- **Java** ≥ 8 (opzionale, per backend Java)
- Browser moderno

---

**Buon divertimento! 🎲🎰**