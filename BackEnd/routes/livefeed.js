/**
 * Live Feed routes
 */

const express = require('express');
const router = express.Router();
const { loadUsers, getGameHistory } = require('../utils/db');

// In-memory live feed (in production, use database)
let liveActivities = [];

function generateFakeActivity() {
  const FAKE_PLAYERS = [
    'Marco_R', 'Sofia99', 'Gianni_B', 'Laura_V', 'Alex_K',
    'Federica', 'Luca_M', 'Chiara_P', 'Dario_T', 'Elena_S'
  ];

  const ACTIVITIES = [
    (p, a) => ({ msg: `${p} ha vinto ${a} alle Slot! 🎰`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha fatto Blackjack! +${a} 🃏`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha vinto ${a} alla Roulette ⭕`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha vinto ${a} al Video Poker ♠`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha scommesso ${a} sul Calcio Virtuale ⚽`, type: 'bet' }),
    (p, a) => ({ msg: `${p} ha piazzato €${Math.floor(Math.random()*50+10)} sulle Corse Cavalli 🐎`, type: 'bet' }),
    (p, a) => ({ msg: `${p} si è iscritto! Benvenuto 👋`, type: 'info' }),
    (p, a) => ({ msg: `${p} ha raggiunto il Livello ${Math.floor(Math.random()*6+2)} ⭐`, type: 'level' }),
    (p, a) => ({ msg: `${p} ha vinto ${a} al Baccarat 💎`, type: 'win' }),
  ];

  const player = FAKE_PLAYERS[Math.floor(Math.random() * FAKE_PLAYERS.length)];
  const template = ACTIVITIES[Math.floor(Math.random() * ACTIVITIES.length)];
  const amounts = [12, 25, 48, 75, 120, 200, 350, 500, 750, 1200, 2000];
  const amount = `€${amounts[Math.floor(Math.random() * amounts.length)]}`;

  return {
    id: Date.now().toString(),
    msg: template(player, amount).msg,
    type: template(player, amount).type,
    time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
  };
}

// Initialize with some fake activities
for (let i = 0; i < 8; i++) {
  const activity = generateFakeActivity();
  const mins = Math.floor(Math.random() * 30);
  activity.time = new Date(Date.now() - mins * 60000).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
  liveActivities.push(activity);
}

// ── GET LIVE FEED ──
router.get('/', (req, res) => {
  try {
    // Add random fake activity
    if (Math.random() > 0.3) {
      const newActivity = generateFakeActivity();
      liveActivities.unshift(newActivity);
      if (liveActivities.length > 50) {
        liveActivities = liveActivities.slice(0, 50);
      }
    }

    res.json({
      ok: true,
      activities: liveActivities.slice(0, 20)
    });
  } catch (e) {
    console.error('[LIVEFEED] Error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── ADD ACTIVITY (called when user wins) ──
router.post('/add', (req, res) => {
  try {
    const { username, game, amount } = req.body;

    const activity = {
      id: Date.now().toString(),
      msg: `${username} ha vinto €${amount} a ${game}!`,
      type: 'win',
      time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
    };

    liveActivities.unshift(activity);
    if (liveActivities.length > 50) {
      liveActivities = liveActivities.slice(0, 50);
    }

    res.json({ ok: true });
  } catch (e) {
    console.error('[LIVEFEED] Add error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
