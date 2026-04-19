// =============================================
// BetCenterNL — LEADERBOARD (collegato al backend)
// =============================================

const Leaderboard = (() => {
  let cachedData = null;
  let sortKey = 'balance';

  async function fetchAndRender(key = 'balance') {
    sortKey = key;
    const el = document.getElementById('leaderboard-container');
    if (!el) return;

    el.innerHTML = '<div class="info-box text-center">⏳ Caricamento classifica...</div>';

    try {
      const result = await API.getLeaderboard();
      if (!result.ok) throw new Error('API error');
      cachedData = result.leaderboard;
      el.innerHTML = buildTable(cachedData, key);
    } catch (err) {
      el.innerHTML = '<div class="info-box text-center">⚠️ Impossibile caricare la classifica. Assicurati che il backend sia avviato.</div>';
    }
  }

  function buildTable(data, key = 'balance') {
    if (!data || data.length === 0) {
      return '<div class="info-box text-center">Nessun giocatore in classifica ancora.</div>';
    }

    const sorted = [...data].sort((a, b) => b[key] - a[key]);
    const myId   = State.user?.id;
    const myRank = sorted.findIndex(p => p.id === myId) + 1;
    const rankIcons = ['🥇','🥈','🥉'];

    return `
      <div style="margin-bottom:2rem">
        <div style="display:flex;align-items:center;gap:1rem;margin-bottom:1rem;flex-wrap:wrap">
          <div style="font-family:var(--font-display);font-size:1.8rem;letter-spacing:0.05em">🏆 CLASSIFICA</div>
          <div style="display:flex;gap:0.5rem;margin-left:auto">
            <button class="filter-pill ${key==='balance'?'active':''}" onclick="Leaderboard.render('balance')">Per Saldo</button>
            <button class="filter-pill ${key==='wins'?'active':''}" onclick="Leaderboard.render('wins')">Per Vittorie</button>
            <button class="filter-pill ${key==='totalGain'?'active':''}" onclick="Leaderboard.render('totalGain')">Per Guadagno</button>
          </div>
        </div>

        ${myRank > 0 ? `
          <div style="background:var(--accent-bg);border:1px solid var(--accent);border-radius:var(--radius-sm);padding:0.75rem 1rem;margin-bottom:1rem;display:flex;align-items:center;gap:1rem;font-size:0.85rem">
            <span style="font-size:1.2rem">${rankIcons[myRank-1] || `#${myRank}`}</span>
            <span>Sei al <strong style="color:var(--accent)">${myRank}° posto</strong> in classifica!</span>
          </div>` : ''}

        <div class="leaderboard">
          <div class="leaderboard-header">
            <span style="width:40px">#</span>
            <span>Giocatore</span>
            <span style="margin-left:auto">${key==='balance'?'Saldo':key==='wins'?'Vittorie':'Guadagno'}</span>
          </div>
          ${sorted.map((p, i) => {
            const rank    = i + 1;
            const rankCls = rank===1?'gold':rank===2?'silver':rank===3?'bronze':'';
            const isMe    = p.id === myId;
            const val     = key==='balance' ? formatCurrency(p.balance)
                          : key==='wins'    ? p.wins
                          : formatCurrency(p.totalGain);
            return `
              <div class="leaderboard-row" style="${isMe?'background:var(--accent-bg);border-left:3px solid var(--accent)':''}">
                <div class="lb-rank ${rankCls}">${rankIcons[i] || rank}</div>
                <div class="lb-name">
                  ${p.username || p.nome}${isMe?' <span style="color:var(--accent);font-size:0.7rem">(Tu)</span>':''}
                  <div style="font-size:0.7rem;color:var(--text-3)">${p.gamesPlayed} partite · ${p.winRate}% vittorie</div>
                </div>
                <div class="lb-value">${val}</div>
              </div>`;
          }).join('')}
        </div>
      </div>`;
  }

  function render(key = 'balance') {
    if (cachedData) {
      const el = document.getElementById('leaderboard-container');
      if (el) el.innerHTML = buildTable(cachedData, key);
    }
    fetchAndRender(key);
  }

  function getHTML() {
    // Avvia il fetch in background
    setTimeout(() => fetchAndRender(sortKey), 0);
    return `<div id="leaderboard-container"><div class="info-box text-center">⏳ Caricamento classifica...</div></div>`;
  }

  return { render, getHTML };
})();
