/**
 * Achievements routes
 */

const express = require('express');
const router = express.Router();
const { authMiddleware } = require('../utils/auth');
const { getUserById, updateUser, getGameHistory } = require('../utils/db');

const ACHIEVEMENTS = [
  { id: 'first_win', name: 'Prima Vittoria', icon: '🎉', desc: 'Vinci la tua prima scommessa' },
  { id: 'high_roller', name: 'High Roller', icon: '💸', desc: 'Scommetti €100 in una mano' },
  { id: 'streak_3', name: 'Tre di Fila', icon: '🔥', desc: 'Vinci 3 scommesse consecutive' },
  { id: 'blackjack', name: 'Blackjack!', icon: '🃏', desc: 'Fai un blackjack naturale' },
  { id: 'jackpot', name: 'Jackpottaro', icon: '🎰', desc: 'Vinci più di €500 alle slot' },
  { id: 'poker_royal', name: 'Royal Flush', icon: '♠', desc: 'Ottieni un Royal Flush al poker' },
  { id: 'multi_bet', name: 'Combinatore', icon: '🎲', desc: 'Vinci una multi-scommessa (3+ eventi)' },
  { id: 'total_100', name: 'Centurione', icon: '💯', desc: 'Gioca 100 scommesse totali' },
  { id: 'balance_5000', name: 'Ricco Sfondato', icon: '🤑', desc: 'Raggiungi €5.000 di saldo' },
  { id: 'zero_hero', name: 'Zero Hero', icon: '🎰', desc: 'Indovina lo zero alla roulette' },
  { id: 'race_winner', name: 'Appassionato di Gare', icon: '🏁', desc: 'Vinci una corsa di cavalli' },
  { id: 'baccarat_tie', name: 'Puntatore Audace', icon: '💎', desc: 'Vinci puntando sul pareggio al baccarat' },
];

function checkAchievements(user) {
  const history = getGameHistory(user.id, 1000);
  const unlocked = [];

  // First win
  if (history.some(h => h.result === 'win')) {
    unlocked.push('first_win');
  }

  // High roller
  if (history.some(h => h.bet >= 100)) {
    unlocked.push('high_roller');
  }

  // Streak 3
  let streak = 0;
  for (const h of history) {
    if (h.result === 'win') {
      streak++;
      if (streak >= 3) {
        unlocked.push('streak_3');
        break;
      }
    } else {
      streak = 0;
    }
  }

  // Blackjack
  if (history.some(h => h.game === 'Blackjack' && h.result === 'win' && h.gain > h.bet)) {
    unlocked.push('blackjack');
  }

  // Jackpot
  if (history.some(h => h.game === 'Slot Machine' && h.gain > 500)) {
    unlocked.push('jackpot');
  }

  // Poker royal
  if (history.some(h => h.game === 'Video Poker' && h.gain >= h.bet * 800)) {
    unlocked.push('poker_royal');
  }

  // Multi bet
  if (history.some(h => h.game === 'Virtual Sports' && h.gain > h.bet * 5)) {
    unlocked.push('multi_bet');
  }

  // Total 100
  if (history.length >= 100) {
    unlocked.push('total_100');
  }

  // Balance 5000
  if (user.balance >= 5000) {
    unlocked.push('balance_5000');
  }

  // Zero hero
  if (history.some(h => h.game === 'Roulette' && h.gain > h.bet * 30)) {
    unlocked.push('zero_hero');
  }

  // Race winner
  if (history.some(h => h.game === 'Corse Cavalli' && h.result === 'win')) {
    unlocked.push('race_winner');
  }

  // Baccarat tie
  if (history.some(h => h.game === 'Baccarat' && h.gain >= h.bet * 8)) {
    unlocked.push('baccarat_tie');
  }

  return [...new Set(unlocked)];
}

// ── GET ACHIEVEMENTS ──
router.get('/', authMiddleware, (req, res) => {
  try {
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    const unlockedIds = checkAchievements(user);
    const achievements = ACHIEVEMENTS.map(a => ({
      ...a,
      unlocked: unlockedIds.includes(a.id)
    }));

    res.json({
      ok: true,
      achievements,
      unlockedCount: unlockedIds.length,
      totalCount: ACHIEVEMENTS.length
    });
  } catch (e) {
    console.error('[ACHIEVEMENTS] Error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
