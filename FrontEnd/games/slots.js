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
        <div style="text-align:center;margin-bottom:1rem">
          <div style="font-size:0.72rem;letter-spacing:0.15em;text-transform:uppercase;color:var(--text-2)">🏆 JACKPOT PROGRESSIVO</div>
          <div style="font-family:var(--font-display);font-size:2.8rem;color:var(--accent);letter-spacing:0.05em;text-shadow:0 0 32px rgba(200,169,110,0.4)" id="jackpot-display">${formatCurrency(parseFloat(jackpot))}</div>
        </div>

        <div class="slot-machine">
          <div class="slot-lines">5 LINEE • AUTO SPIN • 8 SIMBOLI</div>
          <div class="slot-reels">
            <div class="slot-reel"><div class="reel-window" id="reel-0">🍒</div></div>
            <div class="slot-reel"><div class="reel-window" id="reel-1">🍒</div></div>
            <div class="slot-reel"><div class="reel-window" id="reel-2">🍒</div></div>
          </div>
          <div id="slot-result"></div>
          ${createBetControls('slot', 5)}
          <div class="game-btn-row">
            <button class="btn-game btn-deal" id="spin-btn" onclick="SlotGame.spin()">🎰 GIRA!</button>
            <button class="btn-game btn-double" id="auto-btn" onclick="SlotGame.toggleAuto()">AUTO: OFF</button>
          </div>
          <div class="hotkey-hint"><span class="key-badge">Enter</span> per girare rapidamente</div>
        </div>

        <!-- PAYTABLE -->
        <div style="margin-top:1.25rem">
          <div style="font-size:0.72rem;letter-spacing:0.12em;text-transform:uppercase;color:var(--text-2);margin-bottom:0.5rem">TABELLA PAGAMENTI</div>
          <div class="slot-multiplier">
            <div class="mult-item">🍒🍒🍒 → 5x</div>
            <div class="mult-item">🍋🍋🍋 → 8x</div>
            <div class="mult-item">🍊🍊🍊 → 10x</div>
            <div class="mult-item">🍇🍇🍇 → 15x</div>
            <div class="mult-item">⭐⭐⭐ → 25x</div>
            <div class="mult-item">💎💎💎 → 50x</div>
            <div class="mult-item">7️⃣7️⃣7️⃣ → 100x</div>
            <div class="mult-item">🔔🔔🔔 → 200x</div>
            <div class="mult-item">🍒🍒? → 1.5x</div>
          </div>
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
      btn.textContent = 'AUTO SPIN: ON';
      btn.style.background = 'var(--red-bg)';
      btn.style.borderColor = 'var(--red)';
      btn.style.color = 'var(--red)';
      autoSpinInterval = setInterval(() => { if (!spinning) spin(); }, 1500);
    } else {
      btn.textContent = 'AUTO SPIN: OFF';
      btn.style.background = '';
      btn.style.borderColor = '';
      btn.style.color = '';
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

    // Animate
    const reelEls = [0,1,2].map(i => document.getElementById(`reel-${i}`));
    reelEls.forEach(el => { if(el) el.classList.add('spinning'); });

    const newReels = [weightedRandom(), weightedRandom(), weightedRandom()];

    // Stagger stop
    for (let i = 0; i < 3; i++) {
      await delay(400 + i * 250);
      const el = document.getElementById(`reel-${i}`);
      if (el) {
        el.classList.remove('spinning');
        el.textContent = newReels[i];
        AudioEngine.play('slotStop');
      }
    }

    currentReels = newReels;
    const mult = checkWin(newReels);

    if (mult > 0) {
      const win = parseFloat((bet * mult).toFixed(2));
      State.addBalance(win);
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

  return { render, spin, toggleAuto };
})();

function delay(ms) { return new Promise(r => setTimeout(r, ms)); }
