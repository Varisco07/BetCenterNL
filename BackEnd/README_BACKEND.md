# BetCenterNL — Documentazione Backend (Java)

Questo documento descrive i tratti di codice più complessi e degni di nota presenti nel backend Java del progetto BetCenterNL. Il backend è un server HTTP scritto interamente in Java puro, senza framework esterni.

---

## Struttura del Backend

```
BackEnd/src/
├── core/
│   ├── Auth.java          — Autenticazione CLI (registrazione, login, demo)
│   ├── Database.java      — Persistenza su file binari (.dat)
│   ├── State.java         — Jackpot progressivo e saldo globale
│   ├── User.java          — Modello utente serializzabile
│   └── GameRecord.java    — Record di una singola partita
├── server/
│   └── WebServer.java     — Server HTTP con tutti gli endpoint REST
|
└── games/
    └── ...                — Logica dei singoli giochi
```

---

## 1. `Database.java` — Persistenza Atomica con Backup

### Ricerca dinamica della cartella `data/`

Il metodo `findDataDir()` è uno dei tratti più elaborati del progetto: deve trovare la cartella `data/` in modo affidabile indipendentemente dal punto di avvio del programma (terminale dalla root, IntelliJ, o dal server web).

```java
private static String findDataDir() {
    File cur = new File("").getAbsoluteFile();
    for (int i = 0; i < 8; i++) {
        File candidate1 = new File(cur, "data");
        if (new File(cur, "src").exists() && candidate1.exists())
            return candidate1.getAbsolutePath();

        File candidate2 = new File(cur, "BackEnd" + File.separator + "data");
        if (candidate2.exists()) return candidate2.getAbsolutePath();

        // ... fallback ulteriori risalendo l'albero
        cur = cur.getParentFile();
    }
    // Ultimo fallback: cerca "BetCenterNL" nell'albero
    ...
}
```

L'algoritmo risale l'albero delle directory fino a 8 livelli, testando 3 pattern diversi in ogni livello. Se tutto fallisce, crea la cartella `data/` nella directory corrente.

### Scrittura Atomica con File Temporaneo e Backup

Il salvataggio degli utenti non scrive direttamente sul file, ma usa una strategia a tre fasi per prevenire corruzione dei dati:

```java
public static void saveUsers() {
    File tmpFile = new File(USERS_FILE + ".tmp");
    File target  = new File(USERS_FILE);
    File backup  = new File(USERS_FILE + ".bak");

    // FASE 1: scrive sul file temporaneo
    try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
        oos.writeObject(users);
    } catch (IOException e) {
        tmpFile.delete(); // scrittura fallita: non tocca il file reale
        return;
    }

    // FASE 2: backup dell'esistente
    if (target.exists())
        Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);

    // FASE 3: sostituzione atomica (con fallback se il filesystem non la supporta)
    try {
        Files.move(tmpFile.toPath(), target.toPath(),
            StandardCopyOption.REPLACE_EXISTING,
            StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
        Files.move(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
```

`ATOMIC_MOVE` garantisce che il file risultante sia sempre o il vecchio o il nuovo, mai uno stato intermedio corrotto. Il fallback gestisce i filesystem che non supportano operazioni atomiche.

### Sincronizzazione tra terminale e web

Il metodo `reload()` è `synchronized` e viene chiamato prima di ogni login/registrazione per ricaricare il database dal disco. Questo permette a terminale e web server di condividere gli stessi dati senza conflitti:

```java
public static synchronized void reload() {
    File f = new File(USERS_FILE);
    if (!f.exists()) return;
    loadUsers();
    loadGameHistory();
}
```

### Singola sorgente di verità per la cronologia

Un bug storico (documentato nei commenti `// FIX:`) duplicava ogni record di gioco salvandolo sia in `user.history` (dentro `users.dat`) che in `gameHistory` (dentro `games.dat`). La correzione ha introdotto il metodo `updateStats()` che aggiorna solo i contatori, mentre tutta la history è gestita esclusivamente da `Database.recordGameResult()`:

```java
public static void recordGameResult(String userId, GameRecord record) {
    // Aggiunge il record SOLO in gameHistory — singola sorgente di verità
    gameHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, record);
    User user = getUserById(userId);
    if (user != null) {
        user.updateStats(record); // solo contatori/xp, NON aggiunge a history
        saveUsers();
    }
    saveGameHistory();
}
```

---

