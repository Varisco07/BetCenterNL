# 📊 Simulazione Statistica — Documentazione Tecnica

## Panoramica

La classe `Simulazione.java` è un sistema di analisi statistica che dimostra matematicamente il concetto del **vantaggio del banco** (house edge) nei giochi d'azzardo. Esegue 100 partite automatiche per ciascuno dei 3 giochi principali, utilizzando thread paralleli per simulare sessioni di gioco realistiche.

---

## 🎯 Obiettivo

Dimostrare empiricamente che:
> **"Alla lunga il banco vince sempre"**

Anche con strategie ottimali, il giocatore medio perderà denaro nel lungo periodo a causa del vantaggio matematico integrato in ogni gioco.

---

## 🏗️ Architettura

### Thread Paralleli

La simulazione utilizza **3 thread separati** per eseguire le partite in parallelo:

```java
CountDownLatch latch = new CountDownLatch(3);
new Thread(() -> { simulaBlackjack(); latch.countDown(); }, "sim-bj").start();
new Thread(() -> { simulaDadi();      latch.countDown(); }, "sim-dadi").start();
new Thread(() -> { simulaRoulette();  latch.countDown(); }, "sim-rou").start();
```

**Perché thread separati?**
- ✅ Simula sessioni di gioco indipendenti
- ✅ Migliora le performance (esecuzione parallela)
- ✅ Dimostra che il risultato è consistente su più "tavoli" simultanei

Il `CountDownLatch` sincronizza i thread: il programma attende che tutti e 3 i giochi completino le 100 partite prima di mostrare i risultati.

### Variabili Atomiche

Tutti i contatori usano `AtomicInteger` e `AtomicLong` per garantire thread-safety:

```java
private static final AtomicInteger bjVinte = new AtomicInteger();
private static final AtomicLong bjCents = new AtomicLong();
```

**Perché atomiche?**
- I 3 thread scrivono contemporaneamente sui contatori
- Le operazioni atomiche prevengono race condition
- Garantiscono che ogni vittoria/perdita sia contata esattamente una volta

---

## 🎮 Giochi Simulati

### 1. 🎰 Blackjack

**Strategia implementata:** "Stai su 17+"
- Il giocatore pesca carte finché il punteggio è < 17
- Il banco segue le regole standard (pesca fino a 17)

**Logica di simulazione:**

```java
// 1. Distribuisci 2 carte a giocatore e banco
g.aggiungiCarta(mazzo.pescaCarta()); 
b.aggiungiCarta(mazzo.pescaCarta());
g.aggiungiCarta(mazzo.pescaCarta()); 
b.aggiungiCarta(mazzo.pescaCarta());

// 2. Controlla Blackjack naturale (21 con 2 carte)
if (g.haBlackjack() && b.haBlackjack()) { 
    bjPari.incrementAndGet(); 
    continue; 
}
if (g.haBlackjack()) { 
    bjVinte.incrementAndGet(); 
    bjCents.addAndGet(Math.round(BET_INT * 1.5 * 100)); // Payout 3:2
    continue; 
}

// 3. Turno giocatore: pesca fino a 17+
while (g.valoreMano() < 17 && !g.haSballato()) 
    g.aggiungiCarta(mazzo.pescaCarta());

// 4. Se sballato (>21), perde immediatamente
if (g.haSballato()) { 
    bjPerse.incrementAndGet(); 
    bjCents.addAndGet(-BET_INT * 100L); 
    continue; 
}

// 5. Turno banco
while (b.devePescare()) 
    b.aggiungiCarta(mazzo.pescaCarta());

// 6. Confronto punteggi
int pv = g.valoreMano(), dv = b.valoreMano();
if (dv > 21 || pv > dv) { 
    bjVinte.incrementAndGet(); 
    bjCents.addAndGet(BET_INT * 100L); 
}
```

**Dettagli tecnici:**
- Usa un mazzo reale (`MazzoCarte`) che si rimescola quando restano < 15 carte
- Gli assi valgono 11 o 1 (gestito automaticamente da `valoreMano()`)
- Blackjack paga **3:2** (€10 puntati → €15 vinti)
- Ogni partita è indipendente (mazzo rimescolato)

**House Edge teorico:** ~0.5% con strategia base perfetta

---

### 2. 🎲 Dadi (Craps)

**Strategia implementata:** "Pass Line"
- Vinci se il primo lancio è 7 o 11
- Perdi con qualsiasi altro risultato

**Logica di simulazione:**

```java
int sum = random.randomInt(1, 6) + random.randomInt(1, 6);
if (sum == 7 || sum == 11) { 
    dadiVinte.incrementAndGet(); 
    dadiCents.addAndGet(Math.round(BET * 100)); 
}
else { 
    dadiPerse.incrementAndGet(); 
    dadiCents.addAndGet(-Math.round(BET * 100)); 
}
```

