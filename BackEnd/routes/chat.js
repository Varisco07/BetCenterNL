/**
 * Chat routes
 */

const express = require('express');
const router = express.Router();
const { authMiddleware } = require('../utils/auth');
const { getUserById } = require('../utils/db');

// In-memory chat (in production, use database)
let chatMessages = [];

// ── GET CHAT MESSAGES ──
router.get('/messages', (req, res) => {
  try {
    const limit = parseInt(req.query.limit) || 50;
    res.json({
      ok: true,
      messages: chatMessages.slice(-limit)
    });
  } catch (e) {
    console.error('[CHAT] Get messages error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── SEND MESSAGE ──
router.post('/send', authMiddleware, (req, res) => {
  try {
    const { text } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!text || text.trim().length === 0) {
      return res.status(400).json({ error: 'Message cannot be empty', status: 400 });
    }

    if (text.length > 500) {
      return res.status(400).json({ error: 'Message too long', status: 400 });
    }

    const message = {
      id: Date.now().toString(),
      userId: user.id,
      username: user.username,
      nome: user.nome,
      text: text.trim(),
      timestamp: new Date().toISOString(),
      time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
    };

    chatMessages.push(message);
    if (chatMessages.length > 500) {
      chatMessages = chatMessages.slice(-500);
    }

    res.json({
      ok: true,
      message
    });
  } catch (e) {
    console.error('[CHAT] Send error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── DELETE MESSAGE (own messages only) ──
router.delete('/messages/:messageId', authMiddleware, (req, res) => {
  try {
    const { messageId } = req.params;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    const msgIndex = chatMessages.findIndex(m => m.id === messageId && m.userId === user.id);
    if (msgIndex === -1) {
      return res.status(404).json({ error: 'Message not found', status: 404 });
    }

    chatMessages.splice(msgIndex, 1);
    res.json({ ok: true });
  } catch (e) {
    console.error('[CHAT] Delete error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
