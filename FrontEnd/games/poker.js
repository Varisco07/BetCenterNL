// =============================================
// BetCenterNL — VIDEO POKER (collegato al backend)
// =============================================

const PokerGame = (() => {
  const PAYTABLE = [
    { name:'Royal Flush',      mult:800 },
    { name:'Straight Flush',   mult:50  },
    { name:'Poker (4 uguali)', mult:25  },
    { name:'Full House',       mult:9   },
    { name:'Colore (Flush)',   mult:6   },
    { name:'Scala (Straight)', mult:4   },
    { name:'Tris',             mult:3   },
    { name:'Doppia Coppia',    mult:2   },
    { name:'Coppia J+',        mult:1   },
    { name:'Nessuna Vincita',  mult:0   },
  ];

  let hand = [], held = [false,false,false,false,false];
  let phase = 'idle', currentBet = 0;

  function isRed(suit) { return suit === '♥' || suit === '♦'; }

  function cardHTML(card, idx) {
    if (!card) return '';
    const color    = isRed(card.suit) ? 'red' : 'black';
    const heldCls  = held[idx] ? 'held' : '';
    return `<div class="poker-card-col">
      <div class="playing-card ${color} ${heldCls}" onclick="PokerGame.toggleHold(${idx})"
           style="cursor:pointer;${held[idx]?'box-shadow:0 0 12px var(--accent);':''}">
        <div class="card-inner">
          <div class="card-rank">${card.rank}</div>
          <div class="card-suit">${card.suit}</div>
        </div>
      </div>
      <button class="poker-hold-btn ${held[idx]?'held':''}" onclick="PokerGame.toggleHold(${idx})">
        ${held[idx]?'TENUTA':'TIENI'}
      </button>
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
            <div class="pay-mult">${p.mult}x</div>`).join('')}
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

  async function dealDraw() {
    if (phase === 'idle') {
      // ── DEAL ──
      const bet = getBet('vp');
      if (!bet || bet < 1) { showToast('Inserisci una puntata', 'info'); return; }
      if (State.balance < bet) { showToast('Saldo insufficiente!', 'lose'); return; }

      currentBet = bet;
      let result;
      try {
        result = await API.dealPoker(bet);
      } catch (err) {
        showToast('Errore di connessione al server', 'lose');
        return;
      }

      hand  = result.hand;
      held  = [false,false,false,false,false];
      phase = 'draw';
      const btn = document.getElementById('vp-deal-btn');
      if (btn) btn.textContent = '🔄 Cambia Carte';
      document.getElementById('vp-result').innerHTML = '';
      document.getElementById('vp-hand-name').textContent = '';
      updateCards();

    } else if (phase === 'draw') {
      // ── DRAW ──
      let result;
      try {
        result = await API.drawPoker(currentBet, hand, held);
      } catch (err) {
        showToast('Errore di connessione al server', 'lose');
        return;
      }

      hand  = result.hand;
      phase = 'idle';
      held  = [false,false,false,false,false];
      const btn = document.getElementById('vp-deal-btn');
      if (btn) btn.textContent = '🃏 Distribuisci';
      updateCards();

      document.getElementById('vp-hand-name').textContent = result.evaluation.name;
      State.syncFromServer(result.newBalance);

      if (result.evaluation.mult > 0) {
        const win = parseFloat((currentBet * result.evaluation.mult).toFixed(2));
        document.getElementById('vp-result').innerHTML =
          `<div class="result-banner result-win">🏆 ${result.evaluation.name} — +${formatCurrency(win)}</div>`;
        showToast(`♠ ${result.evaluation.name}! Vinto ${formatCurrency(win)}`, 'win');
        State.recordHistory({ game:'Video Poker', bet:currentBet, result:'win', gain:result.gain });
      } else {
        document.getElementById('vp-result').innerHTML =
          `<div class="result-banner result-lose">Nessuna vincita</div>`;
        State.recordHistory({ game:'Video Poker', bet:currentBet, result:'lose', gain:result.gain });
      }

      const balEl = document.getElementById('vp-bal');
      if (balEl) balEl.textContent = formatCurrency(State.balance);
    }
  }

  return { render, dealDraw, toggleHold, clearHolds };
})();
