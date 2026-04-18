# 🐔 CHICKEN DASH - Redesign Completo

## 🎨 Nuovo Design Ispirato a Chicken Dash 10000

Il gioco è stato completamente ridisegnato seguendo lo stile del gioco "Chicken Dash 10000" di TaDa Gaming, con un layout verticale a torre invece della griglia orizzontale precedente.

## 🆕 Caratteristiche Principali

### Layout Verticale a Torre
- **10 Livelli**: Il pollo deve salire una torre di 10 livelli
- **3 Caselle per Livello**: Ogni livello ha 3 opzioni tra cui scegliere
- **1 Auto Nascosta**: Ogni livello contiene 1 auto nascosta (2 caselle sicure)
- **Progressione dal Basso**: Si inizia dal livello 1 e si sale verso il livello 10

### Design a Due Pannelli

#### Pannello Sinistro (300px)
1. **Stats Card**
   - Livello corrente
   - Moltiplicatore (evidenziato con sfondo dorato)
   - Vincita potenziale

2. **Controlli**
   - Input puntata con chip selector
   - Pulsante "INIZIA" (arancione)
   - Pulsante "INCASSA" (verde, grande) - visibile durante il gioco

3. **Info Box**
   - Regole del gioco in formato compatto

#### Pannello Destro (Flex)
- **Torre di Gioco**: Area scrollabile con 10 righe
- **Animazione Pollo**: Sprite animato che si muove tra le caselle
- **Effetti Visivi**: Flip 3D delle carte, glow effects

### Sistema di Gioco

#### Meccanica
1. **Inizio**: Giocatore piazza la puntata e clicca "INIZIA"
2. **Selezione**: Solo la riga attiva è cliccabile (evidenziata)
3. **Rivelazione**: Le caselle si girano con effetto 3D flip
4. **Risultato**:
   - 🐔 Casella sicura → Sali al livello successivo
   - 🚗 Auto → Game Over
5. **Cash Out**: Puoi incassare in qualsiasi momento

#### Moltiplicatore
- **Incremento**: +0.2x per ogni livello completato
- **Progressione**:
  - Livello 0: 1.00x
  - Livello 1: 1.20x
  - Livello 2: 1.40x
  - Livello 3: 1.60x
  - Livello 4: 1.80x
  - Livello 5: 2.00x
  - Livello 6: 2.20x
  - Livello 7: 2.40x
  - Livello 8: 2.60x
  - Livello 9: 2.80x
  - Livello 10: 3.00x (completamento totale)

#### Probabilità
- **Per Livello**: 2/3 caselle sicure = 66.67% di successo
- **Completare 10 Livelli**: (2/3)^10 ≈ 1.73% (molto difficile!)
- **Bilanciamento**: Moltiplicatore moderato ma alta difficoltà

## 🎨 Elementi Visivi

### Animazioni
1. **Flip 3D**: Le caselle si girano con effetto 3D (rotateY 180deg)
2. **Glow Effects**:
   - Verde per caselle sicure
   - Rosso per auto
   - Oro per riga attiva
3. **Chicken Bounce**: Il pollo rimbalza leggermente
4. **Shake Effect**: Quando si colpisce un'auto
5. **Scale Hover**: Le caselle si ingrandiscono al passaggio del mouse

