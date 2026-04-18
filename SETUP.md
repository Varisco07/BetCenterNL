# BetCenterNL — Setup Completo

Guida per avviare il progetto completo (Frontend + Backend).

## Prerequisiti

- Node.js >= 14.0.0
- npm o yarn

## Installazione

### 1. Backend

```bash
cd BackEnd
npm install
npm start
```

Il backend sarà disponibile su: **http://localhost:3001**

### 2. Frontend

In un nuovo terminale:

```bash
cd FrontEnd
npm install
npm start
```

Il frontend sarà disponibile su: **http://localhost:3000**

## Architettura

```
┌─────────────────────────────────────────────────────────────┐
│                    BROWSER (http://localhost:3000)          │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  FrontEnd (HTML/CSS/JavaScript)                      │  │
│  │  - UI Components                                     │  │
│  │  - Game Logic (Client-side)                          │  │
│  │  - State Management (localStorage)                   │  │
│  │  - API Client (js/api.js)                            │  │
│  └──────────────────────────────────────────────────────┘  │
│                          ↓ HTTP/REST                        │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│              BACKEND (http://localhost:3001)                │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Express.js API Server                               │  │
│  │  - Authentication (JWT)                              │  │
│  │  - User Management                                   │  │
│  │  - Game Logic (Server-side validation)               │  │
│  │  - Wallet Management                                 │  │
│  │  - Leaderboard                                       │  │
│  │  - JSON Database (data/users.json, data/games.json)  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## Flusso di Autenticazione

1. **Registrazione/Login** (Frontend)
   - Utente compila form
   - Frontend invia POST a `/api/auth/register` o `/api/auth/login`
   - Backend valida e restituisce JWT token
   - Frontend salva token in localStorage

2. **Richieste Autenticate**
   - Frontend include header: `Authorization: Bearer <token>`
   - Backend verifica token
   - Se valido, processa richiesta
   - Se scaduto, frontend reindirizza a login

## Flusso di Gioco (Esempio: Slot Machine)

1. **Frontend**: Utente clicca "GIRA!"
2. **Frontend**: Chiama `API.spinSlots(bet)`
3. **Frontend**: Invia POST a `/api/games/slots/spin` con puntata
4. **Backend**: 
   - Verifica autenticazione
   - Controlla saldo utente
   - Genera risultato slot
   - Aggiorna saldo
   - Registra storico
   - Restituisce risultato
5. **Frontend**: Riceve risultato, aggiorna UI, mostra vincita/perdita

## Struttura File

```
BetCenterNL/
├── FrontEnd/
│   ├── index.html              # Pagina principale
│   ├── server.js               # Server di sviluppo (Node.js)
│   ├── package.json
│   ├── css/                    # Stili
│   ├── js/
│   │   ├── api.js             # ← CLIENT API (nuovo)
│   │   ├── state.js           # Gestione stato
│   │   ├── auth.js            # Autenticazione
│   │   ├── main.js            # Controller principale
│   │   ├── ui.js              # Utilità UI
│   │   └── ...
│   └── games/                  # Logica giochi
│
├── BackEnd/
│   ├── server.js               # Entry point API
│   ├── package.json
│   ├── .env                    # Configurazione
│   ├── utils/
│   │   ├── db.js              # Database (JSON)
│   │   ├── auth.js            # JWT
│   │   └── games.js           # Logica giochi
│   ├── routes/
│   │   ├── auth.js            # /api/auth/*
│   │   ├── user.js            # /api/user/*
│   │   ├── games.js           # /api/games/*
│   │   ├── wallet.js          # /api/wallet/*
│   │   └── leaderboard.js     # /api/leaderboard/*
│   ├── data/                   # Database JSON (generato)
│   └── README.md
│
└── SETUP.md                    # Questo file
```

## Configurazione API

Il frontend comunica con il backend tramite `js/api.js`.

**URL di default**: `http://localhost:3001/api`

Per cambiare URL (es. produzione):

```javascript
// In FrontEnd/js/api.js, modifica:
const BASE_URL = 'https://api.betcenter.nl/api';
```

Oppure usa variabile d'ambiente:

```bash
API_URL=https://api.betcenter.nl/api npm start
```

## Endpoints Disponibili

### Autenticazione
- `POST /api/auth/register` — Registrazione
- `POST /api/auth/login` — Login
- `GET /api/auth/verify` — Verifica token

### Profilo
- `GET /api/user/profile` — Profilo utente
- `PUT /api/user/profile` — Aggiorna profilo

### Giochi
- `POST /api/games/slots/spin` — Slot machine
- `POST /api/games/blackjack/deal` — Blackjack
- `POST /api/games/roulette/spin` — Roulette
- `POST /api/games/dadi/roll` — Dadi
- `POST /api/games/virtual/:sport/match` — Scommesse virtuali
- `GET /api/games/history` — Storico

### Portafoglio
- `GET /api/wallet/balance` — Saldo
- `POST /api/wallet/deposit` — Deposita
- `POST /api/wallet/daily-bonus` — Bonus giornaliero

### Classifica
- `GET /api/leaderboard` — Top 100
- `GET /api/leaderboard/rank/:userId` — Rank utente

## Troubleshooting

### "Cannot GET /api/..."
- Assicurati che il backend sia in esecuzione su porta 3001
- Controlla che il frontend stia inviando richieste a `http://localhost:3001`

### "CORS error"
- CORS è già abilitato in `BackEnd/server.js`
- Se il problema persiste, controlla che il frontend sia su `http://localhost:3000`

### "Unauthorized" (401)
- Token scaduto o non valido
- Effettua il login di nuovo
- Controlla che il token sia salvato in localStorage

### Database non trovato
- Il backend crea automaticamente `data/` directory
- Se il problema persiste, crea manualmente: `mkdir BackEnd/data`

## Sviluppo

### Hot Reload
- **Frontend**: Modifica file → Ricarica browser (F5)
- **Backend**: Modifica file → Riavvia server (Ctrl+C, npm start)

### Debug
- **Frontend**: Apri DevTools (F12) → Console
- **Backend**: Controlla output del terminale

### Aggiungere Nuovo Gioco

1. Crea logica in `BackEnd/utils/games.js`
2. Crea route in `BackEnd/routes/games.js`
3. Crea client API in `FrontEnd/js/api.js`
4. Crea UI in `FrontEnd/games/newgame.js`
5. Aggiungi a `FrontEnd/index.html`

## Produzione

Per deployare in produzione:

1. **Backend**:
   - Usa database reale (MongoDB, PostgreSQL)
   - Hash password con bcrypt
   - Implementa rate limiting
   - Usa HTTPS
   - Cambia JWT_SECRET

2. **Frontend**:
   - Build: `npm run build` (se configurato)
   - Deploy su CDN/hosting
   - Aggiorna API_URL

## Note

- Questo è un progetto dimostrativo
- Nessuna transazione reale
- Saldi sono virtuali
- Per uso reale: implementare pagamenti, KYC, compliance, etc.

## Supporto

Per problemi o domande, controlla:
- `BackEnd/README.md` — Documentazione backend
- `FrontEnd/README.md` — Documentazione frontend
- Console del browser (F12)
- Output del terminale backend