**Probabilità:**
- **7:** 6/36 = 16.67% (combinazioni: 1+6, 2+5, 3+4, 4+3, 5+2, 6+1)
- **11:** 2/36 = 5.56% (combinazioni: 5+6, 6+5)
- **Totale vincita:** 8/36 = 22.22%
- **Totale perdita:** 28/36 = 77.78%

**House Edge teorico:** ~55.56% (semplificazione didattica; il vero Craps ha regole più complesse)

**Nota:** Questa è una versione semplificata del Craps. Il gioco reale include "Point" e round multipli, con house edge ~1.4%.

---

### 3. ⭕ Roulette

**Strategia implementata:** "Sempre sul rosso"
- Puntata fissa sul rosso
- Payout: **1.9x** (non 2x per simulare il vantaggio del banco)

**Logica di simulazione:**

```java
ruotaRoulette wheel = new ruotaRoulette();
String color = wheel.getColor(wheel.spin());
if (color.equals("red")) { 
    rouVinte.incrementAndGet(); 
    rouCents.addAndGet(Math.round(BET * 0.9 * 100)); // Vinci €9 su €10 puntati
}
else { 
    rouPerse.incrementAndGet(); 
    rouCents.addAndGet(-Math.round(BET * 100)); 
}
```

**Probabilità (Roulette Europea):**
- **Rosso:** 18/37 = 48.65%
- **Nero:** 18/37 = 48.65%
- **Zero (verde):** 1/37 = 2.70%

**House Edge teorico:** ~2.70% (dovuto allo zero verde)

**Payout ridotto:** Il payout 1.9x invece di 2x simula il fatto che lo zero fa perdere tutte le puntate esterne.

---

## 💰 Gestione del Denaro

### Formato Centesimi

Tutti i calcoli finanziari usano **centesimi** (long) per evitare errori di arrotondamento con i double:

```java
bjCents.addAndGet(Math.round(BET_INT * 1.5 * 100)); // €15 → 1500 centesimi
```

**Conversione finale:**
```java
double bjGain = bjCents.get() / 100.0; // 1500 centesimi → €15.00
```

**Perché centesimi?**
- ✅ Evita errori di precisione floating-point (0.1 + 0.2 ≠ 0.3 in double)
- ✅ Garantisce calcoli finanziari esatti
- ✅ Standard nell'industria finanziaria

---

## 📈 Metriche Calcolate

### ROI (Return on Investment)

```java
double investito = NUM_PARTITE * BET * 3; // 100 partite × €10 × 3 giochi = €3000
double totale = bjGain + dadiGain + rouGain;
double roi = (totale / investito) * 100;
```

**Interpretazione:**
- **ROI = -10%** → Hai perso il 10% del capitale investito
- **ROI = +5%** → Hai guadagnato il 5% (raro, dovuto alla varianza)
- **ROI → negativo** → Più partite giochi, più ti avvicini all'house edge teorico

### Guadagno Netto

```java
String sTotale = String.format("%s€%.2f", totale >= 0 ? "+" : "-", Math.abs(totale));
```

Mostra il risultato finale: quanto hai vinto o perso rispetto all'investimento iniziale.

---

## 🎲 Varianza vs House Edge

### Varianza (Short-term)

Su **100 partite**, la varianza può produrre risultati positivi:
- Un giocatore fortunato può vincere €200
- Un giocatore sfortunato può perdere €500
- La media si avvicina all'house edge teorico

### House Edge (Long-term)

Su **10.000+ partite**, la varianza si annulla:
- Il risultato converge matematicamente verso l'house edge
- Blackjack: -0.5%
- Roulette: -2.7%
- Dadi (semplificato): -55%

**Legge dei grandi numeri:** Più partite giochi, più il risultato si avvicina al valore atteso negativo.

---

## 🔧 Parametri Configurabili

```java
private static final int    NUM_PARTITE = 100;   // Numero di partite per gioco
private static final int    BET_INT     = 10;    // Puntata in euro (int)
private static final double BET         = 10.0;  // Puntata in euro (double)
```

**Esperimenti suggeriti:**
- `NUM_PARTITE = 1000` → Vedi l'house edge convergere
- `BET = 100` → Simula high-roller
- `NUM_PARTITE = 10` → Vedi la varianza dominare

---

## 📊 Output della Simulazione

### Esempio di Output

