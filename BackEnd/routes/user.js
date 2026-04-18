/**
 * User routes
 */

const express = require('express');
const router = express.Router();
const { authMiddleware } = require('../utils/auth');
const { getUserById, updateUser } = require('../utils/db');

// ── GET PROFILE ──
router.get('/profile', authMiddleware, (req, res) => {
  try {
    const user = getUserById(req.user.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found', status: 404 });
    }

    res.json({
      ok: true,
      user: {
        id: user.id,
        email: user.email,
        username: user.username,
        nome: user.nome,
        cognome: user.cognome,
        balance: user.balance,
        xp: user.xp,
        createdAt: user.createdAt,
        achievements: user.achievements || []
      }
    });
  } catch (e) {
    console.error('[USER] Profile error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── UPDATE PROFILE ──
router.put('/profile', authMiddleware, (req, res) => {
  try {
    const { nome, cognome, username } = req.body;
    const user = getUserById(req.user.id);
    if (!user) {
      return res.status(404).json({ error: 'User not found', status: 404 });
    }

    const updates = {};
    if (nome) updates.nome = nome;
    if (cognome) updates.cognome = cognome;
    if (username) updates.username = username;

    const updated = updateUser(user.email, updates);
    res.json({
      ok: true,
      user: {
        id: updated.id,
        email: updated.email,
        username: updated.username,
        nome: updated.nome,
        balance: updated.balance
      }
    });
  } catch (e) {
    console.error('[USER] Update error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
