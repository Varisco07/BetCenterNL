# 🎰 BETCENTERNL — Casino & Scommesse Virtuali

Centro scommesse virtuale completo con casino e virtual sports.
Progetto **standalone** — nessuna dipendenza esterna richiesta (solo Node.js).

---

## 🚀 Come avviare in VSCode

### Metodo 1 — Node.js (consigliato)
```bash
# 1. Entra nella cartella
cd BetCenterNL

# 2. Avvia il server
node server.js

# 3. Apri il browser su
http://localhost:3000
```

### Metodo 2 — Live Server (estensione VSCode)
1. Installa l'estensione **Live Server** (Ritwick Dey) in VSCode
2. Tasto destro su `index.html` → **"Open with Live Server"**
3. Il browser si aprirà automaticamente

### Metodo 3 — Apertura diretta
Apri semplicemente `index.html` nel browser (doppio click).
> ⚠️ Alcune funzionalità potrebbero essere limitate dal CORS in modalità `file://`

---

## 🎮 Giochi disponibili

### 🎰 Casino
| Gioco | Descrizione |
|-------|-------------|
| **Slot Machine** | 3 rulli, 8 simboli, jackpot progressivo fino a 200x, auto-spin |
| **Blackjack** | Classico 21, hit/stand/doppio, blackjack paga 3:2 |
| **Video Poker** | Jacks or Better, Royal Flush 800x, tieni le carte migliori |
| **Roulette** | Europea (1 zero), tutti i tipi di scommessa, animazione ruota |
| **Craps/Dadi** | Pass Line, Don't Pass, Field Bet, Hardways |
| **Baccarat** | Giocatore/Banco/Tie, regole ufficiali, terza carta automatica |

### ⚡ Virtual Sports
| Sport | Descrizione |
|-------|-------------|
| **Calcio** | Serie A, Champions, Premier virtuali — quote 1X2 |
| **Tennis** | Tornei su 4 superfici, 12 giocatori virtuali |
| **Basket** | NBA virtuale, spread e totale |
| **Corse Cavalli** | 8 cavalli, gara animata in tempo reale |
| **Corse Cani** | 8 greyhound, simulazione live |

---

## 🧩 Funzionalità extra
- **Sistema di login/registrazione** con localStorage
- **Saldo persistente** tra sessioni
- **Sistema di livelli** (7 livelli: Novizio → Leggenda)
- **12 Traguardi/Achievement** sbloccabili
- **Jackpot progressivo** alle slot
- **Live ticker** con aggiornamenti in tempo reale
- **Effetti visivi**: particelle, numeri flottanti, shake
- **Storico scommesse** con statistiche dettagliate
- **Carnet scommesse** per le virtual sports
- **Scorciatoie da tastiera**: `Enter`, `H`, `S`, `D` (Blackjack)
- **Deposito fondi** in-game
- **Design responsive** (mobile, tablet, desktop)

---

## 📁 Struttura file

```
BetCenterNL/
├── index.html              # Entry point
├── server.js               # Server Node.js locale
├── package.json
├── README.md
├── css/
│   ├── main.css            # Stili principali
│   └── animations.css      # Animazioni & effetti
├── js/
│   ├── state.js            # Gestione stato e persistenza
│   ├── auth.js             # Login / registrazione
│   ├── ui.js               # Utilità UI
│   ├── ticker.js           # Live ticker
│   ├── sections.js         # Lobby, wallet, storico
│   ├── levels.js           # Sistema livelli & achievement
│   ├── vfx.js              # Effetti visivi
│   ├── virtual-sports.js   # Motore scommesse virtuali
│   └── main.js             # Controller principale
└── games/
    ├── slots.js            # Slot machine
    ├── blackjack.js        # Blackjack
    ├── poker.js            # Video Poker
    ├── roulette.js         # Roulette
    ├── dadi.js             # Craps/Dadi
    └── baccarat.js         # Baccarat
```

---

## ⚠️ Disclaimer

Questo è un progetto **dimostrativo** a scopo educativo.
Nessuna transazione reale viene effettuata. Il saldo è virtuale.
Il gioco d'azzardo può causare dipendenza — gioca responsabilmente.

**Requisiti**: Node.js ≥ 14 (solo per il server) oppure qualsiasi browser moderno.
