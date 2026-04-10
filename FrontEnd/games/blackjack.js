// =============================================
// BetCenterNL — BLACKJACK
// =============================================

const BlackjackGame = (() => {
  const SUITS = ['♠','♥','♦','♣'];
  const RANKS = ['A','2','3','4','5','6','7','8','9','10','J','Q','K'];
  let deck = [], playerHand = [], dealerHand = [], gameActive = false, currentBet = 0;

  function newDeck() {
    const d = [];
    for (const s of SUITS) for (const r of RANKS) d.push({ rank: r, suit: s });
    // Shuffle (Fisher-Yates)
    for (let i = d.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [d[i], d[j]] = [d[j], d[i]];
    }
    return d;
  }

  function cardValue(card) {
    if (['J','Q','K'].includes(card.rank)) return 10;
    if (card.rank === 'A') return 11;
    return parseInt(card.rank);
  }

  function handValue(hand) {
    let total = 0, aces = 0;
    for (const c of hand) {
      total += cardValue(c);
      if (c.rank === 'A') aces++;
    }
    while (total > 21 && aces > 0) { total -= 10; aces--; }
    return total;
  }

  function isRed(suit) { return suit === '♥' || suit === '♦'; }

  function cardHTML(card, faceDown = false) {
    if (faceDown) return `<div class="playing-card face-down"></div>`;
    const color = isRed(card.suit) ? 'red' : 'black';
    return `<div class="playing-card ${color}">
      <div class="card-inner">
        <div class="card-rank">${card.rank}</div>
        <div class="card-suit">${card.suit}</div>
      </div>
    </div>`;
  }

  function setButtons(phase) {
    const deal = document.getElementById('bj-deal');
    const hit = document.getElementById('bj-hit');
    const stand = document.getElementById('bj-stand');
    const dbl = document.getElementById('bj-double');
    if (!deal) return;
    if (phase === 'idle') { deal.disabled = false; hit.disabled = true; stand.disabled = true; dbl.disabled = true; }
    if (phase === 'active') { deal.disabled = true; hit.disabled = false; stand.disabled = false; dbl.disabled = false; }
    if (phase === 'over') { deal.disabled = false; hit.disabled = true; stand.disabled = true; dbl.disabled = true; }
  }

  function updateUI(hideDealer = true) {
    const playerArea = document.getElementById('bj-player-cards');
    const dealerArea = document.getElementById('bj-dealer-cards');
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
          <button class="btn-game btn-deal" id="bj-deal" onclick="BlackjackGame.deal()">🃏 Distribuisci</button>
          <button class="btn-game btn-hit" id="bj-hit" onclick="BlackjackGame.hit()" disabled>HIT</button>
          <button class="btn-game btn-stand" id="bj-stand" onclick="BlackjackGame.stand()" disabled>STAND</button>
          <button class="btn-game btn-double" id="bj-double" onclick="BlackjackGame.double()" disabled>DOPPIO</button>
        </div>
        <div class="info-box mt-2">
          <strong>Regole:</strong> Avvicinarsi a 21 senza sforare. Blackjack paga 3:2.
          Il banco deve pescare fino a 17. Il doppio raddoppia la puntata e ricevi una sola carta.
        </div>
        <div class="hotkey-hint"><span class="key-badge">Enter</span> nuova mano &nbsp; <span class="key-badge">H</span> Hit &nbsp; <span class="key-badge">S</span> Stand</div>
      </div>`;
  }

  function deal() {
    const bet = getBet('bj');
    if (!bet || bet < 1) { showToast('Inserisci una puntata', 'info'); return; }
    if (!State.deductBalance(bet)) { showToast('Saldo insufficiente!', 'lose'); return; }
    currentBet = bet;
    deck = newDeck();
    playerHand = [deck.pop(), deck.pop()];
    dealerHand = [deck.pop(), deck.pop()];
    gameActive = true;
    document.getElementById('bj-result').innerHTML = '';
    AudioEngine.play('cardDeal');
    setTimeout(() => AudioEngine.play('cardDeal'), 150);
    setTimeout(() => AudioEngine.play('cardDeal'), 300);
    setTimeout(() => AudioEngine.play('cardDeal'), 450);
    updateUI(true);
    setButtons('active');
    if (handValue(playerHand) === 21) setTimeout(() => stand(true), 500);
  }

  function hit() {
    if (!gameActive) return;
    playerHand.push(deck.pop());
    AudioEngine.play('cardDeal');
    updateUI(true);
    if (handValue(playerHand) > 21)      { finishGame('bust'); }
    else if (handValue(playerHand) === 21) { stand(); }
  }

  async function stand(isBlackjack = false) {
    if (!gameActive) return;
    gameActive = false;
    setButtons('over');
    updateUI(false);

    // Dealer draws
    await delay(600);
    while (handValue(dealerHand) < 17) {
      dealerHand.push(deck.pop());
      updateUI(false);
      await delay(400);
    }
    finishGame('compare', isBlackjack);
  }

  function double() {
    if (!gameActive) return;
    if (!State.deductBalance(currentBet)) { showToast('Saldo insufficiente per il doppio!', 'info'); return; }
    currentBet *= 2;
    playerHand.push(deck.pop());
    updateUI(true);
    if (handValue(playerHand) > 21) {
      finishGame('bust');
    } else {
      stand();
    }
  }

  function finishGame(reason, isBlackjack = false) {
    gameActive = false;
    setButtons('over');
    updateUI(false);

    const pv = handValue(playerHand);
    const dv = handValue(dealerHand);
    let result = '', gain = 0;

    if (reason === 'bust') {
      result = '💥 SBALLATO! Hai superato 21';
      showResult(result, 'lose');
      VFX.screenShake();
      State.recordHistory({ game: 'Blackjack', bet: currentBet, result: 'lose', gain: -currentBet });
      showToast('💥 Sballato!', 'lose');
    } else if (isBlackjack && pv === 21 && dv !== 21) {
      gain = currentBet * 1.5;
      State.addBalance(currentBet + gain);
      result = `🎉 BLACKJACK! Vinto ${formatCurrency(gain)}`;
      showResult(result, 'win');
      VFX.celebrate();
      State.recordHistory({ game: 'Blackjack', bet: currentBet, result: 'win', gain });
      showToast(`🎉 Blackjack! +${formatCurrency(gain)}`, 'win');
    } else if (dv > 21 || pv > dv) {
      gain = currentBet;
      State.addBalance(currentBet + gain);
      result = `✅ VINCE IL GIOCATORE! +${formatCurrency(gain)}`;
      showResult(result, 'win');
      if (gain > 50) VFX.celebrate();
      State.recordHistory({ game: 'Blackjack', bet: currentBet, result: 'win', gain });
      showToast(`✅ Vinci! +${formatCurrency(gain)}`, 'win');
    } else if (pv === dv) {
      State.addBalance(currentBet);
      result = '🤝 PAREGGIO! Puntata restituita';
      showResult(result, 'push');
      State.recordHistory({ game: 'Blackjack', bet: currentBet, result: 'push', gain: 0 });
      showToast('🤝 Pareggio', 'info');
    } else {
      result = `❌ VINCE IL BANCO (${dv} vs ${pv})`;
      showResult(result, 'lose');
      VFX.screenShake();
      State.recordHistory({ game: 'Blackjack', bet: currentBet, result: 'lose', gain: -currentBet });
      showToast('❌ Vince il banco', 'lose');
    }

    const balEl = document.getElementById('bj-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);
  }

  return { render, deal, hit, stand, double };
})();
