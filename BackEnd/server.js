const express = require('express');
const cors = require('cors');
const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

const app = express();
const PORT = process.env.PORT || 3001;

// Middleware
app.use(cors());
app.use(express.json());

// Simulazione database semplice (JSON files)
const DATA_DIR = path.join(__dirname, 'data');
if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR);
}

const USERS_FILE = path.join(DATA_DIR, 'users.json');
const GAMES_FILE = path.join(DATA_DIR, 'games.json');

// Inizializza files se non esistono
if (!fs.existsSync(USERS_FILE)) {
    fs.writeFileSync(USERS_FILE, JSON.stringify([]));
}
if (!fs.existsSync(GAMES_FILE)) {
    fs.writeFileSync(GAMES_FILE, JSON.stringify([]));
}

// Helper functions
function readUsers() {
    return JSON.parse(fs.readFileSync(USERS_FILE, 'utf8'));
}

function writeUsers(users) {
    fs.writeFileSync(USERS_FILE, JSON.stringify(users, null, 2));
}

function readGames() {
    return JSON.parse(fs.readFileSync(GAMES_FILE, 'utf8'));
}

function writeGames(games) {
    fs.writeFileSync(GAMES_FILE, JSON.stringify(games, null, 2));
}

// Auth endpoints
app.post('/api/auth/register', (req, res) => {
    const { nome, cognome, username, email, password, dob } = req.body;
    
    const users = readUsers();
    
    // Check if user exists
    if (users.find(u => u.email === email || u.username === username)) {
        return res.status(400).json({ error: 'Utente già esistente' });
    }
    
    const newUser = {
        id: Date.now().toString(),
        nome,
        cognome,
        username,
        email,
        password, // In produzione: hash con bcrypt
        dob,
        saldo: 1000, // Saldo iniziale
        xp: 0,
        level: 1,
        achievements: [],
        gamesPlayed: 0,
        gamesWon: 0,
        createdAt: new Date().toISOString()
    };
    
    users.push(newUser);
    writeUsers(users);
    
    res.json({ 
        success: true, 
        user: { ...newUser, password: undefined },
        token: 'fake-jwt-token-' + newUser.id
    });
});

app.post('/api/auth/login', (req, res) => {
    const { email, password } = req.body;
    
    const users = readUsers();
    const user = users.find(u => u.email === email && u.password === password);
    
    if (!user) {
        return res.status(401).json({ error: 'Credenziali non valide' });
    }
    
    res.json({
        success: true,
        user: { ...user, password: undefined },
        token: 'fake-jwt-token-' + user.id
    });
});

// User endpoints
app.get('/api/user/profile', (req, res) => {
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const users = readUsers();
    const user = users.find(u => u.id === userId);
    
    if (!user) {
        return res.status(401).json({ error: 'Token non valido' });
    }
    
    res.json({ ...user, password: undefined });
});

// Games endpoints
app.post('/api/games/slots/spin', (req, res) => {
    const { bet } = req.body;
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const users = readUsers();
    const user = users.find(u => u.id === userId);
    
    if (!user || user.saldo < bet) {
        return res.status(400).json({ error: 'Saldo insufficiente' });
    }
    
    // Simulazione slot semplice
    const symbols = ['🍒', '🍋', '⭐', '💎', '🔔', '🍀', '7️⃣', '💰'];
    const reels = [
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)],
        symbols[Math.floor(Math.random() * symbols.length)]
    ];
    
    let multiplier = 0;
    if (reels[0] === reels[1] && reels[1] === reels[2]) {
        // Tre uguali
        multiplier = reels[0] === '💰' ? 10 : 5;
    } else if (reels[0] === reels[1] || reels[1] === reels[2] || reels[0] === reels[2]) {
        // Due uguali
        multiplier = 2;
    }
    
    const win = bet * multiplier;
    const gain = win - bet;
    
    // Aggiorna saldo
    user.saldo += gain;
    user.gamesPlayed++;
    if (gain > 0) user.gamesWon++;
    
    writeUsers(users);
    
    // Salva nel log giochi
    const games = readGames();
    games.push({
        userId,
        game: 'slots',
        bet,
        win,
        gain,
        timestamp: new Date().toISOString()
    });
    writeGames(games);
    
    res.json({
        reels,
        bet,
        win,
        gain,
        balance: user.saldo,
        multiplier
    });
});

