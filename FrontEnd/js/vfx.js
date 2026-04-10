// =============================================
// BetCenterNL — VISUAL EFFECTS ENGINE
// =============================================

const VFX = (() => {

  // ── PARTICLE CELEBRATION ──
  function celebrate(x, y) {
    const colors = ['#c8a96e','#e8c98e','#4ade80','#60a5fa','#f87171','#fb923c','#e879f9','#fff'];
    const container = document.createElement('div');
    container.className = 'win-particles';
    document.body.appendChild(container);

    for (let i = 0; i < 60; i++) {
      const p = document.createElement('div');
      p.className = 'particle';
      p.style.cssText = `
        left: ${Math.random() * 100}%;
        background: ${colors[Math.floor(Math.random() * colors.length)]};
        width: ${Math.random() * 8 + 4}px;
        height: ${Math.random() * 8 + 4}px;
        border-radius: ${Math.random() > 0.5 ? '50%' : '2px'};
        --tx: ${(Math.random() - 0.5) * 200}px;
        animation-duration: ${Math.random() * 1.5 + 1}s;
        animation-delay: ${Math.random() * 0.5}s;
      `;
      container.appendChild(p);
    }

    setTimeout(() => container.remove(), 3000);
  }

  // ── FLOATING WIN/LOSE NUMBERS ──
  function floatNumber(amount, type, x, y) {
    const el = document.createElement('div');
    el.className = `floating-number ${type}`;
    el.textContent = (type === 'win' ? '+' : '') + formatCurrency(amount);
    el.style.cssText = `left:${x}px;top:${y}px;`;
    document.body.appendChild(el);
    setTimeout(() => el.remove(), 1500);
  }

  function floatFromElement(el, amount, type) {
    if (!el) return;
    const rect = el.getBoundingClientRect();
    const x = rect.left + rect.width / 2 - 60;
    const y = rect.top;
    floatNumber(amount, type, x, y);
  }

  // ── JACKPOT FLASH ──
  function jackpotFlash(elId) {
    const el = document.getElementById(elId);
    if (!el) return;
    el.classList.add('jackpot-flash');
    setTimeout(() => el.classList.remove('jackpot-flash'), 1000);
  }

  // ── BALANCE COUNTER ANIMATION ──
  function animateBalance(from, to, duration = 600) {
    const start = performance.now();
    const update = (now) => {
      const t = Math.min(1, (now - start) / duration);
      const ease = 1 - Math.pow(1 - t, 3); // ease-out cubic
      const current = from + (to - from) * ease;
      const fmt = formatCurrency(current);
      document.querySelectorAll('#top-balance, #sidebar-balance').forEach(el => {
        if (el) el.textContent = fmt;
      });
      if (t < 1) requestAnimationFrame(update);
    };
    requestAnimationFrame(update);
  }

  // ── SLOT REEL SPIN EFFECT ──
  function spinReelEffect(reelEl, symbols, duration, onStop) {
    const frames = [symbols[Math.floor(Math.random()*symbols.length)]];
    let elapsed = 0;
    const interval = 60;
    const ticker = setInterval(() => {
      elapsed += interval;
      reelEl.textContent = symbols[Math.floor(Math.random()*symbols.length)];
      if (elapsed >= duration) {
        clearInterval(ticker);
        if (onStop) onStop();
      }
    }, interval);
  }

  // ── CARD DEAL SOUND (visual feedback) ──
  function cardDealt() {
    // Visual flash on deal
    const flash = document.createElement('div');
    flash.style.cssText = `
      position:fixed;inset:0;background:rgba(255,255,255,0.02);
      pointer-events:none;z-index:9999;
      animation:flash-fade 0.15s ease forwards;
    `;
    document.body.appendChild(flash);
    setTimeout(() => flash.remove(), 200);
  }

  // ── SCREEN SHAKE on big loss ──
  function screenShake() {
    const main = document.getElementById('main-content');
    if (!main) return;
    main.style.animation = 'screen-shake 0.4s ease';
    setTimeout(() => { main.style.animation = ''; }, 500);
  }

  // ── NUMBER ANIMATE for stats ──
  function animateNumber(el, target, decimals = 0) {
    if (!el) return;
    const from = parseFloat(el.textContent.replace(/[^0-9.-]/g,'')) || 0;
    const dur = 800;
    const start = performance.now();
    const tick = (now) => {
      const t = Math.min(1, (now - start) / dur);
      const ease = 1 - Math.pow(1 - t, 3);
      const val = from + (target - from) * ease;
      el.textContent = decimals > 0 ? val.toFixed(decimals) : Math.round(val).toLocaleString('it-IT');
      if (t < 1) requestAnimationFrame(tick);
    };
    requestAnimationFrame(tick);
  }

  return { celebrate, floatFromElement, jackpotFlash, animateBalance, spinReelEffect, cardDealt, screenShake, animateNumber };
})();

// Inject screen shake keyframe
const shakeStyle = document.createElement('style');
shakeStyle.textContent = `
  @keyframes screen-shake {
    0%,100%{transform:translateX(0)}
    20%{transform:translateX(-4px)}
    40%{transform:translateX(4px)}
    60%{transform:translateX(-3px)}
    80%{transform:translateX(3px)}
  }
  @keyframes flash-fade {
    from{opacity:1}to{opacity:0}
  }
`;
document.head.appendChild(shakeStyle);

// ── PATCH State.addBalance to trigger VFX + Audio + LiveFeed ──
const _origAdd = State.addBalance.bind(State);
State.addBalance = function(amount) {
  const before = this.balance;
  _origAdd(amount);
  VFX.animateBalance(before, this.balance);

  if (amount > 500) {
    VFX.celebrate();
    AudioEngine.play('jackpot');
    showBigWinMarquee && showBigWinMarquee(`🏆 ${State.user?.nome || 'Giocatore'} ha vinto ${formatCurrency(amount)}!`);
    LiveFeed && LiveFeed.addRealWin && LiveFeed.addRealWin(State.currentSection, amount);
  } else if (amount > 100) {
    VFX.celebrate();
    AudioEngine.play('bigWin');
  } else if (amount > 0) {
    AudioEngine.play('win');
  }

  if (typeof trackAchievements === 'function') trackAchievements();
};

const _origDeduct = State.deductBalance.bind(State);
State.deductBalance = function(amount) {
  const result = _origDeduct(amount);
  if (result && amount > 0) AudioEngine.play('chip');
  return result;
};
