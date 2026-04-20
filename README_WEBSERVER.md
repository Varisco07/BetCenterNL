# BetCenterNL - Architettura WebServer

## 📋 Panoramica

BetCenterNL è un'applicazione web full-stack per casino online e scommesse virtuali, composta da un **backend Java** e un **frontend JavaScript** che comunicano tramite API REST.

## 🏗️ Architettura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                        FRONTEND                                 │
│  ┌─────────────────┐    ┌─────────────────┐                    │
│  │   HTML/CSS/JS   │    │   Static Server │                    │
│  │   (Browser)     │    │   (Node.js)     │                    │
│  │   Port: Browser │    │   Port: 3000    │                    │
│  └─────────────────┘    └─────────────────┘                    │
│           │                       │                            │
│           └───────────────────────┘                            │
│                       │                                        │
└───────────────────────┼────────────────────────────────────────┘
                        │ HTTP Requests
                        │ (API Calls)
┌───────────────────────┼────────────────────────────────────────┐
│                       ▼                                        │
│                    BACKEND                                     │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              WebServer.java                             │   │
│  │           (HttpServer - Port 8080)                      │   │
│  │                                                         │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │    Auth     │  │   Games     │  │   Wallet    │     │   │
│  │  │  Handlers   │  │  Handlers   │  │  Handlers   │     │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                       │                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                Core System                              │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │  Database   │  │    State    │  │    Auth     │     │   │
│  │  │   (.dat)    │  │  Manager    │  │  Manager    │     │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │   │
│  └─────────────────────────────────────────────────────────┘   │
│                       │                                        │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │                Game Engines                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐     │   │
│  │  │  Blackjack  │  │   Roulette  │  │    Slots    │     │   │
│  │  │   Poker     │  │   Baccarat  │  │   Virtual   │     │   │
│  │  │    Dadi     │  │   Chicken   │  │   Sports    │     │   │
│  │  └─────────────┘  └─────────────┘  └─────────────┘     │   │
│  └─────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────┘
```

## 🔗 Comunicazione Frontend-Backend

### 1. **Protocollo di Comunicazione**
- **Protocollo**: HTTP/HTTPS
- **Formato**: JSON (Request/Response)
- **Autenticazione**: Bearer Token
- **CORS**: Configurato per sviluppo locale

### 2. **Flusso di Comunicazione**

```javascript
// Frontend (api.js)
const API = {
  BASE_URL: 'http://localhost:8080/api',
  
  async request(method, endpoint, body) {
    const response = await fetch(`${BASE_URL}${endpoint}`, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(body)
    });
    return response.json();
  }
}
```

```java
// Backend (WebServer.java)
public class WebServer {
    private static final int PORT = 8080;
    
    public static void startServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // Registrazione degli endpoint
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/games/slots/spin", new SlotsHandler());
        // ... altri endpoint
        