## 2. `WebServer.java` — HTTP Server Senza Framework

### Parsing JSON manuale

Poiché il progetto non usa librerie JSON esterne, il server implementa un parser minimale basato su ricerca di stringhe:

```java
private static String jsonStr(String json, String key) {
    String search = "\"" + key + "\"";
    int ki = json.indexOf(search);
    // ... naviga il JSON carattere per carattere
    if (json.charAt(start) == '"') {
        // Valore stringa: trova la virgoletta chiusura
        int end = start + 1;
        while (end < json.length()) {
            if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
            end++;
        }
        return json.substring(start + 1, end);
    } else {
        // Numero/bool/null: legge fino a separatore
        int end = start;
        while (end < json.length() && ",}]\n\r ".indexOf(json.charAt(end)) < 0) end++;
        return json.substring(start, end).trim();
    }
}
```

Lo stesso approccio viene esteso per array (`extractArray()`) e liste di oggetti (es. il parsing delle puntate Roulette in `parseBets()`).

### Autenticazione tramite Bearer Token

Il sistema di autenticazione usa token nel formato `betcenter-{UUID}`. Il metodo `authUser()` li valida senza database di sessioni: il token stesso contiene l'ID utente.

```java
private static User authUser(HttpExchange ex) {
    String auth = ex.getRequestHeaders().getFirst("Authorization");
    if (auth == null) return null;
    if (auth.startsWith("Bearer ")) auth = auth.substring(7).trim();
    if (!auth.startsWith("betcenter-")) return null;
    String id = auth.replace("betcenter-", "");
    return Database.getUserById(id);
}
```

È un approccio stateless: ogni richiesta risolve l'utente direttamente dall'ID nel token, senza bisogno di session store.

### Gestione CORS con pre-flight OPTIONS

Ogni handler chiama `setCors()` e `handleOptions()` per supportare richieste cross-origin dal frontend web:

```java
private static void setCors(HttpExchange ex) {
    ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
    ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
}

private static boolean handleOptions(HttpExchange ex) throws IOException {
    if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
        setCors(ex);
        ex.sendResponseHeaders(204, -1); // 204 No Content
        return true;
    }
    return false;
}
```

### Slot Machine: Pesi e Jackpot Progressivo

La slot machine usa un sistema di pesi ponderati invece di probabilità uniformi, e ogni spin contribuisce al jackpot globale:

```java
private static final int[] WEIGHTS = {20,18,16,14,10,8,6,4}; // più frequente → meno valore
private static final double[] PAYOUTS = {5,8,10,15,25,50,100,200};

private int weightedIdx() {
    int total = 0;
    for (int w : WEIGHTS) total += w;
    int r = rand.nextInt(total);
    for (int i = 0; i < WEIGHTS.length; i++) {
        r -= WEIGHTS[i];
        if (r < 0) return i;
    }
    return 0;
}

// Dopo ogni spin:
if (multiplier == 200) {
    State.resetJackpot(); // jackpot vinto
} else {
    State.addToJackpot(bet); // 3% della puntata va al jackpot
}
```

### Blackjack: Calcolo Valore Mano da JSON

Il metodo `calcHandValueFromJson()` ricostruisce il valore della mano Blackjack direttamente da un array JSON, gestendo correttamente gli assi (valore 11, ridotto a 1 se si sfora 21):

```java
private int calcHandValueFromJson(String arr) {
    int total = 0, aces = 0;
    // Itera su tutti i "rank" nell'array JSON
    while (pos < arr.length()) {
        // ... estrae il rank dalla stringa JSON
        if (rank.equals("Asso") || rank.equals("A")) {
            val = 11; aces++;
        } else if (rank.equals("Jack") || ...) {
            val = 10;
        } else {
            val = Integer.parseInt(rank);
        }
        total += val;
    }
    // Riduce gli assi se si sfora
    while (total > 21 && aces > 0) { total -= 10; aces--; }
    return total;
}
```

### Partite Virtuali: Pattern Generate → Bet con matchId

I giochi virtuali (calcio, basket, tennis, corse) usano un pattern a due fasi per mantenere la consistenza tra le quote mostrate e il risultato:

1. **`/generate`** — genera la partita, la salva in `pendingMatches` con un UUID, restituisce quote e `matchId`
2. **`/bet`** — riceve il `matchId`, preleva il risultato già determinato, calcola la vincita

