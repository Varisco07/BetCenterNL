# BetCenterNL — Migrazione Backend Java → Node.js

## Riepilogo della Trasformazione

Il backend Java è stato completamente trasformato in un'API Node.js/Express moderna, compatibile con il frontend esistente.

## Cosa è Stato Fatto

### 1. **Creazione Backend Node.js**

#### Struttura
```
BackEnd/
├── server.js                 # Entry point Express
├── package.json              # Dipendenze npm
├── .env                      # Configurazione
├── .gitignore
├── utils/
│   ├── db.js                # Database JSON
│   ├── auth.js              # JWT authentication
│   └── games.js             # Logica giochi
└── routes/
    ├── auth.js              # Autenticazione
    ├── user.js              # Profilo utente
    ├── games.js             # Giochi
    ├── wallet.js            # Portafoglio
    └── leaderboard.js       # Classifica
```

#### Tecnologie
- **Express.js** — Framework web
- **JWT** — Autenticazione
- **JSON Storage** — Database (sviluppo)
- **CORS** — Cross-origin requests

### 2. **API REST Endpoints**

Tutti gli endpoint sono RESTful e documentati:

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrazione |
| POST | `/api/auth/login` | Login |
| GET | `/api/auth/verify` | Verifica token |
| GET | `/api/user/profile` | Profilo utente |
| PUT | `/api/user/profile` | Aggiorna profilo |
| POST | `/api/games/slots/spin` | Slot machine |
| POST | `/api/games/blackjack/deal` | Blackjack |
| POST | `/api/games/roulette/spin` | Roulette |
| POST | `/api/games/dadi/roll` | Dadi |
| POST | `/api/games/virtual/:sport/match` | Scommesse virtuali |
| GET | `/api/games/history` | Storico partite |
| GET | `/api/wallet/balance` | Saldo |
| POST | `/api/wallet/deposit` | Deposita |
| POST | `/api/wallet/daily-bonus` | Bonus giornaliero |
| GET | `/api/leaderboard` | Classifica |
| GET | `/api/leaderboard/rank/:userId` | Rank utente |

### 3. **Client API JavaScript**

Nuovo file `FrontEnd/js/api.js` che fornisce:

```javascript
API.register(userData)
API.login(email, password)
API.verify()
API.getProfile()
API.updateProfile(updates)
API.spinSlots(bet)
API.dealBlackjack(bet)
API.spinRoulette(bet, betType, betValue)
API.rollDadi(bet)
API.playVirtualSport(sport, bet, prediction)
API.getGameHistory(limit)
API.getBalance()
API.deposit(amount)
API.claimDailyBonus()
API.getLeaderboard()
API.getUserRank(userId)
```

### 4. **Autenticazione JWT**

- Token generato al login/register
- Scade dopo 7 giorni
- Incluso in header `Authorization: Bearer <token>`
- Validato su ogni richiesta protetta

### 5. **Database**

- **Sviluppo**: JSON file storage (`data/users.json`, `data/games.json`)
- **Produzione**: Pronto per MongoDB, PostgreSQL, etc.

### 6. **Giochi Implementati**

Tutti i giochi del frontend hanno logica server-side:

- ✅ Slot Machine (8 simboli, paytable, jackpot)
- ✅ Blackjack (hit, stand, double)
- ✅ Roulette (numeri, colori, pari/dispari)
- ✅ Dadi/Craps (pass line, field bet)
- ✅ Scommesse Virtuali (calcio, tennis, basket, corse)

## Come Usare

### Avvio Rapido

**Linux/Mac:**
```bash
chmod +x start-dev.sh
./start-dev.sh
```

**Windows:**
```bash
start-dev.bat
```

### Avvio Manuale

**Terminal 1 — Backend:**
```bash
cd BackEnd
npm install
npm start
```

**Terminal 2 — Frontend:**
```bash
cd FrontEnd
npm install
npm start
```

Poi apri: **http://localhost:3000**

## Flusso di Comunicazione

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (3000)                          │
│  - UI Components                                            │
│  - Game Rendering                                           │
│  - State Management (localStorage)                          │
│  - API Client (js/api.js)                                   │
└─────────────────────────────────────────────────────────────┘
                          ↓ HTTP/REST
                    Authorization: Bearer <token>
                          ↓
