// =============================================
// BetCenterNL — LEADERBOARD
// =============================================

const Leaderboard = (() => {

  // Fake leaderboard data (simulates other players)
  const FAKE_PLAYERS = [
    { name: 'Marco_R',    wins: 342, gain: 18450, level: 7 },
    { name: 'Sofia99',    wins: 289, gain: 12300, level: 6 },
    { name: 'Gianni_B',   wins: 251, gain: 9800,  level: 6 },
    { name: 'Laura_V',    wins: 198, gain: 7650,  level: 5 },
    { name: 'Alex_K',     wins: 175, gain: 6200,  level: 5 },
    { name: 'Federica_M', wins: 162, gain: 5400,  level: 4 },
    { name: 'Luca_MT',    wins: 143, gain: 4700,  level: 4 },
    { name: 'Chiara_P',   wins: 128, gain: 3900,  level: 3 },
    { name: 'Dario_T',    wins: 112, gain: 3100,  level: 3 },
    { name: 'Elena_S',    wins: 98,  gain: 2600,  level: 3 },
  ];

  function getPlayerEntry() {
    const wins = State.history.filter(h => h.result === 'win').length;
    const gain = Math.max(0, State.history.reduce((s, h) => s + (h.gain || 0), 0));
    const xp = LevelSystem.getTotalXP();
    const level = LevelSystem.getCurrentLevel(xp);
    return {
      name: State.user?.username || State.user?.nome || 'Tu',
      wins,
      gain,
      level: level.level,
      isMe: true
    };
  }

  function buildTable(sortKey = 'gain') {
    const me = getPlayerEntry();
    const all = [...FAKE_PLAYERS.map(p => ({ ...p, isMe: false })), me];
    all.sort((a, b) => b[sortKey] - a[sortKey]);

    const rankIcons = ['🥇', '🥈', '🥉'];
    const myRank = all.findIndex(p => p.isMe) + 1;

    return `
      <div style="margin-bottom:2rem">
        <div style="display:flex;align-items:center;gap:1rem;margin-bottom:1rem;flex-wrap:wrap">
          <div style="font-family:var(--font-display);font-size:1.8rem;letter-spacing:0.05em">🏆 CLASSIFICA</div>
          <div style="display:flex;gap:0.5rem;margin-left:auto">
            <button class="filter-pill ${sortKey==='gain'?'active':''}" onclick="Leaderboard.render('gain')">Per Guadagno</button>
            <button class="filter-pill ${sortKey==='wins'?'active':''}" onclick="Leaderboard.render('wins')">Per Vittorie</button>
          </div>
        </div>

        ${me.wins > 0 ? `
          <div style="background:var(--accent-bg);border:1px solid var(--accent);border-radius:var(--radius-sm);padding:0.75rem 1rem;margin-bottom:1rem;display:flex;align-items:center;gap:1rem;font-size:0.85rem">
            <span style="font-size:1.2rem">${rankIcons[myRank-1] || `#${myRank}`}</span>
            <span>Sei al <strong style="color:var(--accent)">${myRank}° posto</strong> in classifica!</span>
          </div>` : ''}

        <div class="leaderboard">
          <div class="leaderboard-header">
            <span style="width:40px">#</span>
            <span>Giocatore</span>
            <span style="margin-left:auto">${sortKey === 'gain' ? 'Guadagno' : 'Vittorie'}</span>
          </div>
          ${all.map((p, i) => {
            const rank = i + 1;
            const rankCls = rank === 1 ? 'gold' : rank === 2 ? 'silver' : rank === 3 ? 'bronze' : '';
            return `
              <div class="leaderboard-row" style="${p.isMe ? 'background:var(--accent-bg);border-left:3px solid var(--accent)' : ''}">
                <div class="lb-rank ${rankCls}">${rankIcons[i] || rank}</div>
                <div class="lb-name">
                  ${p.name}${p.isMe ? ' <span style="color:var(--accent);font-size:0.7rem">(Tu)</span>' : ''}
                  <div style="font-size:0.7rem;color:var(--text-3)">Livello ${p.level} · ${p.wins} vittorie</div>
                </div>
                <div class="lb-value">${sortKey === 'gain' ? formatCurrency(p.gain) : p.wins}</div>
              </div>`;
          }).join('')}
        </div>
      </div>`;
  }

  function render(sortKey = 'gain') {
    const el = document.getElementById('leaderboard-container');
    if (el) {
      el.innerHTML = buildTable(sortKey);
    }
  }

  function getHTML(sortKey = 'gain') {
    return `<div id="leaderboard-container">${buildTable(sortKey)}</div>`;
  }

  return { render, getHTML };
})();