```java
// In handleGenerate():
String matchId = UUID.randomUUID().toString();
pendingMatches.put(matchId, new Object[]{home, away, homeOdd, drawOdd, awayOdd, result, sport});

// In handleBet():
Object[] pending = pendingMatches.remove(matchId); // remove = consume once
if (pending == null) { err(ex, 400, "Match not found or expired"); return; }
String result = (String) pending[5];
boolean win = result.equals(normalizedPrediction);
```

Il `remove()` garantisce che ogni match sia scommettibile una sola volta. La pulizia automatica rimuove i match più vecchi di 10 minuti.

### Simulazione Race con Frame Animazione

Il `VirtualRaceSimulateHandler` simula la corsa frame per frame, registrando la progressione di ogni corridore per animarla lato frontend:

```java
while (winnerIdx == -1) {
    for (int i = 0; i < selected.size(); i++) {
        // Avanzamento basato sulla forza del corridore + componente casuale
        int step = BASE_STEP + rand.nextInt(RAND_RANGE) + (strengths[i] - BASE_STRENGTH) / 6;
        distance[i] = Math.min(RACE_LENGTH, distance[i] + Math.max(1, step));
    }
    // Salva snapshot percentuale (0-100%) di ogni corridore
    frames.add("[" + percentages + "]");

    for (int i = 0; i < selected.size(); i++)
        if (distance[i] >= RACE_LENGTH && winnerIdx == -1) winnerIdx = i;
}
```

Il risultato include l'array `frames` completo, permettendo al frontend di riprodurre la corsa con animazione fluida.

---

## 3. `State.java` — Jackpot Progressivo Thread-Safe

Il jackpot è un valore condiviso tra tutti i thread del server (8 thread nel pool). I metodi che lo modificano sono `synchronized` per evitare race condition:

```java
public static synchronized void addToJackpot(double betAmount) {
    jackpot += betAmount * 0.03;
    jackpot = Math.round(jackpot * 100.0) / 100.0;
    saveJackpot(); // persiste su file ad ogni spin
}

public static synchronized void resetJackpot() {
    jackpot = JACKPOT_START;
    saveJackpot();
}
```

La persistenza su file ad ogni spin (`saveJackpot()`) garantisce che il valore del jackpot sopravviva a riavvii del server.

---

## 4. `Auth.java` — Gestione Demo Utente

Un bug storico ricreava l'utente demo a ogni sessione, accumulando duplicati nel database. La correzione controlla prima se l'utente demo esiste già:

```java
private static void demoLogin() {
    // FIX: in precedenza veniva registrato un nuovo utente demo ad ogni sessione.
    User demoUser = Database.getUserByEmail("demo@betcenter.nl");
    if (demoUser == null) {
        demoUser = new User("Demo", "Player", "demo", "demo@betcenter.nl", "demo123", "1990-01-01");
        Database.registerUser(demoUser);
    }
    currentUser = demoUser;
}
```

---

## 5. `User.java` — Serializzazione e Livelli

La classe `User` implementa `Serializable` con un `serialVersionUID` esplicito, fondamentale per garantire compatibilità del file `users.dat` tra versioni diverse del codice:

```java
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    ...
}
```

Il sistema di livelli è calcolato dinamicamente dagli XP accumulati, senza campi aggiuntivi nel database:

```java
public int getCurrentLevel() {
    if (xp < 500)   return 1;  // Novizio
    if (xp < 1500)  return 2;  // Apprendista
    if (xp < 4000)  return 3;  // Giocatore
    if (xp < 10000) return 4;  // Veterano
    if (xp < 25000) return 5;  // Esperto
    if (xp < 75000) return 6;  // Campione
    return 7;                   // Leggenda
}
```

---

## Note Generali

- **Nessuna libreria esterna**: il server usa solo la JDK standard (`com.sun.net.httpserver`, `java.io`, `java.nio`). Tutta la serializzazione JSON è manuale.
- **Thread pool fisso**: il server usa `Executors.newFixedThreadPool(8)` per gestire la concorrenza, il che rende critici i metodi `synchronized` su `State`.
- **Password in chiaro**: le password sono attualmente salvate in chiaro nel database. In un ambiente di produzione andrebbero hashate (es. con BCrypt).
- **Token non firmati**: il token `betcenter-{UUID}` non è firmato crittograficamente, quindi chiunque conosca un UUID utente può autenticarsi. In produzione andrebbe usato un JWT firmato.
