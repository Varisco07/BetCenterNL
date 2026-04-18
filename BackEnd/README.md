# BetCenterNL — Backend API

Backend Node.js/Express per BetCenterNL — Casino & Virtual Sports Platform.

## Installazione

```bash
cd BackEnd
npm install
```

## Avvio

```bash
npm start
```

Il server sarà disponibile su `http://localhost:3001`

## Struttura

```
BackEnd/
├── server.js              # Entry point principale
├── package.json           # Dipendenze
├── .env                   # Variabili di ambiente
├── utils/
│   ├── db.js             # Database utilities (JSON storage)
│   ├── auth.js           # JWT authentication
│   └── games.js          # Game logic
└── routes/
    ├── auth.js           # Autenticazione (register, login)
    ├── user.js           # Profilo utente
    ├── games.js          # Giochi (slots, blackjack, roulette, etc.)
    ├── wallet.js         # Portafoglio e bonus
    └── leaderboard.js    # Classifica
```

## API Endpoints

### Autenticazione

- `POST /api/auth/register` — Registrazione nuovo utente
- `POST /api/auth/login` — Login
- `GET /api/auth/verify` — Verifica token (richiede Bearer token)

### Profilo Utente

- `GET /api/user/profile` — Profilo utente (richiede autenticazione)
- `PUT /api/user/profile` — Aggiorna profilo

### Giochi

- `POST /api/games/slots/spin` — Gira slot machine
- `POST /api/games/blackjack/deal` — Inizia partita blackjack
- `POST /api/games/roulette/spin` — Gira roulette
- `POST /api/games/dadi/roll` — Lancia dadi
- `POST /api/games/virtual/:sport/match` — Scommessa virtuale
- `GET /api/games/history` — Storico partite

### Portafoglio

- `GET /api/wallet/balance` — Saldo attuale
- `POST /api/wallet/deposit` — Deposita fondi
- `POST /api/wallet/daily-bonus` — Ritira bonus giornaliero

### Classifica

- `GET /api/leaderboard` — Top 100 giocatori
- `GET /api/leaderboard/rank/:userId` — Rank di un giocatore

## Autenticazione

Tutti gli endpoint protetti richiedono un header:

```
Authorization: Bearer <token>
```

Il token viene fornito al login/register e scade dopo 7 giorni.

## Database

Il backend usa JSON file storage (in `./data/`):

- `users.json` — Dati utenti
- `games.json` — Storico partite

In produzione, sostituire con un database reale (MongoDB, PostgreSQL, etc.)

## Variabili di Ambiente

```
PORT=3001
NODE_ENV=development
JWT_SECRET=your_jwt_secret_key_change_in_production
DB_PATH=./data/users.json
GAMES_DB_PATH=./data/games.json
```

## Collegamento con Frontend

Il frontend (porta 3000) comunica con il backend (porta 3001) tramite API REST.

Assicurati che CORS sia abilitato (già configurato in `server.js`).

## Note di Sviluppo

- Questo è un backend dimostrativo
- Nessuna transazione reale
- Saldi sono virtuali
- In produzione: implementare hashing password, database reale, rate limiting, etc.