┌─────────────────────────────────────────────────────────────┐
│                    BACKEND (3001)                           │
│  - Authentication (JWT)                                     │
│  - User Management                                          │
│  - Game Logic Validation                                    │
│  - Balance Management                                       │
│  - Leaderboard                                              │
│  - JSON Database                                            │
└─────────────────────────────────────────────────────────────┘
```

## Differenze da Java

| Aspetto | Java | Node.js |
|---------|------|---------|
| Framework | Spring Boot | Express.js |
| Database | SQL/NoSQL | JSON (sviluppo) |
| Autenticazione | Spring Security | JWT |
| Deployment | JAR/WAR | npm start |
| Porta | 8080 | 3001 |
| Linguaggio | Java | JavaScript |

## Prossimi Passi

### Sviluppo
1. ✅ Backend API creato
2. ✅ Frontend client API creato
3. ⏳ Testare integrazione completa
4. ⏳ Aggiungere più giochi
5. ⏳ Implementare WebSocket per live updates

### Produzione
1. Sostituire JSON storage con database reale
2. Implementare hashing password (bcrypt)
3. Aggiungere rate limiting
4. Configurare HTTPS
5. Implementare pagamenti reali
6. Aggiungere KYC/AML compliance
7. Deploy su cloud (AWS, Heroku, etc.)

## File Creati

### Backend
- `BackEnd/server.js` — Entry point
- `BackEnd/package.json` — Dipendenze
- `BackEnd/.env` — Configurazione
- `BackEnd/.gitignore` — Git ignore
- `BackEnd/utils/db.js` — Database utilities
- `BackEnd/utils/auth.js` — JWT utilities
- `BackEnd/utils/games.js` — Game logic
- `BackEnd/routes/auth.js` — Auth endpoints
- `BackEnd/routes/user.js` — User endpoints
- `BackEnd/routes/games.js` — Game endpoints
- `BackEnd/routes/wallet.js` — Wallet endpoints
- `BackEnd/routes/leaderboard.js` — Leaderboard endpoints
- `BackEnd/README.md` — Documentazione backend

### Frontend
- `FrontEnd/js/api.js` — API client

### Documentazione
- `SETUP.md` — Guida setup completo
- `MIGRATION_SUMMARY.md` — Questo file
- `start-dev.sh` — Script avvio Linux/Mac
- `start-dev.bat` — Script avvio Windows

## Configurazione

### Backend (.env)
```
PORT=3001
NODE_ENV=development
JWT_SECRET=your_jwt_secret_key_change_in_production
DB_PATH=./data/users.json
GAMES_DB_PATH=./data/games.json
```

### Frontend (js/api.js)
```javascript
const BASE_URL = 'http://localhost:3001/api';
```

## Testing

### Test Manuale

1. **Registrazione**
   ```bash
   curl -X POST http://localhost:3001/api/auth/register \
     -H "Content-Type: application/json" \
     -d '{"nome":"Mario","cognome":"Rossi","username":"mario","email":"mario@test.com","password":"test123","dob":"1990-01-01"}'
   ```

2. **Login**
   ```bash
   curl -X POST http://localhost:3001/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"mario@test.com","password":"test123"}'
   ```

3. **Spin Slot**
   ```bash
   curl -X POST http://localhost:3001/api/games/slots/spin \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <token>" \
     -d '{"bet":10}'
   ```

## Troubleshooting

### Errore: "Cannot find module 'express'"
```bash
cd BackEnd
npm install
```

### Errore: "Port 3001 already in use"
```bash
# Linux/Mac
lsof -i :3001
kill -9 <PID>

# Windows
netstat -ano | findstr :3001
taskkill /PID <PID> /F
```

### Errore: "CORS error"
- CORS è già abilitato in `server.js`
- Assicurati che frontend sia su `http://localhost:3000`

### Errore: "Unauthorized (401)"
- Token scaduto o non valido
- Effettua il login di nuovo

## Note Importanti

- ✅ Questo è un progetto **dimostrativo**
- ✅ Nessuna transazione reale
- ✅ Saldi sono virtuali
- ✅ Per uso reale: implementare pagamenti, KYC, compliance
- ✅ In produzione: usare database reale, HTTPS, rate limiting

## Supporto

Per problemi:
1. Controlla `BackEnd/README.md`
2. Controlla `SETUP.md`
3. Apri DevTools (F12) nel browser
4. Controlla output del terminale backend

## Conclusione

Il backend Java è stato completamente trasformato in un'API Node.js moderna, pronta per essere collegata al frontend. Entrambi i server possono essere avviati simultaneamente e comunicano tramite REST API con autenticazione JWT.

**Pronto per lo sviluppo! 🚀**
