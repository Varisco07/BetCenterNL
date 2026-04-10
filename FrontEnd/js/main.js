// =============================================
// BetCenterNL — MAIN CONTROLLER (DEFINITIVO)
// =============================================

function initApp() {
  initTicker();
  navigateTo('lobby');
  setupNavListeners();
  setTimeout(() => { try { LiveFeed.start(); } catch(_) {} }, 2000);
  setTimeout(() => {
    const badge = document.getElementById('feed-badge');
    if (badge) badge.style.display = 'flex';
  }, 4000);
  try { DailyBonus.checkOnLogin(); } catch(_) {}
  try { _achievementCount = LevelSystem.getUnlocked().length; } catch(_) {}
}

function setupNavListeners() {
  document.querySelectorAll('.nav-item').forEach(item => {
    item.addEventListener('click', e => {
      e.preventDefault();
      const s = item.dataset.section;
      if (s) navigateTo(s);
      if (window.innerWidth < 700)
        document.getElementById('sidebar')?.classList.remove('open');
    });
  });
}

function navigateTo(section) {
  State.currentSection = section;
  updateNavActive(section);
  updateMobileNav(section);
  renderSection(section);
  try { AudioEngine.play('click'); } catch(_) {}
  const badge = document.getElementById('feed-badge');
  if (badge) badge.style.display = 'none';
}

function updateNavActive(section) {
  document.querySelectorAll('.nav-item').forEach(item =>
    item.classList.toggle('active', item.dataset.section === section)
  );
}

function updateMobileNav(section) {
  document.querySelectorAll('.mob-nav-btn').forEach(btn =>
    btn.classList.toggle('active', btn.dataset.mob === section)
  );
}

function renderSection(section) {
  const ca = document.getElementById('content-area');
  if (!ca) return;

  let html = '';
  try {
    switch (section) {
      case 'lobby':       html = Sections.lobby();                     break;
      case 'slots':       html = SlotGame.render();                    break;
      case 'blackjack':   html = BlackjackGame.render();               break;
      case 'poker':       html = PokerGame.render();                   break;
      case 'roulette':    html = RouletteGame.render();                break;
      case 'dadi':        html = DadiGame.render();                    break;
      case 'baccarat':    html = BaccaratGame.render();                break;
      case 'calcio':      html = VirtualSports.renderFootball();       break;
      case 'tennis':      html = VirtualSports.renderTennis();         break;
      case 'basket':      html = VirtualSports.renderBasket();         break;
      case 'cavalli':     html = VirtualSports.renderRace('cavalli');  break;
      case 'cani':        html = VirtualSports.renderRace('cani');     break;
      case 'wallet':      html = Sections.wallet();                    break;
      case 'history':     html = Sections.history();                   break;
      case 'leaderboard': html = Sections.leaderboard();               break;
      case 'responsible': html = Sections.responsible();               break;
      case 'bonus':
        try { DailyBonus.showModal(); } catch(_) {}
        html = Sections.wallet();
        break;
      default:            html = Sections.lobby();
    }
  } catch (e) {
    console.error('[BetCenterNL] Render error in section', section, e);
    html = `<div class="info-box" style="text-align:center;margin-top:2rem">
      ⚠️ Errore nel caricamento della sezione. <button class="btn-ghost" onclick="navigateTo('lobby')" style="margin-left:0.5rem">Torna alla Lobby</button>
    </div>`;
  }

  ca.innerHTML = html;
  ca.scrollTop = 0;

  // Post-render inits (guarded)
  if (section === 'slots')  try { SlotGame.init  && SlotGame.init();  } catch(_) {}
  if (section === 'blackjack')  try { BlackjackGame.init  && BlackjackGame.init();  } catch(_) {}
}

// ── BIG WIN MARQUEE ──
function showBigWinMarquee(msg) {
  try {
    const el = document.getElementById('big-win-marquee');
    const txt = document.getElementById('big-win-text');
    if (!el || !txt) return;
    txt.textContent = msg;
    el.classList.remove('hidden');
    setTimeout(() => el.classList.add('hidden'), 7000);
  } catch(_) {}
}

// ── KEYBOARD SHORTCUTS ──
document.addEventListener('keydown', e => {
  if (!State.user) return;
  if (['INPUT','SELECT','TEXTAREA'].includes(e.target.tagName)) return;

  try {
    switch (e.key) {
      case 'Escape':
        closeDeposit();
        const dm = document.getElementById('daily-modal');
        if (dm) dm.remove();
        break;
      case 'Enter': {
        const actions = {
          slots:     () => SlotGame.spin(),
          blackjack: () => BlackjackGame.deal(),
          poker:     () => PokerGame.dealDraw(),
          dadi:      () => DadiGame.roll(),
          baccarat:  () => BaccaratGame.play(),
        };
        const fn = actions[State.currentSection];
        if (fn) fn();
        break;
      }
      case 'h': case 'H':
        if (State.currentSection === 'blackjack') BlackjackGame.hit();
        break;
      case 's': case 'S':
        if (State.currentSection === 'blackjack') BlackjackGame.stand();
        break;
      case 'd': case 'D':
        if (State.currentSection === 'blackjack') BlackjackGame.double();
        break;
    }
  } catch(_) {}
});

// ── SAVE ON UNLOAD ──
window.addEventListener('beforeunload', () => {
  try { if (State.user) State.save(); } catch(_) {}
});

// ── RESPONSIVE ──
window.addEventListener('resize', () => {
  if (window.innerWidth > 700)
    document.getElementById('sidebar')?.classList.remove('open');
});

// ── CLOSE MODALS ON BACKDROP CLICK ──
document.addEventListener('click', e => {
  if (e.target.id === 'deposit-modal') closeDeposit();
  if (e.target.id === 'daily-modal') {
    const m = document.getElementById('daily-modal');
    if (m) m.remove();
  }
});