```
╔══════════════════════════════════════════════════╗
║       📊 RISULTATI SIMULAZIONE                   ║
║  100 partite per gioco  ·  puntata fissa €10    ║
╠══════════════════════════════════════════════════╣
║                                                  ║
║  🎰 BLACKJACK  (strategia: stai su 17+)          ║
║    Vinte: 42   Perse: 48   Pari: 10             ║
║    Guadagno netto: -€60.00                       ║
║                                                  ║
║  🎲 DADI  (Pass Line: vinci con 7 o 11)          ║
║    Vinte: 24   Perse: 76                         ║
║    Guadagno netto: -€520.00                      ║
║                                                  ║
║  ⭕ ROULETTE  (rosso, payout 1.9x)               ║
║    Vinte: 51   Perse: 49                         ║
║    Guadagno netto: -€31.00                       ║
║                                                  ║
╠══════════════════════════════════════════════════╣
║  💸 Totale investito:   €3000.00                 ║
║  💰 Guadagno/Perdita:   -€611.00                 ║
║  📉 ROI:                -20.37%                  ║
╠══════════════════════════════════════════════════╣
║  ⚠️  Alla lunga il banco vince sempre.           ║
║     Gioca solo per divertimento.                 ║
╚══════════════════════════════════════════════════╝
```

### Interpretazione

- **Blackjack:** Risultato vicino al pareggio (house edge basso)
- **Dadi:** Perdita massiccia (versione semplificata con house edge alto)
- **Roulette:** Perdita moderata (house edge ~2.7%)
- **ROI totale:** -20.37% → Hai perso €611 su €3000 investiti

---

## 🧪 Come Usare la Simulazione

### Da Terminale

```bash
cd BackEnd
javac -d out -sourcepath src src/Main.java
java -cp out Main
# Seleziona opzione 19 - Simulazione
```

### Da Codice

```java
Simulazione.avvia();
```

### Da Web API

```
GET http://localhost:8080/api/simulation/run
```

---

## 🎓 Concetti Matematici Dimostrati

### 1. Legge dei Grandi Numeri

Più partite giochi, più il risultato medio converge verso il valore atteso (negativo).

### 2. Vantaggio del Banco (House Edge)

Ogni gioco ha una probabilità matematica che favorisce il banco:
- Blackjack: regole asimmetriche (giocatore sballa per primo)
- Roulette: zero verde (37 numeri, payout su 36)
- Dadi: probabilità sfavorevoli (22% vincita vs 78% perdita)

### 3. Varianza vs Aspettativa

- **Varianza:** Fluttuazioni casuali nel breve periodo
- **Aspettativa:** Risultato matematico nel lungo periodo

**Esempio:**
- 10 partite: puoi vincere €200 (varianza alta)
- 10.000 partite: perderai ~€500 (aspettativa negativa)

### 4. Fallacia del Giocatore

La simulazione dimostra che:
- Ogni partita è **indipendente**
- Le perdite passate **non aumentano** le probabilità di vincita futura
- Non esiste "strategia vincente" contro l'house edge

---

## 🔬 Estensioni Possibili

### 1. Strategie Avanzate

```java
// Blackjack: strategia base completa
if (g.valoreMano() <= 11) g.aggiungiCarta(); // Sempre pesca con ≤11
if (g.valoreMano() == 12 && b.getCarta(0).getValore() >= 4) g.aggiungiCarta();
// ... tabella strategia completa
```

### 2. Più Giochi

```java
private static void simulaPoker() { /* ... */ }
private static void simulaSlot() { /* ... */ }
```

### 3. Grafici

```java
// Salva risultati per ogni partita
List<Double> balanceHistory = new ArrayList<>();
// Genera grafico dell'andamento del saldo
```

### 4. Analisi Statistica

```java
// Calcola deviazione standard
double mean = totale / NUM_PARTITE;
double variance = /* ... */;
double stdDev = Math.sqrt(variance);
```

---

## ⚠️ Limitazioni

### 1. Semplificazioni

- **Dadi:** Versione ultra-semplificata (vero Craps ha regole complesse)
- **Blackjack:** Strategia base fissa (non considera split/insurance)
- **Roulette:** Payout fisso 1.9x (semplificazione didattica)

### 2. Randomness

Usa `java.util.Random` (pseudo-random), non un generatore crittografico.

### 3. Numero di Partite

100 partite sono sufficienti per dimostrare il concetto, ma non per analisi statistica rigorosa (servirebbero 10.000+).

---

## 📚 Riferimenti

- **House Edge:** https://en.wikipedia.org/wiki/Casino_game#House_advantage
- **Legge dei Grandi Numeri:** https://en.wikipedia.org/wiki/Law_of_large_numbers
- **Blackjack Basic Strategy:** https://wizardofodds.com/games/blackjack/strategy/calculator/
- **Roulette Probabilities:** https://en.wikipedia.org/wiki/Roulette#Bet_odds_table

---

## 🎯 Conclusione

La simulazione dimostra empiricamente che:

> **Il gioco d'azzardo è matematicamente sfavorevole al giocatore.**

Anche con strategie ottimali, l'house edge garantisce che il banco vinca nel lungo periodo. Questa è una **certezza matematica**, non un'opinione.

**Messaggio finale:** Gioca solo per divertimento, mai per guadagnare. Il banco vince sempre. 🎰
