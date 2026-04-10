// =============================================
// BetCenterNL — LEVEL & ACHIEVEMENT SYSTEM
// =============================================

const LevelSystem = (() => {
  const LEVELS = [
    { level: 1,  name: 'Novizio',       min: 0,     icon: '🌱', color: '#74748a' },
    { level: 2,  name: 'Apprentista',   min: 500,   icon: '🎯', color: '#60a5fa' },
    { level: 3,  name: 'Giocatore',     min: 1500,  icon: '🎮', color: '#4ade80' },
    { level: 4,  name: 'Veterano',      min: 4000,  icon: '⚡', color: '#fb923c' },
    { level: 5,  name: 'Esperto',       min: 10000, icon: '💎', color: '#c084fc' },
    { level: 6,  name: 'Campione',      min: 25000, icon: '👑', color: '#c8a96e' },
    { level: 7,  name: 'Leggenda',      min: 75000, icon: '🏆', color: '#e8c98e' },
  ];

  const ACHIEVEMENTS = [
    { id: 'first_win',       name: 'Prima Vittoria',      icon: '🎉', desc: 'Vinci la tua prima scommessa',           check: (s) => s.history.some(h => h.result === 'win') },
    { id: 'high_roller',     name: 'High Roller',          icon: '💸', desc: 'Scommetti €100 in una mano',             check: (s) => s.history.some(h => h.bet >= 100) },
    { id: 'streak_3',        name: 'Tre di Fila',          icon: '🔥', desc: 'Vinci 3 scommesse consecutive',          check: (s) => hasStreak(s, 3) },
    { id: 'blackjack',       name: 'Blackjack!',           icon: '🃏', desc: 'Fai un blackjack naturale',              check: (s) => s.history.some(h => h.game === 'Blackjack' && h.result === 'win' && h.gain > h.bet) },
    { id: 'jackpot',         name: 'Jackpottaro',          icon: '🎰', desc: 'Vinci più di €500 alle slot',            check: (s) => s.history.some(h => h.game === 'Slot Machine' && h.gain > 500) },
    { id: 'poker_royal',     name: 'Royal Flush',          icon: '♠',  desc: 'Ottieni un Royal Flush al poker',        check: (s) => s.history.some(h => h.game === 'Video Poker' && h.gain >= h.bet * 800) },
    { id: 'multi_bet',       name: 'Combinatore',          icon: '🎲', desc: 'Vinci una multi-scommessa (3+ eventi)',  check: (s) => s.history.some(h => h.game === 'Virtual Sports' && h.gain > h.bet * 5) },
    { id: 'total_100',       name: 'Centurione',           icon: '💯', desc: 'Gioca 100 scommesse totali',             check: (s) => s.history.length >= 100 },
    { id: 'balance_5000',    name: 'Ricco Sfondato',       icon: '🤑', desc: 'Raggiungi €5.000 di saldo',              check: (s) => s.balance >= 5000 },
    { id: 'zero_hero',       name: 'Zero Hero',            icon: '🎰', desc: 'Indovina lo zero alla roulette',         check: (s) => s.history.some(h => h.game === 'Roulette' && h.gain > h.bet * 30) },
    { id: 'race_winner',     name: 'Appassionato di Gare', icon: '🏁', desc: 'Vinci una corsa di cavalli',             check: (s) => s.history.some(h => h.game === 'Corse Cavalli' && h.result === 'win') },
    { id: 'baccarat_tie',    name: 'Puntatore Audace',     icon: '💎', desc: 'Vinci puntando sul pareggio al baccarat',check: (s) => s.history.some(h => h.game === 'Baccarat' && h.gain >= h.bet * 8) },
  ];

  function hasStreak(s, n) {
    let streak = 0;
    for (const h of s.history) {
      if (h.result === 'win') { streak++; if (streak >= n) return true; }
      else streak = 0;
    }
    return false;
  }

  function getTotalXP() {
    const wins = State.history.filter(h => h.result === 'win').length;
    const bets = State.history.length;
    const gainXP = Math.max(0, State.history.reduce((s, h) => s + (h.gain || 0), 0));
    return Math.floor(wins * 100 + bets * 20 + gainXP * 0.5);
  }

  function getCurrentLevel(xp) {
    let current = LEVELS[0];
    for (const l of LEVELS) {
      if (xp >= l.min) current = l;
    }
    return current;
  }

  function getNextLevel(xp) {
    for (const l of LEVELS) {
      if (l.min > xp) return l;
    }
    return null;
  }

  function getUnlocked() {
    return ACHIEVEMENTS.filter(a => {
      try { return a.check(State); } catch { return false; }
    });
  }

  function getLevelWidget() {
    const xp = getTotalXP();
    const current = getCurrentLevel(xp);
    const next = getNextLevel(xp);
    const progress = next ? Math.min(100, ((xp - current.min) / (next.min - current.min)) * 100) : 100;

    return `
      <div class="level-widget">
        <div class="level-badge" style="background:${current.color}18;font-size:1.6rem">
          ${current.icon}
        </div>
        <div class="level-info">
          <div class="level-title">Livello ${current.level} — ${current.name}</div>
          <div class="level-subtitle">${next ? `Prossimo: ${next.name} (Lv.${next.level})` : '🏆 Livello Massimo!'}</div>
          <div class="level-bar">
            <div class="level-bar-fill" style="width:${progress}%"></div>
          </div>
          <div class="level-xp-text">${xp.toLocaleString('it-IT')} XP${next ? ` / ${next.min.toLocaleString('it-IT')} XP` : ''}</div>
        </div>
        <div style="text-align:right;flex-shrink:0">
          <div style="font-family:var(--font-display);font-size:1.4rem;color:var(--accent)">${getUnlocked().length}</div>
          <div style="font-size:0.72rem;color:var(--text-2)">/ ${ACHIEVEMENTS.length}<br>Traguardi</div>
        </div>
      </div>`;
  }

  function getAchievementsHTML() {
    const unlocked = new Set(getUnlocked().map(a => a.id));
    return `
      <div class="page-header" style="margin-top:1.5rem">
        <h3 style="font-family:var(--font-display);font-size:1.5rem;letter-spacing:0.05em">🏅 TRAGUARDI</h3>
        <p style="font-size:0.82rem;color:var(--text-2)">${unlocked.size} di ${ACHIEVEMENTS.length} sbloccati</p>
      </div>
      <div class="achievements-grid">
        ${ACHIEVEMENTS.map(a => `
          <div class="achievement-card ${unlocked.has(a.id) ? 'unlocked' : 'locked'}">
            ${unlocked.has(a.id) ? '<span class="achievement-checkmark">✓</span>' : ''}
            <span class="achievement-icon">${a.icon}</span>
            <div class="achievement-name">${a.name}</div>
            <div class="achievement-desc">${a.desc}</div>
          </div>
        `).join('')}
      </div>`;
  }

  // Check and notify new achievements
  function checkNewAchievements(prevCount) {
    const unlocked = getUnlocked();
    if (unlocked.length > prevCount) {
      const newest = unlocked.slice(prevCount);
      newest.forEach(a => {
        setTimeout(() => {
          showToast(`🏅 Traguardo sbloccato: "${a.name}" ${a.icon}`, 'win', 4000);
        }, 500);
      });
    }
    return unlocked.length;
  }

  return { getLevelWidget, getAchievementsHTML, checkNewAchievements, getUnlocked, getTotalXP, getCurrentLevel };
})();

// Track achievement count
let _achievementCount = 0;
function trackAchievements() {
  _achievementCount = LevelSystem.checkNewAchievements(_achievementCount);
}
