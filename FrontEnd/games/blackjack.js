// =============================================
// BetCenterNL — BLACKJACK (collegato al backend)
// =============================================

const BlackjackGame = (() => {
  const SUITS = ['♠','♥','♦','♣'];
  const RANKS = ['A','2','3','4','5','6','7','8','9','10','J','Q','K'];

  let playerHand = [], dealerHand = [], dealerHandFull = [];
  let gameActive = false, currentBet = 0;

  function cardValue(card) {
    const r = card.rank;
    if (['J','Q','K','Jack','Queen','King'].includes(r)) return 10;
    if (r === 'A' || r === 'Asso') return 11;
    const n = parseInt(r);
    return isNaN(n) ? 0 : n;
  }

  function handValue(hand) {
    let total = 0, aces = 0;
    for (const c of hand) {
      if (!c || c.rank === '?') continue;
      total += cardValue(c);
      if (c.rank === 'A' || c.rank === 'Asso') aces++;
    }
    while (total > 21 && aces > 0) { total -= 10; aces--; }
    return total;
  }

  function isRed(suit) { return suit === '♥' || suit === '♦'; }

  function cardHTML(card, faceDown = false) {
    if (faceDown || !card || card.rank === '?')
      return `<div class="playing-card face-down"></div>`;
    const color = isRed(card.suit) ? 'red' : 'black';
    return `<div class="playing-card ${color}">
      <div class="card-inner">
        <div class="card-rank">${card.rank}</div>
        <div class="card-suit">${card.suit}</div>
      </div>
    </div>`;
  }

  function setButtons(phase) {
    const deal  = document.getElementById('bj-deal');
    const hit   = document.getElementById('bj-hit');
    const stand = document.getElementById('bj-stand');
    const dbl   = document.getElementById('bj-double');
    if (!deal) return;
    if (phase === 'idle')   { deal.disabled=false; hit.disabled=true;  stand.disabled=true;  dbl.disabled=true;  }
    if (phase === 'active') { deal.disabled=true;  hit.disabled=false; stand.disabled=false; dbl.disabled=false; }
    if (phase === 'over')   { deal.disabled=false; hit.disabled=true;  stand.disabled=true;  dbl.disabled=true;  }
  }

  function updateUI(hideDealer = true) {
    const playerArea  = document.getElementById('bj-player-cards');
    const dealerArea  = document.getElementById('bj-dealer-cards');
    const playerScore = document.getElementById('bj-player-score');
    const dealerScore = document.getElementById('bj-dealer-score');
    if (!playerArea) return;
    playerArea.innerHTML = playerHand.map(c => cardHTML(c)).join('');
    dealerArea.innerHTML = dealerHand.map((c, i) => cardHTML(c, hideDealer && i === 1)).join('');
    playerScore.textContent = `(${handValue(playerHand)})`;
    dealerScore.textContent = hideDealer ? '(?)' : `(${handValue(dealerHand)})`;
  }

  function showResult(msg, type) {
    const el = document.getElementById('bj-result');
    if (el) el.innerHTML = `<div class="result-banner result-${type}">${msg}</div>`;
  }

  // Mazzo locale per hit/stand (il server gestisce solo deal e resolve)
  function newLocalDeck() {
    const d = [];
    for (const s of SUITS) for (const r of RANKS) d.push({ rank: r, suit: s });
    for (let i = d.length-1; i > 0; i--) {
      const j = Math.floor(Math.random()*(i+1));
      [d[i],d[j]] = [d[j],d[i]];
    }
    return d;
  }
  let localDeck = [];

  function render() {
    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">🃏 BLACKJACK</h2>
          <div class="game-balance" id="bj-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="card-area">
          <div>
            <div class="card-row-label">BANCO <span class="card-score" id="bj-dealer-score">(0)</span></div>
            <div class="card-row" id="bj-dealer-cards"></div>
          </div>
          <div>
            <div class="card-row-label">GIOCATORE <span class="card-score" id="bj-player-score">(0)</span></div>
            <div class="card-row" id="bj-player-cards"></div>
          </div>
        </div>
        <div id="bj-result"></div>
        ${createBetControls('bj', 10)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal"   id="bj-deal"   onclick="BlackjackGame.deal()">🃏 Distribuisci</button>
          <button class="btn-game btn-hit"    id="bj-hit"    onclick="BlackjackGame.hit()"    disabled>HIT</button>
          <button class="btn-game btn-stand"  id="bj-stand"  onclick="BlackjackGame.stand()"  disabled>STAND</button>
          <button class="btn-game btn-double" id="bj-double" onclick="BlackjackGame.double()" disabled>DOPPIO</button>
        </div>
        <div class="info-box mt-2">
          <strong>Regole:</strong> Avvicinarsi a 21 senza sforare. Blackjack paga 3:2.
          Il banco deve pescare fino a 17.
        </div>
        <div class="hotkey-hint">
          <span class="key-badge">Enter</span> nuova mano &nbsp;
          <span class="key-badge">H</span> Hit &nbsp;
          <span class="key-badge">S</span> Stand
        </div>
      </div>`;
  }

  async function deal() {
    const bet = getBet('bj');
    if (!bet || bet < 1) { showToast('Inserisci una puntata', 'info'); return; }
    if (State.balance < bet) { showToast('Saldo insufficiente!', 'lose'); return; }

    setButtons('active');
    document.getElementById('bj-result').innerHTML = '';

    let serverResult;
    try {
      serverResult = await API.dealBlackjack(bet);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      setButtons('idle');
      return;
    }

    currentBet    = bet;
    playerHand    = serverResult.playerHand;
    dealerHand    = serverResult.dealerHand;       // seconda carta nascosta
    dealerHandFull = serverResult.dealerHandFull;  // mano completa per il turno banco
    localDeck     = newLocalDeck();

    // Rimuovi le carte già distribuite dal mazzo locale
    [...playerHand, ...dealerHandFull].forEach(c => {
      const idx = localDeck.findIndex(d => d.rank === c.rank && d.suit === c.suit);
      if (idx !== -1) localDeck.splice(idx, 1);
    });

    gameActive = true;
    try { AudioEngine.play('cardDeal'); } catch (_) {}
    updateUI(true);

    // Blackjack naturale giocatore
    if (handValue(playerHand) === 21) setTimeout(() => stand(true), 500);
  }

  function hit() {
    if (!gameActive) return;
    const card = localDeck.pop();
    playerHand.push(card);
    try { AudioEngine.play('cardDeal'); } catch (_) {}
    updateUI(true);
    if (handValue(playerHand) > 21)       finishGame('bust');
    else if (handValue(playerHand) === 21) stand();
  }

  async function stand(isBlackjack = false) {
    if (!gameActive) return;
    gameActive = false;
    setButtons('over');

    // Usa la mano completa del banco
    dealerHand = [...dealerHandFull];
    updateUI(false);

    // Il banco pesca fino a 17
    await delay(600);
    while (handValue(dealerHand) < 17) {
      dealerHand.push(localDeck.pop());
      updateUI(false);
      await delay(400);
    }

    finishGame('compare', isBlackjack);
  }

  function double() {
    if (!gameActive) return;
    if (State.balance < currentBet) { showToast('Saldo insufficiente per il doppio!', 'info'); return; }
    currentBet *= 2;
    playerHand.push(localDeck.pop());
    updateUI(true);
    if (handValue(playerHand) > 21) finishGame('bust');
    else stand();
  }

  async function finishGame(reason, isBlackjack = false) {
    gameActive = false;
    setButtons('over');
    updateUI(false);

    const pv = handValue(playerHand);
    const dv = handValue(dealerHand);

    // Chiedi al server di calcolare l'esito e aggiornare il saldo
    let serverResult;
    try {
      serverResult = await API.resolveBlackjack(
        currentBet,
        playerHand,
        dealerHand,
        isBlackjack && pv === 21
      );
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      return;
    }

    State.syncBalance(serverResult.newBalance);

    const { result, gain } = serverResult;
    let msg = '', type = 'lose';

    if (reason === 'bust') {
      msg = '💥 SBALLATO! Hai superato 21';
      try { VFX.screenShake(); } catch (_) {}
    } else if (result === 'win' && isBlackjack) {
      msg = `🎉 BLACKJACK! Vinto ${formatCurrency(gain)}`;
      type = 'win';
      try { VFX.celebrate(); } catch (_) {}
    } else if (result === 'win') {
      msg = `✅ VINCE IL GIOCATORE! +${formatCurrency(gain)}`;
      type = 'win';
      if (gain > 50) try { VFX.celebrate(); } catch (_) {}
    } else if (result === 'push') {
      msg = '🤝 PAREGGIO! Puntata restituita';
      type = 'push';
    } else {
      msg = `❌ VINCE IL BANCO (${dv} vs ${pv})`;
      try { VFX.screenShake(); } catch (_) {}
    }

    showResult(msg, type);
    showToast(msg.replace(/[🎉✅❌💥🤝]/g,'').trim(), type === 'win' ? 'win' : type === 'push' ? 'info' : 'lose');
    State.recordHistory({ game: 'Blackjack', bet: currentBet, result, gain });

    const balEl = document.getElementById('bj-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);
  }

  return { render, deal, hit, stand, double };
})();
