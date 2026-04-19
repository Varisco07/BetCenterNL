# BetCenterNL — Casino & Scommesse Virtuali

Centro scommesse virtuale con backend Java, frontend web e gioco da terminale.

---

## Avvio rapido

### Prerequisiti
- **Java JDK 17+** — [Scarica da Adoptium](https://adoptium.net/temurin/releases/?version=17)
- **Node.js** — solo per il frontend web

### 1. Backend Java (porta 8080)
```powershell
cd BackEnd
.\start-java-server.bat
```

### 2. Frontend Web (porta 3000)
```powershell
cd FrontEnd
node server.js
```

Apri il browser su **http://localhost:3000**

### 3. Gioco da terminale (IntelliJ)
Apri il progetto in IntelliJ IDEA e avvia `Main.java`.

---

## Architettura

```
Browser (porta 3000)
    ↓  HTTP fetch
WebServer.java (porta 8080)
    ↓  usa direttamente
Logica Java: Baccarat.java, VideoPoker.java, VirtualCavalli.java, ecc.
Database: BackEnd/data/users.dat + games.dat + jackpot.dat
```

Il database è **condiviso** tra terminale e web — gli utenti creati da terminale sono accessibili dal sito e viceversa.

---

## Giochi disponibili

### Casino
| Gioco | Descrizione |
|-------|-------------|
| 🎰 Slot Machine | 8 simboli, jackpot progressivo condiviso (🔔🔔🔔 = jackpot) |
| 🃏 Blackjack | Hit, Stand, Double. Blackjack paga 3:2 |
| ♠ Video Poker | Jacks or Better. Royal Flush paga 800x |
| ⭕ Roulette | Europea. Numeri, colori, dozzine, colonne |
| 🎲 Dadi (Craps) | Pass Line, Field Bet, Hardways |
| 💎 Baccarat | Giocatore vs Banco. Tie paga 8:1 |
| 🐔 Chicken Road | Attraversa la strada evitando le auto. Moltiplicatore crescente |

### Scommesse Virtuali
| Sport | Descrizione |
|-------|-------------|
| ⚽ Calcio Virtuale | Serie A, Champions, Premier. Quote 1X2 con ForzaSquadra.java |
| 🎾 Tennis Virtuale | ATP virtuale. Quote basate su forza giocatori |
| 🏀 Basket Virtuale | NBA virtuale |
| 🐎 Corse Cavalli | Simulazione live con ForzaCavalli.java (Equinox, Flightline, ecc.) |
| 🐕 Corse Cani | Simulazione live con ForzaCani.java (Romeo Magico, Signet Ace, ecc.) |

---

## Struttura del progetto

```
BetCenterNL/
├── BackEnd/
│   ├── src/
│   │   ├── Main.java              # Entry point terminale
│   │   ├── Simulazione.java       # Simulazione statistica (3 thread)
│   │   ├── server/
│   │   │   └── WebServer.java     # Server HTTP Java (porta 8080)
│   │   ├── core/
│   │   │   ├── Database.java      # Persistenza utenti e storico
│   │   │   ├── User.java          # Modello utente
│   │   │   ├── State.java         # Stato globale + jackpot
│   │   │   ├── Auth.java          # Autenticazione terminale
│   │   │   └── GameRecord.java    # Record partita
│   │   └── games/
│   │       ├── slot/              # SlotMachine.java
│   │       ├── BlackJack/         # Blackjack con mazzo reale
│   │       ├── roulette/          # ruotaRoulette.java
│   │       ├── dadi/              # dadi.java (Craps)
│   │       ├── baccarat/          # Baccarat.java
│   │       ├── poker/             # VideoPoker.java
│   │       ├── chicken/           # ChickenGame.java
│   │       ├── virtual/           # VirtualCalcio + ForzaSquadra
│   │       ├── virtualCavalli/    # VirtualCavalli + ForzaCavalli
│   │       ├── virtualCani/       # VirtualCani + ForzaCani
│   │       ├── virtualBasket/     # VirtualNBA
│   │       └── virtualTennis/     # VirtualTennis
│   ├── data/
│   │   ├── users.dat              # Database utenti (binario Java)
│   │   ├── games.dat              # Storico partite (binario Java)
│   │   └── jackpot.dat            # Jackpot progressivo condiviso
│   └── start-java-server.bat      # Script compilazione + avvio
├── FrontEnd/
│   ├── index.html
│   ├── js/
│   │   ├── api.js                 # Client HTTP → WebServer.java
│   │   ├── auth.js                # Login/registrazione via API
│   │   ├── state.js               # Stato locale + sync server
│   │   ├── sections.js            # Portafoglio, storico, classifica
│   │   ├── simulation.js          # Pagina simulazione web
│   │   └── virtual-sports.js      # Scommesse virtuali
│   ├── games/
│   │   ├── slots.js               # Slot con jackpot dal server
│   │   ├── blackjack.js           # Blackjack via API Java
│   │   ├── roulette.js            # Roulette via API Java
│   │   ├── dadi.js                # Dadi via API Java
│   │   ├── baccarat.js            # Baccarat via API Java
│   │   ├── poker.js               # Poker via API Java
│   │   └── chicken.js             # Chicken via API Java
│   └── server.js                  # Server statico Node.js
└── README.md
```

---

## Database

Il database usa **serializzazione Java binaria** (`ObjectOutputStream`).

- `users.dat` — mappa `email → User` con saldo, statistiche, storico
- `games.dat` — mappa `userId → List<GameRecord>` con tutte le partite
- `jackpot.dat` — valore corrente del jackpot progressivo

Per resettare tutto: cancella i file `.dat` in `BackEnd/data/`.

---

## Jackpot progressivo

- Ogni spin alle slot aggiunge il **3%** della puntata al jackpot
- Tre campane 🔔🔔🔔 = vinci l'intero jackpot
- Il jackpot è **condiviso** tra tutti gli utenti (web + terminale)
- Valore iniziale: **€12.450**

---

## Simulazione statistica

Disponibile sia dal terminale (opzione 19) che dal sito web (sezione Simulazione).

Simula **100 partite** per 3 giochi in **thread paralleli**:
- 🃏 Blackjack — strategia base: stai su 17+
- 🎲 Dadi — Pass Line (vinci con 7 o 11)
- ⭕ Roulette — sempre sul rosso (payout 1.9x)

Dimostra che **alla lunga il banco vince sempre**.

---

## API del server Java

| Metodo | Endpoint | Descrizione |
|--------|----------|-------------|
| POST | `/api/auth/register` | Registrazione |
| POST | `/api/auth/login` | Login |
| GET | `/api/auth/verify` | Verifica token |
| GET | `/api/wallet/balance` | Saldo |
| POST | `/api/wallet/deposit` | Deposito |
| POST | `/api/wallet/daily-bonus` | Bonus giornaliero |
| POST | `/api/games/slots/spin` | Spin slot |
| POST | `/api/games/blackjack/deal` | Distribuisci carte |
| POST | `/api/games/blackjack/resolve` | Risolvi mano |
| POST | `/api/games/roulette/spin` | Gira roulette |
| POST | `/api/games/dadi/roll` | Lancia dadi |
| POST | `/api/games/baccarat/play` | Gioca baccarat |
| POST | `/api/games/poker/deal` | Distribuisci poker |
| POST | `/api/games/poker/draw` | Cambia carte |
| POST | `/api/games/chicken/move` | Muovi pollo |
| POST | `/api/games/chicken/cashout` | Incassa |
| GET | `/api/games/virtual/:sport/generate` | Genera evento virtuale |
| POST | `/api/games/virtual/:sport/bet` | Scommetti su evento |
| POST | `/api/games/virtual/race/preview` | Anteprima corridori |
| POST | `/api/games/virtual/race/simulate` | Simula gara |
| GET | `/api/games/history` | Storico partite |
| GET | `/api/leaderboard` | Classifica |
| GET | `/api/jackpot` | Jackpot attuale |
| GET | `/api/simulation/run` | Esegui simulazione |

---

## Gioco responsabile

Questo è un progetto **dimostrativo** a scopo educativo/tecnico.
Nessuna transazione reale. Il saldo è completamente virtuale.

Il gioco d'azzardo può causare dipendenza. Gioca sempre con consapevolezza.