app.post('/api/games/blackjack/deal', (req, res) => {
    const { bet } = req.body;
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const users = readUsers();
    const user = users.find(u => u.id === userId);
    
    if (!user || user.saldo < bet) {
        return res.status(400).json({ error: 'Saldo insufficiente' });
    }
    
    // Simulazione blackjack semplice
    const cards = ['A', '2', '3', '4', '5', '6', '7', '8', '9', '10', 'J', 'Q', 'K'];
    const getCard = () => cards[Math.floor(Math.random() * cards.length)];
    const getValue = (card) => {
        if (card === 'A') return 11;
        if (['J', 'Q', 'K'].includes(card)) return 10;
        return parseInt(card);
    };
    
    const playerCards = [getCard(), getCard()];
    const dealerCards = [getCard(), getCard()];
    
    let playerValue = playerCards.reduce((sum, card) => sum + getValue(card), 0);
    let dealerValue = getValue(dealerCards[0]); // Solo prima carta visibile
    
    // Aggiusta Assi
    if (playerValue > 21 && playerCards.includes('A')) {
        playerValue -= 10;
    }
    
    let result = 'continue';
    let gain = 0;
    
    if (playerValue === 21) {
        result = 'blackjack';
        gain = bet * 1.5; // Blackjack paga 3:2
    } else if (playerValue > 21) {
        result = 'bust';
        gain = -bet;
    }
    
    if (result !== 'continue') {
        user.saldo += gain;
        user.gamesPlayed++;
        if (gain > 0) user.gamesWon++;
        writeUsers(users);
        
        const games = readGames();
        games.push({
            userId,
            game: 'blackjack',
            bet,
            win: gain > 0 ? bet + gain : 0,
            gain,
            timestamp: new Date().toISOString()
        });
        writeGames(games);
    }
    
    res.json({
        playerCards,
        dealerCards: [dealerCards[0], '?'], // Seconda carta nascosta
        playerValue,
        dealerValue,
        result,
        gain,
        balance: user.saldo,
        gameId: Date.now().toString()
    });
});

app.post('/api/games/roulette/spin', (req, res) => {
    const { bet, betType, betValue } = req.body;
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const users = readUsers();
    const user = users.find(u => u.id === userId);
    
    if (!user || user.saldo < bet) {
        return res.status(400).json({ error: 'Saldo insufficiente' });
    }
    
    const number = Math.floor(Math.random() * 37); // 0-36
    const isRed = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36].includes(number);
    const isBlack = number > 0 && !isRed;
    
    let win = false;
    let multiplier = 0;
    
    switch (betType) {
        case 'number':
            if (number === betValue) {
                win = true;
                multiplier = 35;
            }
            break;
        case 'red':
            if (isRed) {
                win = true;
                multiplier = 1;
            }
            break;
        case 'black':
            if (isBlack) {
                win = true;
                multiplier = 1;
            }
            break;
        case 'even':
            if (number > 0 && number % 2 === 0) {
                win = true;
                multiplier = 1;
            }
            break;
        case 'odd':
            if (number % 2 === 1) {
                win = true;
                multiplier = 1;
            }
            break;
    }
    
    const winAmount = win ? bet * (multiplier + 1) : 0;
    const gain = winAmount - bet;
    
    user.saldo += gain;
    user.gamesPlayed++;
    if (gain > 0) user.gamesWon++;
    
    writeUsers(users);
    
    const games = readGames();
    games.push({
        userId,
        game: 'roulette',
        bet,
        win: winAmount,
        gain,
        timestamp: new Date().toISOString()
    });
    writeGames(games);
    
    res.json({
        number,
        color: number === 0 ? 'green' : (isRed ? 'red' : 'black'),
        win,
        winAmount,
        gain,
        balance: user.saldo,
        multiplier
    });
});

// Wallet endpoints
app.get('/api/wallet/balance', (req, res) => {
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const users = readUsers();
    const user = users.find(u => u.id === userId);
    
    if (!user) {
        return res.status(401).json({ error: 'Token non valido' });
    }
    
    res.json({ balance: user.saldo });
});

app.post('/api/wallet/deposit', (req, res) => {
    const { amount } = req.body;
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const users = readUsers();
    const user = users.find(u => u.id === userId);
    
    if (!user) {
        return res.status(401).json({ error: 'Token non valido' });
    }
    
    if (amount < 1 || amount > 10000) {
        return res.status(400).json({ error: 'Importo non valido (€1-€10.000)' });
    }
    
    user.saldo += amount;
    writeUsers(users);
    
    res.json({ 
        success: true, 
        newBalance: user.saldo,
        deposited: amount
    });
});

// Leaderboard
app.get('/api/leaderboard', (req, res) => {
    const users = readUsers();
    const leaderboard = users
        .map(u => ({
            username: u.username,
            saldo: u.saldo,
            gamesPlayed: u.gamesPlayed,
            gamesWon: u.gamesWon,
            winRate: u.gamesPlayed > 0 ? (u.gamesWon / u.gamesPlayed * 100).toFixed(1) : '0.0'
        }))
        .sort((a, b) => b.saldo - a.saldo)
        .slice(0, 100);
    
    res.json(leaderboard);
});

// Games history
app.get('/api/games/history', (req, res) => {
    const token = req.headers.authorization?.replace('Bearer ', '');
    const userId = token?.replace('fake-jwt-token-', '');
    
    const games = readGames();
    const userGames = games
        .filter(g => g.userId === userId)
        .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
        .slice(0, 50);
    
    res.json(userGames);
});

// Health check
app.get('/api/health', (req, res) => {
    res.json({ status: 'OK', timestamp: new Date().toISOString() });
});

// Start server
app.listen(PORT, () => {
    console.log(`🚀 BetCenterNL API Server running on http://localhost:${PORT}`);
    console.log(`📁 Data directory: ${DATA_DIR}`);
});