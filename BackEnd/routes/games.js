/**
 * Game routes
 */

const express = require('express');
const router = express.Router();
const { authMiddleware } = require('../utils/auth');
const { getUserById, updateUser, recordGameResult, getGameHistory } = require('../utils/db');
const {
  spinSlots,
  dealBlackjackHand,
  calculateHandValue,
  dealCard,
  spinRoulette,
  checkRouletteWin,
  rollTwoDice,
  generateVirtualMatch
} = require('../utils/games');

// ── SLOT MACHINE ──
router.post('/slots/spin', authMiddleware, (req, res) => {
  try {
    const { bet } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const result = spinSlots();
    let gain = -bet;

    if (result.win) {
      gain = bet * result.multiplier - bet;
    }

    const newBalance = user.balance + gain;
    updateUser(user.email, { balance: newBalance });
    recordGameResult(user.id, {
      game: 'Slot Machine',
      bet,
      result: result.win ? 'win' : 'lose',
      gain,
      reels: result.reels,
      multiplier: result.multiplier
    });

    res.json({
      ok: true,
      reels: result.reels,
      multiplier: result.multiplier,
      win: result.win,
      gain,
      newBalance
    });
  } catch (e) {
    console.error('[GAMES] Slots error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── BLACKJACK ──
router.post('/blackjack/deal', authMiddleware, (req, res) => {
  try {
    const { bet } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const playerHand = dealBlackjackHand();
    const dealerHand = dealBlackjackHand();

    res.json({
      ok: true,
      playerHand,
      dealerHand: [dealerHand[0], '?'],
      playerValue: calculateHandValue(playerHand),
      dealerValue: calculateHandValue([dealerHand[0]]),
      gameId: Date.now().toString()
    });
  } catch (e) {
    console.error('[GAMES] Blackjack error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── ROULETTE ──
router.post('/roulette/spin', authMiddleware, (req, res) => {
  try {
    const { bet, betType, betValue } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const spin = spinRoulette();
    const betObj = { type: betType, value: betValue };
    const { win, multiplier } = checkRouletteWin(betObj, spin);

    let gain = -bet;
    if (win) {
      gain = bet * multiplier - bet;
    }

    const newBalance = user.balance + gain;
    updateUser(user.email, { balance: newBalance });
    recordGameResult(user.id, {
      game: 'Roulette',
      bet,
      result: win ? 'win' : 'lose',
      gain,
      spin,
      multiplier
    });

    res.json({
      ok: true,
      spin,
      win,
      multiplier,
      gain,
      newBalance
    });
  } catch (e) {
    console.error('[GAMES] Roulette error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── DADI (CRAPS) ──
router.post('/dadi/roll', authMiddleware, (req, res) => {
  try {
    const { bet } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const [d1, d2] = rollTwoDice();
    const total = d1 + d2;
    const win = [7, 11].includes(total);
    let gain = -bet;

    if (win) {
      gain = bet;
    }

    const newBalance = user.balance + gain;
    updateUser(user.email, { balance: newBalance });
    recordGameResult(user.id, {
      game: 'Dadi',
      bet,
      result: win ? 'win' : 'lose',
      gain,
      dice: [d1, d2],
      total
    });

    res.json({
      ok: true,
      dice: [d1, d2],
      total,
      win,
      gain,
      newBalance
    });
  } catch (e) {
    console.error('[GAMES] Dadi error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── VIRTUAL SPORTS ──
router.post('/virtual/:sport/match', authMiddleware, (req, res) => {
  try {
    const { sport } = req.params;
    const { bet, prediction } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const match = generateVirtualMatch(sport);
    const win = match.result === prediction;
    let gain = -bet;

    if (win) {
      gain = bet * parseFloat(match.odds[prediction]) - bet;
    }

    const newBalance = user.balance + gain;
    updateUser(user.email, { balance: newBalance });
    recordGameResult(user.id, {
      game: `Virtual ${sport}`,
      bet,
      result: win ? 'win' : 'lose',
      gain,
      match,
      prediction
    });

    res.json({
      ok: true,
      match,
      win,
      gain,
      newBalance
    });
  } catch (e) {
    console.error('[GAMES] Virtual sports error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── BACCARAT ──
router.post('/baccarat/play', authMiddleware, (req, res) => {
  try {
    const { bet, betType } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const result = playBaccarat(bet, bet, bet);
    let gain = -bet;
    let resultType = 'lose';

    if (betType === result.winner) {
      gain = bet * result.payouts[result.winner] - bet;
      resultType = 'win';
    } else if (result.winner === 'tie' && betType !== 'tie') {
      gain = 0;
      resultType = 'push';
    }

    const newBalance = user.balance + gain;
    updateUser(user.email, { balance: newBalance });
    recordGameResult(user.id, {
      game: 'Baccarat',
      bet,
      result: resultType,
      gain,
      winner: result.winner,
      betType
    });

    res.json({
      ok: true,
      playerHand: result.playerHand,
      bankerHand: result.bankerHand,
      playerValue: result.playerValue,
      bankerValue: result.bankerValue,
      winner: result.winner,
      resultType,
      gain,
      newBalance
    });
  } catch (e) {
    console.error('[GAMES] Baccarat error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

// ── VIDEO POKER ──
router.post('/poker/deal', authMiddleware, (req, res) => {
  try {
    const { bet } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!bet || bet < 1) {
      return res.status(400).json({ error: 'Invalid bet', status: 400 });
    }

    if (user.balance < bet) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    const hand = dealPokerHand();
    const evaluation = evaluatePokerHand(hand);

    res.json({
      ok: true,
      hand,
      evaluation,
      gameId: Date.now().toString()
    });
  } catch (e) {
    console.error('[GAMES] Poker error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

router.post('/poker/draw', authMiddleware, (req, res) => {
  try {
    const { bet, hand, held } = req.body;
    const user = getUserById(req.user.id);
    if (!user) return res.status(404).json({ error: 'User not found', status: 404 });

    if (!State.deductBalance(bet)) {
      return res.status(400).json({ error: 'Insufficient balance', status: 400 });
    }

    // Replace non-held cards
    const newHand = [...hand];
    const deck = [];
    for (const s of ['♠','♥','♦','♣']) {
      for (const r of ['2','3','4','5','6','7','8','9','10','J','Q','K','A']) {
        deck.push(`${r}${s}`);
      }
    }
    
    for (let i = 0; i < 5; i++) {
      if (!held[i]) {
        newHand[i] = deck[Math.floor(Math.random() * deck.length)];
      }
    }

    const evaluation = evaluatePokerHand(newHand);
    let gain = -bet;
    let resultType = 'lose';

    if (evaluation.mult > 0) {
      gain = bet * evaluation.mult - bet;
      resultType = 'win';
    }

    const newBalance = user.balance + gain;
    updateUser(user.email, { balance: newBalance });
    recordGameResult(user.id, {
      game: 'Video Poker',
      bet,
      result: resultType,
      gain,
      hand: newHand,
      evaluation: evaluation.name
    });

    res.json({
      ok: true,
      hand: newHand,
      evaluation,
      gain,
      newBalance
    });
  } catch (e) {
    console.error('[GAMES] Poker draw error:', e);
    res.status(500).json({ error: 'Internal server error', status: 500 });
  }
});

module.exports = router;