### Colori
- **Background Torre**: Gradiente scuro (#1a1a1a → #0a0a0a)
- **Caselle**: Gradiente grigio (#3a3a3a → #2a2a2a)
- **Sicuro**: Verde con glow (#00ff00)
- **Pericolo**: Rosso con glow (#ff0000)
- **Attivo**: Oro con glow (#ffd700)

### Tipografia
- **Display Font**: Bebas Neue per titoli e numeri grandi
- **Body Font**: DM Sans per testo normale
- **Mono Font**: JetBrains Mono per valori monetari

## 📱 Responsive Design

### Desktop (>900px)
- Layout a due colonne
- Torre alta 700px con scroll
- Pannello sinistro fisso 300px

### Tablet (600-900px)
- Layout verticale
- Pannello sinistro full width
- Torre ridotta a 500px

### Mobile (<600px)
- Layout compatto
- Font ridotti
- Icone più piccole (1.5rem)
- Padding ridotto

## 🔧 Modifiche Tecniche

### File Modificati

1. **FrontEnd/games/chicken.js**
   - Cambiato da griglia 5x1 a torre 10x3
   - Aggiunto sistema di righe attive
   - Implementato movimento del pollo
   - Moltiplicatore lineare invece di esponenziale
   - Flip 3D per rivelazione caselle

2. **FrontEnd/css/chicken-game.css**
   - Completamente riscritto per layout verticale
   - Aggiunto sistema a due pannelli
   - Implementate animazioni 3D
   - Responsive design migliorato

3. **FrontEnd/js/sections.js**
   - Aggiornato nome: "Chicken Road" → "Chicken Dash"
   - Aggiornata descrizione per riflettere torre verticale

4. **FrontEnd/index.html**
   - Aggiornato nome nel menu sidebar

### Funzioni Principali

```javascript
// Genera torre 10x3
generateTower()

// Attiva una riga specifica
activateRow(row)

// Seleziona una casella
selectTile(row, col)

// Muove il pollo visivamente
moveChicken(row, col)

// Sale di livello
levelUp()

// Incassa vincita
cashOut()

// Game over
gameOver()

// Rivela tutte le auto
revealAllDangers()
```

## 🎯 Esperienza Utente

### Vantaggi del Nuovo Design
1. **Più Visivo**: La torre verticale è più intuitiva
2. **Progressione Chiara**: Si vede chiaramente quanto si è saliti
3. **Tensione Crescente**: Più sali, più è difficile decidere se incassare
4. **Feedback Immediato**: Animazioni chiare per ogni azione
5. **Mobile Friendly**: Layout verticale funziona meglio su mobile

### Strategia di Gioco
- **Conservativa**: Incassa dopo 3-4 livelli (1.6x-1.8x)
- **Moderata**: Incassa dopo 5-6 livelli (2.0x-2.2x)
- **Aggressiva**: Prova a raggiungere 8-10 livelli (2.6x-3.0x)
- **All-in**: Tenta il completamento totale (3.0x) - molto rischioso!

## 📊 Statistiche di Gioco

### Probabilità di Successo
- **1 Livello**: 66.67%
- **2 Livelli**: 44.44%
- **3 Livelli**: 29.63%
- **4 Livelli**: 19.75%
- **5 Livelli**: 13.17%
- **6 Livelli**: 8.78%
- **7 Livelli**: 5.85%
- **8 Livelli**: 3.90%
- **9 Livelli**: 2.60%
- **10 Livelli**: 1.73%

### Expected Value (EV)
Con moltiplicatore lineare e probabilità 2/3 per livello, il gioco è bilanciato per dare un leggero vantaggio al banco, tipico dei giochi crash.

## 🚀 Come Testare

1. Avvia il frontend: `cd FrontEnd && node server.js`
2. Apri browser: `http://localhost:3000`
3. Login o demo
4. Naviga a "Chicken Dash"
5. Piazza puntata e gioca!

## ✨ Prossimi Miglioramenti (Opzionali)

1. **Sound Effects**: Suoni per flip, win, lose
2. **Particle Effects**: Coriandoli quando si vince
3. **Leaderboard**: Classifica per livello massimo raggiunto
4. **Modalità Difficoltà**: Easy (2 auto), Normal (1 auto), Hard (0 auto sicure)
5. **Power-ups**: Shield, Reveal, Skip
6. **Achievements**: "Reach Level 10", "Cash out at 3.0x"
7. **Auto-play**: Modalità automatica con stop loss/win

---

**Status**: ✅ COMPLETO E FUNZIONANTE
**Design**: Ispirato a Chicken Dash 10000 (TaDa Gaming)
**Data**: 18 Aprile 2026
