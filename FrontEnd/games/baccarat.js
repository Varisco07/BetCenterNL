// =============================================
// BetCenterNL — BACCARAT (collegato al backend)
// =============================================

const BaccaratGame = (() => {
  let selectedBet = null;
  let playing = false;

  function isRed(suit) { return suit === '♥' || suit === '♦'; }

  function cardHTML(card) {
    if (!card) return '';
    const color = isRed(card.suit) ? 'red' : 'black';
    return `<div class="playing-card ${color}">
      <div class="card-inner">
        <div class="card-rank">${card.rank}</div>
        <div class="card-suit">${card.suit}</div>
      </div>
    </div>`;
  }

  function render() {
    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">💎 BACCARAT</h2>
          <div class="game-balance" id="bc-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="baccarat-table">
          <div class="baccarat-areas">
            <div class="baccarat-side">
              <div class="baccarat-side-label">GIOCATORE</div>
              <div class="card-row" id="bc-player-cards" style="justify-content:center;min-height:100px"></div>
              <div class="baccarat-score" id="bc-player-score">—</div>
            </div>
            <div class="baccarat-vs">VS</div>
            <div class="baccarat-side">
              <div class="baccarat-side-label">BANCO</div>
              <div class="card-row" id="bc-banker-cards" style="justify-content:center;min-height:100px"></div>
              <div class="baccarat-score" id="bc-banker-score">—</div>
            </div>
          </div>
        </div>
        <div id="bc-result"></div>
        <div class="baccarat-bets">
          <div class="bacc-bet-btn ${selectedBet==='player'?'selected':''}" onclick="BaccaratGame.selectBet('player')">
            <strong>GIOCATORE</strong><small>Paga 1:1</small>
          </div>
          <div class="bacc-bet-btn ${selectedBet==='tie'?'selected':''}" onclick="BaccaratGame.selectBet('tie')">
            <strong>PAREGGIO</strong><small>Paga 8:1</small>
          </div>
          <div class="bacc-bet-btn ${selectedBet==='banker'?'selected':''}" onclick="BaccaratGame.selectBet('banker')">
            <strong>BANCO</strong><small>Paga 0.95:1</small>
          </div>
        </div>
        ${createBetControls('bc', 10)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal" onclick="BaccaratGame.play()">🃏 GIOCA</button>
        </div>
        <div class="info-box mt-2">
          <strong>Regole:</strong> 10/J/Q/K = 0, Asso = 1. La mano più vicina a 9 vince.
          Commissione del 5% sul banco.
        </div>
      </div>`;
  }

  function selectBet(type) {
    selectedBet = type;
    document.querySelectorAll('.bacc-bet-btn').forEach(el => {
      const map = { player:'GIOCATORE', tie:'PAREGGIO', banker:'BANCO' };
      el.classList.toggle('selected', el.querySelector('strong')?.textContent === map[type]);
    });
  }

  async function play() {
    if (playing) return;
    if (!selectedBet) { showToast('Scegli su chi scommettere!', 'info'); return; }
    const bet = getBet('bc');
    if (!bet || bet < 1) { showToast('Inserisci una puntata', 'info'); return; }
    if (State.balance < bet) { showToast('Saldo insufficiente!', 'lose'); return; }

    playing = true;
    document.getElementById('bc-result').innerHTML = '';
    const playerCards = document.getElementById('bc-player-cards');
    const bankerCards = document.getElementById('bc-banker-cards');
    if (playerCards) playerCards.innerHTML = '';
    if (bankerCards) bankerCards.innerHTML = '';
    document.getElementById('bc-player-score').textContent = '—';
    document.getElementById('bc-banker-score').textContent = '—';

    let result;
    try {
      result = await API.playBaccarat(bet, selectedBet);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      playing = false;
      return;
    }

    // Anima le carte
    for (const c of result.playerHand) {
      await delay(200);
      if (playerCards) playerCards.innerHTML += cardHTML(c);
    }
    for (const c of result.bankerHand) {
      await delay(200);
      if (bankerCards) bankerCards.innerHTML += cardHTML(c);
    }

    document.getElementById('bc-player-score').textContent = result.playerValue;
    document.getElementById('bc-banker-score').textContent = result.bankerValue;

    State.syncFromServer(result.newBalance);

    const labels = { player:'GIOCATORE', banker:'BANCO', tie:'PAREGGIO' };
    let msg = '', type = 'lose';

    if (result.resultType === 'win') {
      msg  = `✅ ${labels[result.winner]} VINCE! +${formatCurrency(result.gain)}`;
      type = 'win';
      try { VFX.celebrate(); } catch (_) {}
    } else if (result.resultType === 'push') {
      msg  = '🤝 PAREGGIO — Puntata restituita';
      type = 'push';
    } else {
      msg = `❌ ${labels[result.winner]} VINCE`;
    }

    document.getElementById('bc-result').innerHTML =
      `<div class="result-banner result-${type}">${msg}</div>`;
    showToast(msg.replace(/[✅❌🤝]/g,'').trim(), type==='win'?'win':type==='push'?'info':'lose');
    State.recordHistory({ game:'Baccarat', bet, result:result.resultType, gain:result.gain });

    const balEl = document.getElementById('bc-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);
    playing = false;
  }

  return { render, selectBet, play };
})();
