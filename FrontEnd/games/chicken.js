// =============================================
// BetCenterNL — CHICKEN CROSS THE ROAD
// =============================================

const ChickenGame = (() => {
  const GRID_SIZE = 5;
  
  let gameActive = false;
  let bet = 0;
  let level = 0;
  let multiplier = 1.0;
  let cars = [];
  let selectedPosition = null;
  
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
            
            <div class="chicken-grid" id="chicken-grid">
              ${generateGrid()}
            </div>
            
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
                  <span class="btn-icon">🐔</span>
                  <span class="btn-text">INIZIA PARTITA</span>
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
              <div class="rule-item">
                <div class="rule-icon">🐔</div>
                <div class="rule-text">Il pollo deve attraversare la strada</div>
              </div>
              <div class="rule-item">
                <div class="rule-icon">🚗</div>
                <div class="rule-text">Evita le auto nascoste</div>
              </div>
              <div class="rule-item">
                <div class="rule-icon">💰</div>
                <div class="rule-text">Ogni livello: moltiplicatore x1.5</div>
              </div>
              <div class="rule-item">
                <div class="rule-icon">🏆</div>
                <div class="rule-text">Incassa quando vuoi!</div>
              </div>
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
          <div class="tile-front">
            <span class="tile-number">${i + 1}</span>
          </div>
        </div>
      </div>`;
    }
    return html;
  }

  function startGame() {
    const betAmount = getBet('chicken');
    if (!betAmount || betAmount < 1) {
      showToast('Inserisci una puntata valida', 'info');
      return;
    }

    if (!State.deductBalance(betAmount)) {
      showToast('Saldo insufficiente!', 'lose');
      return;
    }

    bet = betAmount;
    level = 0;
    multiplier = 1.0;
    gameActive = true;
    selectedPosition = null;

    // Nascondi controlli bet, mostra controlli gioco
    document.getElementById('chicken-bet-controls').style.display = 'none';
    document.getElementById('chicken-game-controls').style.display = 'block';

    // Reset grid
    resetGrid();
    
    // Genera auto per il primo livello
    generateCars();

    updateStats();
    showToast('🐔 Partita iniziata! Scegli una casella', 'info');
  }

  function generateCars() {
    // Genera auto casuali, aumenta difficoltà con il livello
    const numCars = Math.min(1 + Math.floor(level / 2), GRID_SIZE - 1);
    cars = new Array(GRID_SIZE).fill(false);
    
    for (let i = 0; i < numCars; i++) {
      let pos;
      do {
        pos = Math.floor(Math.random() * GRID_SIZE);
      } while (cars[pos]);
      cars[pos] = true;
    }
  }

  function selectTile(position) {
    if (!gameActive) {
      showToast('Inizia una partita prima!', 'info');
      return;
    }

    const tile = document.getElementById(`tile-${position}`);
    const tileOuter = document.querySelector(`[data-pos="${position}"]`);
    
    if (tile.classList.contains('revealed')) {
      return; // Già rivelata
    }

    // Rivela la casella
    tile.classList.add('revealed');
    tileOuter.classList.add('flipped');
    
    if (cars[position]) {
      // Auto! Game Over
      tile.innerHTML = '<div class="tile-back danger"><span class="tile-icon">🚗</span></div>';
      AudioEngine.play('lose');
      setTimeout(() => gameOver(), 800);
    } else {
      // Sicuro!
      tile.innerHTML = '<div class="tile-back safe"><span class="tile-icon">🐔</span></div>';
      AudioEngine.play('win');
      setTimeout(() => levelUp(), 600);
    }
  }

  function levelUp() {
    level++;
    multiplier *= 1.5;
    
    updateStats();
    
    showToast(`✅ Livello ${level} completato! Moltiplicatore: ${multiplier.toFixed(2)}x`, 'win');
    
    // Genera nuove auto per il prossimo livello
    setTimeout(() => {
      resetGrid();
      generateCars();
    }, 1500);
  }

  function resetGrid() {
    for (let i = 0; i < GRID_SIZE; i++) {
      const tile = document.getElementById(`tile-${i}`);
      const tileOuter = document.querySelector(`[data-pos="${i}"]`);
      tile.className = 'tile-content';
      tile.classList.remove('revealed');
      tileOuter.classList.remove('flipped');
      tile.innerHTML = '<div class="tile-front"><span class="tile-number">' + (i + 1) + '</span></div>';
    }
  }

  function cashOut() {
    if (!gameActive) return;
    
    const winAmount = bet * multiplier;
    State.addBalance(winAmount);
    
    gameActive = false;
    
    // Mostra tutte le auto
    revealAllCars();
    
    const resultEl = document.getElementById('chicken-result');
    resultEl.innerHTML = `
      <div class="result-banner result-win">
        <div class="result-title">💰 HAI INCASSATO!</div>
        <div class="result-details">
          <div>Livelli completati: <strong>${level}</strong></div>
          <div>Moltiplicatore: <strong>${multiplier.toFixed(2)}x</strong></div>
          <div>Vincita: <strong>${formatCurrency(winAmount)}</strong></div>
          <div>Guadagno: <strong>${formatCurrency(winAmount - bet)}</strong></div>
        </div>
      </div>
    `;
    
    showToast(`🏆 Incassato ${formatCurrency(winAmount)}!`, 'win', 4000);
    
    if (winAmount > bet * 5) VFX.celebrate();
    
    State.recordHistory({
      game: 'Chicken Road',
      bet: bet,
      result: 'win',
      gain: winAmount - bet
    });
    
    // Aggiorna saldo
    const balEl = document.getElementById('chicken-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);
    
    // Ripristina controlli
    setTimeout(() => {
      document.getElementById('chicken-bet-controls').style.display = 'block';
      document.getElementById('chicken-game-controls').style.display = 'none';
      resultEl.innerHTML = '';
      resetGrid();
    }, 5000);
  }

  function gameOver() {
    gameActive = false;
    
    // Mostra tutte le auto
    revealAllCars();
    
    const resultEl = document.getElementById('chicken-result');
    resultEl.innerHTML = `
      <div class="result-banner result-lose">
        <div class="result-title">🚗💥 GAME OVER!</div>
        <div class="result-details">
          <div>Il pollo è stato investito!</div>
          <div>Livelli completati: <strong>${level}</strong></div>
          <div>Hai perso: <strong>${formatCurrency(bet)}</strong></div>
        </div>
      </div>
    `;
    
    showToast('💥 Il pollo è stato investito!', 'lose');
    VFX.screenShake();
    
    State.recordHistory({
      game: 'Chicken Road',
      bet: bet,
      result: 'lose',
      gain: -bet
    });
    
    // Aggiorna saldo
    const balEl = document.getElementById('chicken-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);
    
    // Ripristina controlli
    setTimeout(() => {
      document.getElementById('chicken-bet-controls').style.display = 'block';
      document.getElementById('chicken-game-controls').style.display = 'none';
      resultEl.innerHTML = '';
      resetGrid();
    }, 5000);
  }

  function revealAllCars() {
    for (let i = 0; i < GRID_SIZE; i++) {
      const tile = document.getElementById(`tile-${i}`);
      const tileOuter = document.querySelector(`[data-pos="${i}"]`);
      if (!tile.classList.contains('revealed') && cars[i]) {
        tile.classList.add('revealed', 'auto-reveal');
        tileOuter.classList.add('flipped');
        tile.innerHTML = '<div class="tile-back danger auto-reveal"><span class="tile-icon">🚗</span></div>';
      }
    }
  }

  function updateStats() {
    document.getElementById('chicken-level').textContent = level;
    document.getElementById('chicken-multiplier').textContent = multiplier.toFixed(2) + 'x';
    document.getElementById('chicken-win').textContent = formatCurrency(bet * multiplier);
    document.getElementById('cashout-amount').textContent = formatCurrency(bet * multiplier);
  }

  return { render, startGame, selectTile, cashOut };
})();