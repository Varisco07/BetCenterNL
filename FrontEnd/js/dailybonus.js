// =============================================
// BetCenterNL — DAILY BONUS SYSTEM
// =============================================

const DailyBonus = (() => {
  const BONUSES = [50, 75, 100, 150, 200, 300, 500]; // 7-day streak

  function getKey() { return `betcenter_daily_${State.user?.username || 'demo'}`; }

  function getData() {
    try {
      const raw = localStorage.getItem(getKey());
      return raw ? JSON.parse(raw) : { streak: 0, lastClaim: null };
    } catch { return { streak: 0, lastClaim: null }; }
  }

  function saveData(data) {
    localStorage.setItem(getKey(), JSON.stringify(data));
  }

  function canClaim() {
    const data = getData();
    if (!data.lastClaim) return true;
    const last = new Date(data.lastClaim);
    const now = new Date();
    // Check if it's a new day
    return last.toDateString() !== now.toDateString();
  }

  function isStreakBroken() {
    const data = getData();
    if (!data.lastClaim) return false;
    const last = new Date(data.lastClaim);
    const now = new Date();
    const diffDays = Math.floor((now - last) / (1000 * 60 * 60 * 24));
    return diffDays > 1;
  }

  function claim() {
    if (!canClaim()) {
      showToast('Hai già ritirato il bonus oggi! Torna domani.', 'info');
      return;
    }
    const data = getData();
    if (isStreakBroken()) data.streak = 0;
    data.streak = Math.min(data.streak + 1, 7);
    data.lastClaim = new Date().toISOString();

    const amount = BONUSES[data.streak - 1];
    State.addBalance(amount);
    saveData(data);
    closeDailyModal();
    AudioEngine.play('bigWin');
    showToast(`🎁 Bonus giornaliero ritirato: +${formatCurrency(amount)}! (Giorno ${data.streak}/7)`, 'win', 5000);
    State.recordHistory({ game: 'Bonus Giornaliero', bet: 0, result: 'win', gain: amount });
  }

  function showModal() {
    if (!canClaim()) return;
    const data = getData();
    const streak = isStreakBroken() ? 0 : data.streak;

    const modal = document.createElement('div');
    modal.id = 'daily-modal';
    modal.className = 'modal-overlay';
    modal.innerHTML = `
      <div class="modal-box" style="max-width:460px">
        <div class="modal-header">
          <h3 style="font-family:var(--font-display);font-size:1.6rem;letter-spacing:0.05em">🎁 BONUS GIORNALIERO</h3>
          <button onclick="closeDailyModal()">✕</button>
        </div>
        <div style="text-align:center;padding:0.5rem 0 1rem">
          <p style="color:var(--text-2);font-size:0.85rem;margin-bottom:1.25rem">Ritira il tuo bonus ogni giorno. Mantieni la serie per premi crescenti!</p>
          <div style="display:grid;grid-template-columns:repeat(7,1fr);gap:0.4rem;margin-bottom:1.5rem">
            ${BONUSES.map((b, i) => {
              const day = i + 1;
              const isPast = day < streak + 1;
              const isToday = day === streak + 1;
              const isFuture = day > streak + 1;
              return `
                <div style="
                  background:${isPast ? 'var(--green-bg)' : isToday ? 'var(--accent-bg)' : 'var(--bg-3)'};
                  border:1px solid ${isPast ? 'var(--green)' : isToday ? 'var(--accent)' : 'var(--border)'};
                  border-radius:var(--radius-sm);
                  padding:0.6rem 0.3rem;
                  text-align:center;
                  opacity:${isFuture ? 0.5 : 1};
                ">
                  <div style="font-size:0.65rem;color:var(--text-2);margin-bottom:0.25rem">G${day}</div>
                  <div style="font-size:${isToday ? '1.1rem' : '0.85rem'};color:${isPast ? 'var(--green)' : isToday ? 'var(--accent)' : 'var(--text-1)'}">
                    ${isPast ? '✓' : isToday ? '🎁' : `€${b}`}
                  </div>
                  ${isToday ? `<div style="font-size:0.65rem;color:var(--accent);margin-top:0.2rem">€${b}</div>` : ''}
                </div>`;
            }).join('')}
          </div>
          <div style="font-size:1rem;color:var(--text-1);margin-bottom:1rem">
            Oggi ricevi: <strong style="color:var(--accent);font-family:var(--font-mono);font-size:1.3rem">+${formatCurrency(BONUSES[Math.min(streak, 6)])}</strong>
          </div>
          ${streak >= 6 ? `<div style="color:var(--accent);font-size:0.82rem;margin-bottom:0.75rem">🔥 Ultima giornata della serie! Massimo bonus!</div>` : ''}
          <button class="btn-primary btn-full" onclick="DailyBonus.claim()">🎁 Ritira Bonus</button>
        </div>
      </div>`;
    document.body.appendChild(modal);
  }

  function closeDailyModal() {
    const m = document.getElementById('daily-modal');
    if (m) m.remove();
  }

  function checkOnLogin() {
    setTimeout(() => {
      if (canClaim()) showModal();
    }, 1500);
  }

  return { claim, showModal, canClaim, checkOnLogin };
})();

function closeDailyModal() { DailyBonus.closeDailyModal && DailyBonus.closeDailyModal(); const m = document.getElementById('daily-modal'); if(m) m.remove(); }
