/**
 * Wallet routes
 */

const express = require('express');
const router = express.Router();
const { authMiddleware } = require('../utils/auth');
const { getUserById, updateUser } = require('../utils/db');

// ── GET BALANCE ──
router.get('/balance', authMiddleware, (req, res) => {
  try {
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    res.json({
      ok: true,
      balance: user.balance,
      xp: user.xp
    });
  } catch (e) {
    console.error('[WALLET] Balance error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── DEPOSIT ──
router.post('/deposit', authMiddleware, (req, res) => {
  try {
    const { amount } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!amount || amount < 1 || amount > 10000) {
      return res.status(400).json({ error: 'Invalid amount', status: 400 });
    }

    const newBalance = user.balance + amount;
    updateUser(user.email, { balance: newBalance });

    res.json({
      ok: true,
      amount,
      newBalance,
      message: 'Deposit successful'
    });
  } catch (e) {
    console.error('[WALLET] Deposit error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── DAILY BONUS ──
router.post('/daily-bonus', authMiddleware, (req, res) => {
  try {
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    // Check if already claimed today
    const lastBonus = user.lastBonusDate || null;
    const today = new Date().toDateString();

    if (lastBonus === today) {
      return res.status(400).json({ error: 'Bonus already claimed today', status: 400 });
    }

    // Calculate bonus (increases with streak)
    const streak = user.bonusStreak || 0;
    const bonusAmount = Math.min(50 + streak * 10, 500);

    const newBalance = user.balance + bonusAmount;
    updateUser(user.email, {
      balance: newBalance,
      lastBonusDate: today,
      bonusStreak: streak + 1
    });

    res.json({
      ok: true,
      bonusAmount,
      newBalance,
      streak: streak + 1,
      message: 'Daily bonus claimed!'
    });
  } catch (e) {
    console.error('[WALLET] Daily bonus error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
