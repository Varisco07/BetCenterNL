// =============================================
// BetCenterNL — SLOT MACHINE
// =============================================

const SlotGame = (() => {
  const SYMBOLS = ['🍒', '🍋', '🍊', '🍇', '⭐', '💎', '7️⃣', '🔔'];
  const WEIGHTS = [20, 18, 16, 14, 10, 8, 6, 4]; // Sum=96
  const PAYTABLE = {
    '🍒🍒🍒': 5,
    '🍋🍋🍋': 8,
    '🍊🍊🍊': 10,
    '🍇🍇🍇': 15,
    '⭐⭐⭐': 25,
    '💎💎💎': 50,
    '7️⃣7️⃣7️⃣': 100,
    '🔔🔔🔔': 200,
    // Two of a kind
    '🍒🍒_': 1.5,
    '_🍒🍒': 1.5,
    '💎💎_': 3,
    '_💎💎': 3,
    '7️⃣7️⃣_': 4,
    '_7️⃣7️⃣': 4,
    '🔔🔔_': 6,
    '_🔔🔔': 6,
  };

  let spinning = false;
  let currentReels = ['🍒', '🍒', '🍒'];

  function weightedRandom() {
    const total = WEIGHTS.reduce((a, b) => a + b, 0);
    let r = Math.random() * total;
    for (let i = 0; i < SYMBOLS.length; i++) {
      r -= WEIGHTS[i];
      if (r <= 0) return SYMBOLS[i];
    }
    return SYMBOLS[0];
  }

  function checkWin(reels) {
    const key = reels.join('');
    if (PAYTABLE[key]) return PAYTABLE[key];
    // Check two-of-a-kind
    const r = reels;
    if (r[0] === r[1]) return PAYTABLE[`${r[0]}${r[1]}_`] || 0;
    if (r[1] === r[2]) return PAYTABLE[`_${r[1]}${r[2]}`] || 0;
    return 0;
  }

  function render() {
    const jackpot = (parseFloat(localStorage.getItem('betcenter_jackpot') || '12450') + Math.random() * 50).toFixed(2);
    localStorage.setItem('betcenter_jackpot', jackpot);

    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">🎰 SLOT MACHINE</h2>
          <div class="game-balance" id="slot-bal">${formatCurrency(State.balance)}</div>
        </div>

        <!-- JACKPOT DISPLAY -->
        <div class="jackpot-container">
          <div class="jackpot-label">🏆 JACKPOT PROGRESSIVO 🏆</div>
          <div class="jackpot-amount" id="jackpot-display">${formatCurrency(parseFloat(jackpot))}</div>
          <div class="jackpot-subtitle">Vinci con 🔔🔔🔔</div>
        </div>

        <div class="slot-machine">
          <div class="slot-machine-frame">
            <div class="slot-machine-top">
              <div class="slot-lights">
                <span class="light"></span>
                <span class="light"></span>
                <span class="light"></span>
                <span class="light"></span>
                <span class="light"></span>
              </div>
            </div>
            
            <div class="slot-reels-container">
              <div class="slot-reels">
                <div class="slot-reel" id="reel-0"><div class="reel-symbol">🍒</div></div>
                <div class="slot-reel" id="reel-1"><div class="reel-symbol">🍒</div></div>
                <div class="slot-reel" id="reel-2"><div class="reel-symbol">🍒</div></div>
              </div>
              <div class="slot-payline"></div>
            </div>
            
            <div class="slot-machine-bottom">
              <div id="slot-result"></div>
            </div>
          </div>

          ${createBetControls('slot', 5)}
          
          <div class="game-btn-row">
            <button class="btn-game btn-deal btn-spin-large" id="spin-btn" onclick="SlotGame.spin()">
              <span class="btn-icon">🎰</span>
              <span class="btn-text">GIRA!</span>
            </button>
            <button class="btn-game btn-double" id="auto-btn" onclick="SlotGame.toggleAuto()">
              <span class="btn-icon">⚡</span>
              <span class="btn-text">AUTO: OFF</span>
            </button>
          </div>
          
          <div class="hotkey-hint">
            <span class="key-badge">Enter</span> per girare rapidamente
          </div>
        </div>

        <!-- PAYTABLE -->
        <div class="paytable-container">
          <div class="paytable-header">💰 TABELLA PAGAMENTI</div>
          <div class="paytable-grid">
            <div class="paytable-item premium">
              <div class="paytable-symbols">🔔🔔🔔</div>
              <div class="paytable-multiplier">200x</div>
            </div>
            <div class="paytable-item premium">
              <div class="paytable-symbols">7️⃣7️⃣7️⃣</div>
              <div class="paytable-multiplier">100x</div>
            </div>
            <div class="paytable-item high">
              <div class="paytable-symbols">💎💎💎</div>
              <div class="paytable-multiplier">50x</div>
            </div>
            <div class="paytable-item high">
              <div class="paytable-symbols">⭐⭐⭐</div>
              <div class="paytable-multiplier">25x</div>
            </div>
            <div class="paytable-item medium">
              <div class="paytable-symbols">🍇🍇🍇</div>
              <div class="paytable-multiplier">15x</div>
            </div>
            <div class="paytable-item medium">
              <div class="paytable-symbols">🍊🍊🍊</div>
              <div class="paytable-multiplier">10x</div>
            </div>
            <div class="paytable-item low">
              <div class="paytable-symbols">🍋🍋🍋</div>
              <div class="paytable-multiplier">8x</div>
            </div>
            <div class="paytable-item low">
              <div class="paytable-symbols">🍒🍒🍒</div>
              <div class="paytable-multiplier">5x</div>
            </div>
          </div>
          <div class="paytable-note">💡 Due simboli uguali pagano anche!</div>
        </div>
      </div>`;
  }

  let autoSpinInterval = null;
  let autoOn = false;

  function toggleAuto() {
    autoOn = !autoOn;
    const btn = document.getElementById('auto-btn');
    if (!btn) return;
    if (autoOn) {
      btn.innerHTML = '<span class="btn-icon">⚡</span><span class="btn-text">AUTO: ON</span>';
      btn.classList.add('auto-active');
      autoSpinInterval = setInterval(() => { if (!spinning) spin(); }, 1500);
    } else {
      btn.innerHTML = '<span class="btn-icon">⚡</span><span class="btn-text">AUTO: OFF</span>';
      btn.classList.remove('auto-active');
      clearInterval(autoSpinInterval);
    }
  }

  async function spin() {
    if (spinning) return;
    const bet = getBet('slot');
    if (!bet || bet < 1) { showToast('Inserisci una puntata valida', 'info'); return; }
    if (!State.deductBalance(bet)) {
      showToast('Saldo insufficiente!', 'lose');
      if (autoOn) toggleAuto();
      return;
    }

    spinning = true;
    document.getElementById('spin-btn').disabled = true;
    document.getElementById('slot-result').innerHTML = '';
    AudioEngine.play('spin');

    const newReels = [weightedRandom(), weightedRandom(), weightedRandom()];

    // Start spinning all reels
    for (let i = 0; i < 3; i++) {
      startReelSpin(i);
    }

    // Stop reels one by one with delay
    for (let i = 0; i < 3; i++) {
      await delay(800 + i * 400);
      stopReelSpin(i, newReels[i]);
      AudioEngine.play('slotStop');
    }

    currentReels = newReels;
    const mult = checkWin(newReels);

    if (mult > 0) {
      const win = parseFloat((bet * mult).toFixed(2));
      State.addBalance(win);
      
      // Add glow effect to winning reels
      [0, 1, 2].forEach(i => {
        const reel = document.getElementById(`reel-${i}`);
        if (reel) {
          reel.classList.add('winning');
          setTimeout(() => reel.classList.remove('winning'), 2000);
        }
      });
      
      const resultEl = document.getElementById('slot-result');
      if (resultEl) {
        resultEl.innerHTML = `<div class="result-banner result-win">🏆 HAI VINTO ${formatCurrency(win)}! (${mult}x)</div>`;
      }
      // VFX
      if (win > 100) VFX.celebrate();
      VFX.floatFromElement(document.getElementById('spin-btn'), win, 'win');
      showToast(`🎰 ${newReels.join('')} — Vinto ${formatCurrency(win)}!`, 'win');
      State.recordHistory({ game: 'Slot Machine', bet, result: 'win', gain: win - bet });

      // Update jackpot display
      if (mult === 200) {
        localStorage.setItem('betcenter_jackpot', '1000');
        VFX.jackpotFlash('jackpot-display');
      } else {
        const jp = parseFloat(localStorage.getItem('betcenter_jackpot') || '12450') + bet * 0.05;
        localStorage.setItem('betcenter_jackpot', jp.toFixed(2));
        const jpEl = document.getElementById('jackpot-display');
        if (jpEl) jpEl.textContent = formatCurrency(jp);
      }
    } else {
      const resultEl = document.getElementById('slot-result');
      if (resultEl) {
        resultEl.innerHTML = `<div class="result-banner result-lose">Nessuna vincita</div>`;
      }
      // Grow jackpot slightly on every spin
      const jp = parseFloat(localStorage.getItem('betcenter_jackpot') || '12450') + bet * 0.03;
      localStorage.setItem('betcenter_jackpot', jp.toFixed(2));
      const jpEl = document.getElementById('jackpot-display');
      if (jpEl) jpEl.textContent = formatCurrency(jp);
      State.recordHistory({ game: 'Slot Machine', bet, result: 'lose', gain: -bet });
    }

    // Update balance display
    const balEl = document.getElementById('slot-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    spinning = false;
    const spinBtn = document.getElementById('spin-btn');
    if (spinBtn) spinBtn.disabled = false;
  }

  function startReelSpin(reelIndex) {
    const reelEl = document.getElementById(`reel-${reelIndex}`);
    if (!reelEl) return;
    
    reelEl.classList.add('spinning');
    
    // Attiva le luci
    document.querySelectorAll('.light').forEach(light => light.classList.add('active'));
    
    // Cambia simbolo velocemente per dare effetto di rotazione
    const interval = setInterval(() => {
      const randomSymbol = SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)];
      reelEl.innerHTML = `<div class="reel-symbol">${randomSymbol}</div>`;
    }, 100);
    
    // Salva l'interval per poterlo fermare dopo
    reelEl.dataset.interval = interval;
  }

  function stopReelSpin(reelIndex, finalSymbol) {
    const reelEl = document.getElementById(`reel-${reelIndex}`);
    if (!reelEl) return;
    
    // Ferma l'interval
    const interval = reelEl.dataset.interval;
    if (interval) {
      clearInterval(parseInt(interval));
    }
    
    reelEl.classList.remove('spinning');
    reelEl.innerHTML = `<div class="reel-symbol final">${finalSymbol}</div>`;
    
    // Se è l'ultimo rullo, disattiva le luci
    if (reelIndex === 2) {
      setTimeout(() => {
        document.querySelectorAll('.light').forEach(light => light.classList.remove('active'));
      }, 300);
    }
  }

  return { render, spin, toggleAuto };
})();

function delay(ms) { return new Promise(r => setTimeout(r, ms)); }
