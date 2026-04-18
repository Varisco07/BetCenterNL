/**
 * Leaderboard routes
 */

const express = require('express');
const router = express.Router();
const { loadUsers } = require('../utils/db');
const { getGameHistory } = require('../utils/db');

// ── GET LEADERBOARD ──
router.get('/', (req, res) => {
  try {
    const users = loadUsers();
    const leaderboard = Object.values(users)
      .map(user => {
        const history = getGameHistory(user.id, 1000);
        const wins = history.filter(h => h.result === 'win').length;
        const totalGain = history.reduce((s, h) => s + (h.gain || 0), 0);

        return {
          id: user.id,
          username: user.username,
          nome: user.nome,
          balance: user.balance,
          xp: user.xp,
          wins,
          totalGain,
          gamesPlayed: history.length,
          winRate: history.length > 0 ? ((wins / history.length) * 100).toFixed(1) : 0
        };
      })
      .sort((a, b) => b.balance - a.balance)
      .slice(0, 100);

    res.json({
      ok: true,
      leaderboard
    });
  } catch (e) {
    console.error('[LEADERBOARD] Error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── GET USER RANK ──
router.get('/rank/:userId', (req, res) => {
  try {
    const { userId } = req.params;
    const users = loadUsers();
    const leaderboard = Object.values(users)
      .map(user => {
        const history = getGameHistory(user.id, 1000);
        const wins = history.filter(h => h.result === 'win').length;
        const totalGain = history.reduce((s, h) => s + (h.gain || 0), 0);

        return {
          id: user.id,
          username: user.username,
          balance: user.balance,
          wins,
          totalGain,
          gamesPlayed: history.length
        };
      })
      .sort((a, b) => b.balance - a.balance);

    const rank = leaderboard.findIndex(u => u.id === userId) + 1;
    const user = leaderboard.find(u => u.id === userId);

    if (!user) {
      return res.status(404).json({ error: 'User not found', status: 404 });
    }

    res.json({
      ok: true,
      rank,
      user
    });
  } catch (e) {
    console.error('[LEADERBOARD] Rank error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