        server.start();
    }
}
```

## 🛣️ Mappa degli Endpoint API

### **Autenticazione**
```
POST /api/auth/register    - Registrazione utente
POST /api/auth/login       - Login utente  
GET  /api/auth/verify      - Verifica token
```

### **Gestione Utente**
```
GET  /api/user/profile     - Profilo utente
GET  /api/wallet/balance   - Saldo corrente
POST /api/wallet/deposit   - Deposito fondi
POST /api/wallet/daily-bonus - Bonus giornaliero
```

### **Giochi Casino**
```
POST /api/games/slots/spin           - Gira slot machine
POST /api/games/blackjack/deal       - Distribuisci carte blackjack
POST /api/games/blackjack/resolve    - Risolvi mano blackjack
POST /api/games/roulette/spin        - Gira roulette
POST /api/games/dadi/roll           - Lancia dadi
POST /api/games/baccarat/play       - Gioca baccarat
POST /api/games/poker/deal          - Distribuisci poker
POST /api/games/poker/draw          - Pesca carte poker
POST /api/games/chicken/move        - Mossa chicken game
POST /api/games/chicken/cashout     - Incassa chicken game
```

### **Scommesse Virtuali**
```
GET  /api/games/virtual/{sport}/generate  - Genera match virtuale
POST /api/games/virtual/{sport}/bet       - Scommetti su match
POST /api/games/virtual/race/preview      - Anteprima corsa
POST /api/games/virtual/race/simulate     - Simula corsa
```

### **Sistema**
```
GET  /api/games/history    - Storico partite
GET  /api/leaderboard      - Classifica
GET  /api/jackpot          - Jackpot corrente
GET  /api/simulation/run   - Esegui simulazione
GET  /api/health           - Health check
```

## 🔐 Sistema di Autenticazione

### **Flusso di Login**
1. **Frontend** invia credenziali a `/api/auth/login`
2. **Backend** verifica credenziali nel database
3. **Backend** genera token: `"betcenter-" + userId`
4. **Frontend** salva token in localStorage
5. **Frontend** include token in tutte le richieste successive

```javascript
// Frontend - Invio richiesta autenticata
headers: {
  'Authorization': `Bearer betcenter-${userId}`
}
```

```java
// Backend - Verifica token
private static User authUser(HttpExchange ex) {
    String auth = ex.getRequestHeaders().getFirst("Authorization");
    if (auth != null && auth.startsWith("Bearer betcenter-")) {
        String userId = auth.replace("Bearer betcenter-", "");
        return Database.getUserById(userId);
    }
    return null;
}
```

## 💾 Gestione Dati

### **Backend - Persistenza**
- **Database**: File `.dat` (users.dat, games.dat)
- **Formato**: Serializzazione Java
- **Accesso**: Classe `Database.java`
- **Stato**: Classe `State.java` per dati runtime

### **Frontend - Stato**
- **Token**: localStorage
- **Stato App**: Oggetti JavaScript globali
- **Cache**: Nessuna persistenza locale dei dati di gioco

## 🎮 Architettura dei Giochi

### **Separazione delle Responsabilità**

1. **Frontend (UI/UX)**:
   - Interfaccia utente
   - Animazioni e effetti
   - Validazione input
   - Gestione stato UI

2. **Backend (Game Logic)**:
   - Logica di gioco
   - Calcolo vincite
   - Aggiornamento saldo
   - Registrazione storico
   - Generazione numeri casuali

### **Esempio: Slot Machine**

```javascript
// Frontend - Richiesta spin
async function spinSlots(bet) {
  const result = await API.spinSlots(bet);
  // Anima i rulli con il risultato
  animateReels(result.reels);
  // Aggiorna UI
  updateBalance(result.newBalance);
}
```

```java
// Backend - Logica spin
static class SlotsHandler implements HttpHandler {
    public void handle(HttpExchange ex) throws IOException {
        // Genera simboli casuali
        int[] symbols = generateRandomSymbols();
        // Calcola vincita
        double multiplier = calculateWin(symbols);
        // Aggiorna saldo utente
        user.setSaldo(newBalance);
        // Registra nel database
        Database.recordGameResult(user.getId(), gameRecord);
        // Risposta JSON
        ok(ex, jsonResponse);
    }
}
```

## 🚀 Avvio del Sistema

### **1. Backend (Java)**
```bash
cd BackEnd
# Compila
javac -cp . src/**/*.java -d out/
# Avvia
java -cp out Main
# Server attivo su http://localhost:8080
```

### **2. Frontend (Node.js)**
```bash
cd FrontEnd
# Avvia server di sviluppo
node server.js
# Server attivo su http://localhost:3000
```

### **3. Script di Avvio Automatico**
```bash
# Windows
start.bat

# Linux/Mac  
./start.sh
```

## 🔧 Configurazione CORS

Il backend è configurato per accettare richieste dal frontend:

```java
private static void setCors(HttpExchange ex) {
    ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
    ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
}
```

## 📊 Monitoraggio e Debug

### **Logging Backend**
- Console output per richieste HTTP
- Errori di autenticazione
- Operazioni database

### **Logging Frontend**
- Console.log per chiamate API
- Gestione errori con toast notifications
- Debug state management

## 🔒 Sicurezza

### **Validazioni**
- **Frontend**: Validazione input utente
- **Backend**: Validazione server-side (autorità)
- **Database**: Controlli integrità dati

### **Autenticazione**
- Token-based authentication
- Timeout sessione (gestito lato client)
- Validazione token su ogni richiesta

### **Gioco Responsabile**
- Limiti di puntata
- Controllo saldo
- Simulazioni statistiche per educazione

## 🎯 Punti Chiave dell'Architettura

1. **Separazione Netta**: Frontend per UI, Backend per logica
2. **API RESTful**: Comunicazione standardizzata
3. **Stateless Backend**: Ogni richiesta è indipendente
4. **Sicurezza First**: Validazione e autenticazione rigorose
5. **Scalabilità**: Architettura modulare e estendibile
6. **Sviluppo Locale**: Setup semplice per development

Questa architettura garantisce una separazione pulita delle responsabilità, sicurezza nelle transazioni di gioco e un'esperienza utente fluida.