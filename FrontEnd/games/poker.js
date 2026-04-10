// =============================================
// BetCenterNL — VIDEO POKER (Jacks or Better)
// =============================================

const PokerGame = (() => {
  const SUITS = ['♠','♥','♦','♣'];
  const RANKS = ['2','3','4','5','6','7','8','9','10','J','Q','K','A'];
  const RANK_VAL = Object.fromEntries(RANKS.map((r,i) => [r, i+2]));
  const PAYTABLE = [
    { name: 'Royal Flush',      mult: 800 },
    { name: 'Straight Flush',   mult: 50 },
    { name: 'Poker (4 uguali)', mult: 25 },
    { name: 'Full House',       mult: 9 },
    { name: 'Colore (Flush)',   mult: 6 },
    { name: 'Scala (Straight)', mult: 4 },
    { name: 'Tris',             mult: 3 },
    { name: 'Doppia Coppia',    mult: 2 },
    { name: 'Coppia J+',        mult: 1 },
    { name: 'Nessuna Vincita',  mult: 0 },
  ];

  let deck = [], hand = [], held = [false,false,false,false,false], phase = 'idle', currentBet = 0;

  function newDeck() {
    const d = [];
    for (const s of SUITS) for (const r of RANKS) d.push({ rank: r, suit: s });
    for (let i = d.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [d[i], d[j]] = [d[j], d[i]];
    }
    return d;
  }

  function isRed(suit) { return suit === '♥' || suit === '♦'; }

  function evaluateHand(h) {
    const vals = h.map(c => RANK_VAL[c.rank]).sort((a,b) => a-b);
    const suits = h.map(c => c.suit);
    const flush = suits.every(s => s === suits[0]);
    const rankCounts = {};
    vals.forEach(v => rankCounts[v] = (rankCounts[v] || 0) + 1);
    const counts = Object.values(rankCounts).sort((a,b) => b-a);
    const straight = counts[0] === 1 && (vals[4]-vals[0] === 4 || (vals[4]===14 && vals[0]===2 && vals[3]===5));
    const royal = straight && flush && vals[0] >= 10;

    if (royal) return 'Royal Flush';
    if (straight && flush) return 'Straight Flush';
    if (counts[0] === 4) return 'Poker (4 uguali)';
    if (counts[0] === 3 && counts[1] === 2) return 'Full House';
    if (flush) return 'Colore (Flush)';
    if (straight) return 'Scala (Straight)';
    if (counts[0] === 3) return 'Tris';
    if (counts[0] === 2 && counts[1] === 2) return 'Doppia Coppia';
    if (counts[0] === 2) {
      const pairVal = parseInt(Object.entries(rankCounts).find(([,v]) => v===2)[0]);
      if (pairVal >= 11) return 'Coppia J+';
    }
    return 'Nessuna Vincita';
  }

  function getMult(handName) {
    return PAYTABLE.find(p => p.name === handName)?.mult || 0;
  }

  function cardHTML(card, idx) {
    const color = isRed(card.suit) ? 'red' : 'black';
    const heldClass = held[idx] ? 'held' : '';
    return `<div class="poker-card-col">
      <div class="playing-card ${color} ${heldClass}" onclick="PokerGame.toggleHold(${idx})" style="cursor:pointer;${held[idx] ? 'box-shadow:0 0 12px var(--accent);' : ''}">
        <div class="card-inner">
          <div class="card-rank">${card.rank}</div>
          <div class="card-suit">${card.suit}</div>
        </div>
      </div>
      <button class="poker-hold-btn ${held[idx] ? 'held' : ''}" onclick="PokerGame.toggleHold(${idx})">${held[idx] ? 'TENUTA' : 'TIENI'}</button>
    </div>`;
  }

  function render() {
    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">♠ VIDEO POKER</h2>
          <div class="game-balance" id="vp-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="poker-table">
          <div class="poker-hand-label">LA TUA MANO</div>
          <div class="poker-cards" id="vp-cards" style="gap:0.75rem;flex-wrap:wrap;"></div>
          <div class="hand-name" id="vp-hand-name"></div>
        </div>
        <div id="vp-result"></div>
        ${createBetControls('vp', 5)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal" id="vp-deal-btn" onclick="PokerGame.dealDraw()">🃏 Distribuisci</button>
          <button class="btn-game btn-clear" onclick="PokerGame.clearHolds()">Reset Tieni</button>
        </div>
        <div class="poker-paytable">
          ${PAYTABLE.map(p => `
            <div class="pay-hand">${p.name}</div>
            <div class="pay-mult">${p.mult}x</div>
          `).join('')}
        </div>
      </div>`;
  }

  function updateCards() {
    const el = document.getElementById('vp-cards');
    if (el && hand.length) el.innerHTML = hand.map((c, i) => cardHTML(c, i)).join('');
  }

  function toggleHold(idx) {
    if (phase !== 'draw') return;
    held[idx] = !held[idx];
    updateCards();
  }

  function clearHolds() {
    held = [false,false,false,false,false];
    updateCards();
  }

  function dealDraw() {
    if (phase === 'idle') {
      const bet = getBet('vp');
      if (!bet || bet < 1) { showToast('Inserisci una puntata', 'info'); return; }
      if (!State.deductBalance(bet)) { showToast('Saldo insufficiente!', 'lose'); return; }
      currentBet = bet;
      deck = newDeck();
      hand = [deck.pop(), deck.pop(), deck.pop(), deck.pop(), deck.pop()];
      held = [false,false,false,false,false];
      phase = 'draw';
      const btn = document.getElementById('vp-deal-btn');
      if (btn) btn.textContent = '🔄 Cambia Carte';
      document.getElementById('vp-result').innerHTML = '';
      document.getElementById('vp-hand-name').textContent = '';
      updateCards();
    } else if (phase === 'draw') {
      // Replace non-held cards
      for (let i = 0; i < 5; i++) {
        if (!held[i]) hand[i] = deck.pop();
      }
      phase = 'idle';
      const btn = document.getElementById('vp-deal-btn');
      if (btn) btn.textContent = '🃏 Distribuisci';
      held = [false,false,false,false,false];
      updateCards();

      const handName = evaluateHand(hand);
      const mult = getMult(handName);
      document.getElementById('vp-hand-name').textContent = handName;

      if (mult > 0) {
        const win = parseFloat((currentBet * mult).toFixed(2));
        State.addBalance(win);
        document.getElementById('vp-result').innerHTML = `<div class="result-banner result-win">🏆 ${handName} — +${formatCurrency(win)}</div>`;
        showToast(`♠ ${handName}! Vinto ${formatCurrency(win)}`, 'win');
        State.recordHistory({ game: 'Video Poker', bet: currentBet, result: 'win', gain: win - currentBet });
      } else {
        document.getElementById('vp-result').innerHTML = `<div class="result-banner result-lose">Nessuna vincita</div>`;
        State.recordHistory({ game: 'Video Poker', bet: currentBet, result: 'lose', gain: -currentBet });
      }
      const balEl = document.getElementById('vp-bal');
      if (balEl) balEl.textContent = formatCurrency(State.balance);
    }
  }

  return { render, dealDraw, toggleHold, clearHolds };
})();
