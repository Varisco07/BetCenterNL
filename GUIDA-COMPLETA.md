# 🎰 BetCenterNL - Guida Completa

## 🚀 Avvio Rapido

### Opzione 1: Sistema Completo (Frontend + Backend API)

```bash
# 1. Installa dipendenze
npm run install-all

# 2. Avvia tutto
npm start
```

Poi apri: **http://localhost:3000**

### Opzione 2: Solo Frontend (Demo)

```bash
cd FrontEnd
# Apri index.html con Live Server in VSCode
```

### Opzione 3: Backend Java (Console)

```bash
cd BackEnd
javac -d out src/**/*.java
java -cp out Main
```

## 🎨 Miglioramenti Grafici Implementati

### ✨ Slot Machine Premium
- **Jackpot Display**: Animato con effetto shine dorato
- **Frame 3D**: Cornice con luci lampeggianti
- **Rulli Migliorati**: Effetto vetro e shadow 3D
- **Animazioni**: Blur durante lo spin, pop al risultato
- **Paytable Colorata**: Categorie visive (oro, viola, blu, verde)
- **Bottoni Premium**: Gradiente brillante con effetto shine
- **Luci Decorative**: 5 luci che lampeggiano in sequenza

### 📡 Live Ticker Migliorato
- **Larghezza Completa**: Occupa tutta la riga centrale
- **Sfondo Gradiente**: Effetto visivo accattivante
- **Label Animata**: Pulse rosso brillante
- **Testo Scorrevole**: Animazione fluida 40s
- **Bordo Luminoso**: Glow rosso sottile

### 🎯 UI Generale
- **Game Cards**: Effetto shine al hover, lift animation
- **Balance Display**: Sfondo azzurro con glow
- **Bottoni**: Ripple effect al click
- **Result Banners**: Entrance animation + shine effect
- **Sidebar**: Indicatore laterale animato
- **Input Fields**: Focus glow azzurro
- **Scrollbar**: Gradiente azzurro personalizzato

### 🎲 Roulette
- **Ruota Orizzontale**: Numeri che scorrono
- **Animazione Fluida**: 3 secondi con easing
- **Numeri Ripetuti**: Sempre visibili
- **Indicatore Centrale**: Freccia che punta al risultato
- **Reset Automatico**: Torna alla posizione iniziale

### 🏇 Corse (Cavalli e Cani)
- **Podio Animato**: Top 3 con medaglie
- **Classifica Completa**: Tutti i partecipanti
- **Emoji Specifiche**: 🐎 per cavalli, 🐕 per cani
- **Animazioni**: Pop-in per podio, slide-in per classifica

## 🎮 Giochi Disponibili

### 🎰 Casino
1. **Slot Machine** - 8 simboli, jackpot progressivo, auto-spin
2. **Blackjack** - Classico 21, hit/stand/double
3. **Video Poker** - Jacks or Better, Royal Flush 800x
4. **Roulette** - Europea, tutti i tipi di scommessa
5. **Dadi/Craps** - Pass Line, Field Bet, Hardways
6. **Baccarat** - Giocatore/Banco/Pareggio

### ⚽ Virtual Sports
1. **Calcio** - Serie A, Champions, Premier
2. **Tennis** - 4 superfici, 12 giocatori
3. **Basket** - NBA virtuale, spread e totale
4. **Corse Cavalli** - 8 cavalli con podio
5. **Corse Cani** - 8 greyhound con podio

## 🎨 Palette Colori

```css
--accent: #00d4ff      /* Azzurro principale */
--green: #00ff88       /* Vincite */
--red: #ff3366         /* Perdite/Live */
--gold: #ffd700        /* Jackpot/Premium */
--bg-0: #0f0f12        /* Sfondo più scuro */
--bg-1: #131318        /* Sfondo principale */
--bg-2: #1a1a22        /* Sfondo cards */
--bg-3: #252530        /* Sfondo inputs */
```

## 🔧 Tecnologie

- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Backend**: Java (console) + Node.js/Express (API)
- **Database**: JSON file storage
- **Animazioni**: CSS Animations + Transitions
- **Font**: Bebas Neue, DM Sans, JetBrains Mono

## 📱 Responsive Design

- **Desktop**: Layout completo con sidebar
- **Tablet**: Grid adattivo
- **Mobile**: Menu hamburger, layout verticale

## 🎯 Funzionalità Extra

- ✅ Sistema login/registrazione
- ✅ Saldo persistente
- ✅ Sistema livelli (7 livelli)
- ✅ 12 Achievement sbloccabili
- ✅ Jackpot progressivo
- ✅ Live ticker
- ✅ Storico scommesse
- ✅ Classifica giocatori
- ✅ Bonus giornaliero
- ✅ Effetti audio
- ✅ Particelle e VFX
- ✅ Scorciatoie tastiera

## 🎨 Animazioni Implementate

### Slot Machine
- `jackpot-shine`: Effetto shine sul jackpot
- `jackpot-pulse`: Pulse del valore jackpot
- `light-blink`: Luci che lampeggiano
- `light-blink-fast`: Luci veloci durante spin
- `button-shine`: Shine sul bottone GIRA
- `symbol-pop`: Pop del simbolo finale
- `blur-spin`: Blur durante rotazione
- `winning-glow`: Glow verde quando si vince
- `auto-pulse`: Pulse rosso per AUTO SPIN

### UI Generale
- `card-shine`: Shine sulle game card
- `ticker-pulse`: Pulse del label LIVE
- `ticker-move`: Scorrimento del ticker
- `banner-entrance`: Entrance dei result banner
- `banner-shine`: Shine sui banner
- `toast-slide-in`: Slide-in delle notifiche
- `glow-pulse`: Pulse del glow
- `float`: Floating animation
- `shake`: Shake animation
- `spin`: Loading spinner

### Roulette
- `result-number-pop`: Pop del numero risultato
- `result-glow`: Glow del risultato
- `banner-slide`: Slide dei banner

### Corse
- `podium-pop`: Pop del podio
- `medal-bounce`: Bounce delle medaglie
- `standings-slide`: Slide della classifica
- `row-slide-in`: Slide-in delle righe

## 🎯 Prossimi Miglioramenti Possibili

- [ ] WebSocket per live updates real-time
- [ ] Multiplayer games
- [ ] Tornei e competizioni
- [ ] Sistema di referral
- [ ] Notifiche push
- [ ] Più giochi (Poker multiplayer, Bingo, Keno)
- [ ] Statistiche avanzate
- [ ] Grafici delle performance
- [ ] Temi personalizzabili
- [ ] Modalità scura/chiara

## 📝 Note

- Progetto **dimostrativo** a scopo educativo
- Nessuna transazione reale
- Saldi virtuali
- Gioco responsabile

## 🏆 Credits

Sviluppato con ❤️ per BetCenterNL
Design moderno e premium
Animazioni fluide e accattivanti

---

**Buon divertimento! 🎰🎲🃏**
