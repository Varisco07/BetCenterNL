// =============================================
// BetCenterNL — SLOT MACHINE (collegata al backend)
// =============================================

const SlotGame = (() => {
  const SYMBOLS = ['🍒','🍋','🍊','🍇','⭐','💎','7️⃣','🔔'];
  const PAYTABLE = {
    '🍒🍒🍒':5,'🍋🍋🍋':8,'🍊🍊🍊':10,'🍇🍇🍇':15,
    '⭐⭐⭐':25,'💎💎💎':50,'7️⃣7️⃣7️⃣':100,'🔔🔔🔔':200
  };

  let spinning = false;
  let autoOn = false;
  let autoSpinInterval = null;

  function render() {
    // Carica jackpot dal server
    API.getJackpot().then(r => {
      if (r.ok) {
        const jpEl = document.getElementById('jackpot-display');
        if (jpEl) jpEl.textContent = formatCurrency(r.jackpot);
      }
    }).catch(() => {});

    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">🎰 SLOT MACHINE</h2>
          <div class="game-balance" id="slot-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="jackpot-container">
          <div class="jackpot-label">🏆 JACKPOT PROGRESSIVO 🏆</div>
          <div class="jackpot-amount" id="jackpot-display">⏳</div>
          <div class="jackpot-subtitle">Vinci con 🔔🔔🔔</div>
        </div>
        <div class="slot-machine">
          <div class="slot-machine-frame">
            <div class="slot-machine-top">
              <div class="slot-lights">
                <span class="light"></span><span class="light"></span>
                <span class="light"></span><span class="light"></span><span class="light"></span>
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
            <div class="slot-machine-bottom"><div id="slot-result"></div></div>
          </div>
          ${createBetControls('slot', 5)}
          <div class="game-btn-row">
            <button class="btn-game btn-deal btn-spin-large" id="spin-btn" onclick="SlotGame.spin()">
              <span class="btn-icon">🎰</span><span class="btn-text">GIRA!</span>
            </button>
            <button class="btn-game btn-double" id="auto-btn" onclick="SlotGame.toggleAuto()">
              <span class="btn-icon">⚡</span><span class="btn-text">AUTO: OFF</span>
            </button>
          </div>
          <div class="hotkey-hint"><span class="key-badge">Enter</span> per girare rapidamente</div>
        </div>
        <div class="paytable-container">
          <div class="paytable-header">💰 TABELLA PAGAMENTI</div>
          <div class="paytable-grid">
            ${Object.entries(PAYTABLE).map(([k,v]) => `
              <div class="paytable-item ${v>=100?'premium':v>=25?'high':v>=10?'medium':'low'}">
                <div class="paytable-symbols">${k}</div>
                <div class="paytable-multiplier">${k === '🔔🔔🔔' ? '🏆 JACKPOT' : v + 'x'}</div>
              </div>`).join('')}
          </div>
          <div class="paytable-note">💡 Due 🍒 in qualsiasi posizione pagano 1.5x!</div>
        </div>
      </div>`;
  }

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
    if (State.balance < bet) { showToast('Saldo insufficiente!', 'lose'); if (autoOn) toggleAuto(); return; }

    spinning = true;
    document.getElementById('spin-btn').disabled = true;
    document.getElementById('slot-result').innerHTML = '';
    try { AudioEngine.play('spin'); } catch (_) {}

    // Avvia animazione rulli
    for (let i = 0; i < 3; i++) startReelSpin(i);

    let result;
    try {
      result = await API.spinSlots(bet);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      for (let i = 0; i < 3; i++) stopReelSpin(i, SYMBOLS[0]);
      spinning = false;
      document.getElementById('spin-btn').disabled = false;
      return;
    }

    // Ferma rulli con i risultati del server
    for (let i = 0; i < 3; i++) {
      await delay(800 + i * 400);
      stopReelSpin(i, result.reels[i]);
      try { AudioEngine.play('slotStop'); } catch (_) {}
    }

    // Sincronizza saldo
    State.syncFromServer(result.newBalance);

    // Aggiorna jackpot dal server
    const jpEl = document.getElementById('jackpot-display');
    if (jpEl && result.jackpot) jpEl.textContent = formatCurrency(result.jackpot);

    if (result.win) {
      const win = parseFloat((bet * result.multiplier).toFixed(2));
      [0,1,2].forEach(i => {
        const reel = document.getElementById(`reel-${i}`);
        if (reel) { reel.classList.add('winning'); setTimeout(() => reel.classList.remove('winning'), 2000); }
      });

      const winLabel = result.jackpotWon
        ? `🏆 JACKPOT! ${formatCurrency(win)}`
        : `🏆 HAI VINTO ${formatCurrency(win)}! (${result.multiplier}x)`;

      document.getElementById('slot-result').innerHTML =
        `<div class="result-banner result-win">${winLabel}</div>`;
      if (win > 100) try { VFX.celebrate(); } catch (_) {}
      try { VFX.floatFromElement(document.getElementById('spin-btn'), win, 'win'); } catch (_) {}
      showToast(result.jackpotWon ? `🏆 JACKPOT! +${formatCurrency(win)}` : `🎰 ${result.reels.join('')} — Vinto ${formatCurrency(win)}!`, 'win');
      State.recordHistory({ game: 'Slot Machine', bet, result: 'win', gain: result.gain });
      if (result.jackpotWon) {
        try { VFX.jackpotFlash('jackpot-display'); } catch (_) {}
        showToast('🏆 HAI VINTO IL JACKPOT!', 'win', 6000);
      }
    } else {
      document.getElementById('slot-result').innerHTML =
        `<div class="result-banner result-lose">Nessuna vincita</div>`;
      State.recordHistory({ game: 'Slot Machine', bet, result: 'lose', gain: result.gain });
    }

    const balEl = document.getElementById('slot-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    spinning = false;
    document.getElementById('spin-btn').disabled = false;
  }

  function startReelSpin(reelIndex) {
    const reelEl = document.getElementById(`reel-${reelIndex}`);
    if (!reelEl) return;
    reelEl.classList.add('spinning');
    document.querySelectorAll('.light').forEach(l => l.classList.add('active'));
    const interval = setInterval(() => {
      reelEl.innerHTML = `<div class="reel-symbol">${SYMBOLS[Math.floor(Math.random()*SYMBOLS.length)]}</div>`;
    }, 100);
    reelEl.dataset.interval = interval;
  }

  function stopReelSpin(reelIndex, finalSymbol) {
    const reelEl = document.getElementById(`reel-${reelIndex}`);
    if (!reelEl) return;
    clearInterval(parseInt(reelEl.dataset.interval));
    reelEl.classList.remove('spinning');
    reelEl.innerHTML = `<div class="reel-symbol final">${finalSymbol}</div>`;
    if (reelIndex === 2) {
      setTimeout(() => document.querySelectorAll('.light').forEach(l => l.classList.remove('active')), 300);
    }
  }

  return { render, spin, toggleAuto };
})();

function delay(ms) { return new Promise(r => setTimeout(r, ms)); }
