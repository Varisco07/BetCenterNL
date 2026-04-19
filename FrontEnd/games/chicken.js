// =============================================
// BetCenterNL — CHICKEN CROSS THE ROAD (collegato al backend)
// =============================================

const ChickenGame = (() => {
  const GRID_SIZE = 5;
  let gameActive = false, bet = 0, level = 0, multiplier = 1.0;

  function render() {
    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">🐔 CHICKEN CROSS THE ROAD</h2>
          <div class="game-balance" id="chicken-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="chicken-game-container">
          <div class="chicken-info-panel">
            <div class="chicken-stat">
              <div class="stat-icon">📊</div>
              <div class="stat-content">
                <div class="stat-label">Livello</div>
                <div class="stat-value" id="chicken-level">0</div>
              </div>
            </div>
            <div class="chicken-stat highlight">
              <div class="stat-icon">💰</div>
              <div class="stat-content">
                <div class="stat-label">Moltiplicatore</div>
                <div class="stat-value" id="chicken-multiplier">1.00x</div>
              </div>
            </div>
            <div class="chicken-stat">
              <div class="stat-icon">🏆</div>
              <div class="stat-content">
                <div class="stat-label">Vincita Potenziale</div>
                <div class="stat-value" id="chicken-win">€0.00</div>
              </div>
            </div>
          </div>
          <div class="chicken-road">
            <div class="road-header">
              <div class="road-sign">
                <span class="sign-icon">🚦</span>
                <span class="sign-text">ATTRAVERSA LA STRADA</span>
                <span class="sign-icon">🚦</span>
              </div>
            </div>
            <div class="chicken-grid" id="chicken-grid">${generateGrid()}</div>
            <div class="road-footer">
              <div class="road-instruction">
                <span class="instruction-icon">👆</span>
                Clicca su una casella per far attraversare il pollo!
              </div>
            </div>
          </div>
          <div id="chicken-result"></div>
          <div class="chicken-controls">
            <div id="chicken-bet-controls">
              ${createBetControls('chicken', 10)}
              <div class="game-btn-row">
                <button class="btn-game btn-deal btn-chicken-start" id="chicken-start" onclick="ChickenGame.startGame()">
                  <span class="btn-icon">🐔</span><span class="btn-text">INIZIA PARTITA</span>
                </button>
              </div>
            </div>
            <div id="chicken-game-controls" style="display:none;">
              <button class="btn-game btn-cashout-chicken" id="chicken-cashout" onclick="ChickenGame.cashOut()">
                <span class="btn-icon">💰</span>
                <span class="btn-text">INCASSA</span>
                <span class="btn-amount" id="cashout-amount">€0.00</span>
              </button>
            </div>
          </div>
          <div class="chicken-rules">
            <div class="rules-title">📋 COME GIOCARE</div>
            <div class="rules-grid">
              <div class="rule-item"><div class="rule-icon">🐔</div><div class="rule-text">Il pollo deve attraversare la strada</div></div>
              <div class="rule-item"><div class="rule-icon">🚗</div><div class="rule-text">Evita le auto nascoste</div></div>
              <div class="rule-item"><div class="rule-icon">💰</div><div class="rule-text">Ogni livello: moltiplicatore x1.5</div></div>
              <div class="rule-item"><div class="rule-icon">🏆</div><div class="rule-text">Incassa quando vuoi!</div></div>
            </div>
          </div>
        </div>
      </div>`;
  }

  function generateGrid() {
    let html = '';
    for (let i = 0; i < GRID_SIZE; i++) {
      html += `<div class="chicken-tile" data-pos="${i}" onclick="ChickenGame.selectTile(${i})">
        <div class="tile-content" id="tile-${i}">
          <div class="tile-front"><span class="tile-number">${i+1}</span></div>
        </div>
      </div>`;
    }
    return html;
  }

  async function startGame() {
    const betAmount = getBet('chicken');
    if (!betAmount || betAmount < 1) { showToast('Inserisci una puntata valida', 'info'); return; }
    if (State.balance < betAmount)   { showToast('Saldo insufficiente!', 'lose'); return; }

    bet        = betAmount;
    level      = 0;
    multiplier = 1.0;
    gameActive = true;

    document.getElementById('chicken-bet-controls').style.display  = 'none';
    document.getElementById('chicken-game-controls').style.display = 'block';
    resetGrid();
    updateStats();
    showToast('🐔 Partita iniziata! Scegli una casella', 'info');
  }

  async function selectTile(position) {
    if (!gameActive) { showToast('Inizia una partita prima!', 'info'); return; }

    const tile      = document.getElementById(`tile-${position}`);
    const tileOuter = document.querySelector(`[data-pos="${position}"]`);
    if (!tile || tile.classList.contains('revealed')) return;

    // Chiedi al server se c'è un'auto
    let result;
    try {
      result = await API.chickenMove(bet, level, position);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      return;
    }

    tile.classList.add('revealed');
    tileOuter.classList.add('flipped');

    if (result.hit) {
      // Auto!
      tile.innerHTML = '<div class="tile-back danger"><span class="tile-icon">🚗</span></div>';
      try { AudioEngine.play('lose'); } catch (_) {}
      // Mostra le auto rimanenti
      result.cars.forEach((hasCar, i) => {
        if (hasCar && i !== position) {
          const t = document.getElementById(`tile-${i}`);
          const o = document.querySelector(`[data-pos="${i}"]`);
          if (t && !t.classList.contains('revealed')) {
            t.classList.add('revealed');
            o.classList.add('flipped');
            t.innerHTML = '<div class="tile-back danger auto-reveal"><span class="tile-icon">🚗</span></div>';
          }
        }
      });
      setTimeout(() => gameOver(), 800);
    } else {
      // Sicuro!
      tile.innerHTML = '<div class="tile-back safe"><span class="tile-icon">🐔</span></div>';
      try { AudioEngine.play('win'); } catch (_) {}
      setTimeout(() => levelUp(), 600);
    }
  }

  function levelUp() {
    level++;
    multiplier *= 1.5;
    updateStats();
    showToast(`✅ Livello ${level} completato! Moltiplicatore: ${multiplier.toFixed(2)}x`, 'win');
    setTimeout(() => resetGrid(), 1500);
  }

  function resetGrid() {
    for (let i = 0; i < GRID_SIZE; i++) {
      const tile      = document.getElementById(`tile-${i}`);
      const tileOuter = document.querySelector(`[data-pos="${i}"]`);
      if (!tile) continue;
      tile.className = 'tile-content';
      tileOuter.classList.remove('flipped');
      tile.innerHTML = `<div class="tile-front"><span class="tile-number">${i+1}</span></div>`;
    }
  }

  async function cashOut() {
    if (!gameActive) return;
    gameActive = false;

    let result;
    try {
      result = await API.chickenCashout(bet, multiplier);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      return;
    }

    State.syncFromServer(result.newBalance);

    document.getElementById('chicken-result').innerHTML = `
      <div class="result-banner result-win">
        <div class="result-title">💰 HAI INCASSATO!</div>
        <div class="result-details">
          <div>Livelli completati: <strong>${level}</strong></div>
          <div>Moltiplicatore: <strong>${multiplier.toFixed(2)}x</strong></div>
          <div>Vincita: <strong>${formatCurrency(result.winAmt)}</strong></div>
          <div>Guadagno: <strong>${formatCurrency(result.gain)}</strong></div>
        </div>
      </div>`;

    showToast(`🏆 Incassato ${formatCurrency(result.winAmt)}!`, 'win', 4000);
    if (result.winAmt > bet * 5) try { VFX.celebrate(); } catch (_) {}
    State.recordHistory({ game:'Chicken Road', bet, result:'win', gain:result.gain });

    const balEl = document.getElementById('chicken-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    setTimeout(() => {
      document.getElementById('chicken-bet-controls').style.display  = 'block';
      document.getElementById('chicken-game-controls').style.display = 'none';
      document.getElementById('chicken-result').innerHTML = '';
      resetGrid();
    }, 5000);
  }

  async function gameOver() {
    gameActive = false;

    try {
      const result = await API.chickenGameover(bet);
      State.syncFromServer(result.newBalance);
    } catch (_) {}

    document.getElementById('chicken-result').innerHTML = `
      <div class="result-banner result-lose">
        <div class="result-title">🚗💥 GAME OVER!</div>
        <div class="result-details">
          <div>Il pollo è stato investito!</div>
          <div>Livelli completati: <strong>${level}</strong></div>
          <div>Hai perso: <strong>${formatCurrency(bet)}</strong></div>
        </div>
      </div>`;

    showToast('💥 Il pollo è stato investito!', 'lose');
    try { VFX.screenShake(); } catch (_) {}
    State.recordHistory({ game:'Chicken Road', bet, result:'lose', gain:-bet });

    const balEl = document.getElementById('chicken-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    setTimeout(() => {
      document.getElementById('chicken-bet-controls').style.display  = 'block';
      document.getElementById('chicken-game-controls').style.display = 'none';
      document.getElementById('chicken-result').innerHTML = '';
      resetGrid();
    }, 5000);
  }

  function updateStats() {
    const el = id => document.getElementById(id);
    if (el('chicken-level'))      el('chicken-level').textContent      = level;
    if (el('chicken-multiplier')) el('chicken-multiplier').textContent = multiplier.toFixed(2) + 'x';
    if (el('chicken-win'))        el('chicken-win').textContent        = formatCurrency(bet * multiplier);
    if (el('cashout-amount'))     el('cashout-amount').textContent     = formatCurrency(bet * multiplier);
  }

  return { render, startGame, selectTile, cashOut };
})();
