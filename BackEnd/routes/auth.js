/**
 * Authentication routes
 */

const express = require('express');
const router = express.Router();
const { generateToken, authMiddleware } = require('../utils/auth');
const { getUser, createUser, getUserById } = require('../utils/db');

// ── REGISTER ──
router.post('/register', (req, res) => {
  try {
    const { nome, cognome, username, email, password, dob } = req.body;

    if (!email || !password || !username) {
      return res.status(400).json({ error: 'Missing required fields', status: 400 });
    }

    // Age check
    if (dob) {
      const age = (new Date() - new Date(dob)) / (365.25 * 24 * 60 * 60 * 1000);
      if (age < 18) {
        return res.status(400).json({ error: 'Must be 18 or older', status: 400 });
      }
    }

    const existing = getUser(email);
    if (existing) {
      return res.status(409).json({ error: 'Email already registered', status: 409 });
    }

    const user = createUser({ nome, cognome, username, email, password, dob });
    if (!user) {
      return res.status(500).json({ error: 'Failed to create user', status: 500 });
    }

    const token = generateToken(user);
    res.status(201).json({
      ok: true,
      user: {
        id: user.id,
        email: user.email,
        username: user.username,
        nome: user.nome,
        balance: user.balance
      },
      token
    });
  } catch (e) {
    console.error('[AUTH] Register error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── LOGIN ──
router.post('/login', (req, res) => {
  try {
    const { email, password } = req.body;

    if (!email || !password) {
      return res.status(400).json({ error: 'Email and password required', status: 400 });
    }

    const user = getUser(email);
    if (!user || user.password !== password) {
      return res.status(401).json({ error: 'Invalid credentials', status: 401 });
    }

    const token = generateToken(user);
    res.json({
      ok: true,
      user: {
        id: user.id,
        email: user.email,
        username: user.username,
        nome: user.nome,
        balance: user.balance
      },
      token
    });
  } catch (e) {
    console.error('[AUTH] Login error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── VERIFY TOKEN ──
router.get('/verify', authMiddleware, (req, res) => {
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
        balance: user.balance
      }
    });
  } catch (e) {
    console.error('[AUTH] Verify error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
