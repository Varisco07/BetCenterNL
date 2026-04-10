// =============================================
// BetCenterNL — VIRTUAL SPORTS ENGINE
// =============================================

const VirtualSports = (() => {

  // ============ DATA GENERATORS ============

  function rnd(min, max) { return parseFloat((Math.random() * (max - min) + min).toFixed(2)); }
  function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
  function fmt2(n) { return n.toFixed(2); }

  const FOOTBALL_TEAMS = [
    'FC Roma V', 'Milan United V', 'Juventus City V', 'Inter Blue V',
    'Napoli Stars V', 'Lazio FC V', 'Fiorentina V', 'Torino FC V',
    'Atalanta V', 'Sampdoria V', 'Genoa Virtual', 'Bologna FC V',
    'Verona FC V', 'Spezia V', 'Venezia V', 'Empoli V'
  ];

  const TENNIS_PLAYERS = [
    'Djokovic V', 'Federer V', 'Nadal V', 'Murray V',
    'Alcaraz V', 'Sinner V', 'Medvedev V', 'Zverev V',
    'Tsitsipas V', 'Rublev V', 'Berrettini V', 'Shapovalov V'
  ];

  const BASKET_TEAMS = [
    'Lakers Virtual', 'Celtics Virtual', 'Bulls Virtual', 'Warriors Virtual',
    'Heat Virtual', 'Nets Virtual', 'Knicks Virtual', 'Bucks Virtual'
  ];

  const HORSE_NAMES = [
    'Tornado Express', 'Silver Arrow', 'Black Thunder', 'Golden Dawn',
    'Storm Rider', 'Night Wind', 'Fire Flash', 'Desert Storm',
    'Royal Flush', 'Iron Force', 'Blue Lightning', 'Red Comet'
  ];

  const DOG_NAMES = [
    'Fulmine Nero', 'Vento d\'Est', 'Saetta Rossa', 'Ombra Veloce',
    'Turbo Rex', 'Flash Gordon', 'Stelle Cadenti', 'Corsa Libera'
  ];

  const TENNIS_SURFACES = ['Cemento', 'Erba', 'Terra Rossa', 'Indoor'];
  const LEAGUES = ['Serie A Virtuale', 'Champions V', 'Premier V', 'La Liga V'];
  const JOCKEY_NAMES = ['M. Rossi', 'L. Ferrari', 'G. Bianchi', 'A. Ricci', 'F. Costa'];

  // Color assignment
  const RUNNER_COLORS = ['#e74c3c','#3498db','#2ecc71','#f39c12','#9b59b6','#1abc9c','#e67e22','#95a5a6'];

  function generateOdd(base, spread = 0.5) {
    return Math.max(1.1, parseFloat((base + (Math.random() - 0.5) * spread).toFixed(2)));
  }

  // ============ EVENT GENERATORS ============

  function generateFootballEvents(count = 6) {
    const events = [];
    const used = new Set();
    while (events.length < count) {
      let home = pick(FOOTBALL_TEAMS), away;
      do { away = pick(FOOTBALL_TEAMS); } while (away === home);
      const key = `${home}-${away}`;
      if (used.has(key)) continue;
      used.add(key);

      const homeOdd = generateOdd(2.1, 1.5);
      const drawOdd = generateOdd(3.2, 0.8);
      const awayOdd = generateOdd(2.8, 1.5);

      events.push({
        id: Date.now() + Math.random(),
        league: pick(LEAGUES),
        home, away, homeOdd, drawOdd, awayOdd,
        time: `${Math.floor(Math.random() * 85) + 1}'`,
        sport: 'calcio'
      });
    }
    return events;
  }

  function generateTennisEvents(count = 4) {
    const events = [];
    const used = new Set();
    while (events.length < count) {
      let p1 = pick(TENNIS_PLAYERS), p2;
      do { p2 = pick(TENNIS_PLAYERS); } while (p2 === p1);
      const key = `${p1}-${p2}`;
      if (used.has(key)) continue;
      used.add(key);
      const p1Odd = generateOdd(1.8, 0.8);
      const p2Odd = generateOdd(2.0, 0.8);
      events.push({
        id: Date.now() + Math.random(),
        surface: pick(TENNIS_SURFACES),
        p1, p2, p1Odd, p2Odd,
        score: `${Math.floor(Math.random()*3)}-${Math.floor(Math.random()*3)}`,
        sport: 'tennis'
      });
    }
    return events;
  }

  function generateBasketEvents(count = 4) {
    const events = [];
    const used = new Set();
    while (events.length < count) {
      let home = pick(BASKET_TEAMS), away;
      do { away = pick(BASKET_TEAMS); } while (away === home);
      const key = `${home}-${away}`;
      if (used.has(key)) continue;
      used.add(key);
      const spread = rnd(-8, 8);
      events.push({
        id: Date.now() + Math.random(),
        home, away,
        homeOdd: generateOdd(1.85, 0.4),
        awayOdd: generateOdd(1.95, 0.4),
        spread: spread.toFixed(1),
        total: rnd(205, 225),
        sport: 'basket'
      });
    }
    return events;
  }

  function generateRace(sport, count = 8) {
    const names = sport === 'cavalli' ? HORSE_NAMES.slice(0, count) : DOG_NAMES.slice(0, count);
    return names.map((name, i) => ({
      id: i + 1,
      name,
      odds: generateOdd(sport === 'cavalli' ? 4.5 : 5.0, 4),
      jockey: sport === 'cavalli' ? pick(JOCKEY_NAMES) : null,
      color: RUNNER_COLORS[i % RUNNER_COLORS.length]
    }));
  }

  // ============ STATE ============

  let betSlip = [];
  let raceBets = {}; // runnerIdx → amount

  function clearSlip() { betSlip = []; renderSlip(); }

  function addToBetSlip(event, betLabel, odds) {
    // One bet per event
    betSlip = betSlip.filter(b => b.eventId !== event.id);
    betSlip.push({
      eventId: event.id,
      label: `${event.home || event.p1} vs ${event.away || event.p2}`,
      betLabel, odds,
      amount: 0
    });
    renderSlip();
    showToast(`✅ ${betLabel} @${odds} aggiunto al carnet`, 'info');
  }

  function renderSlip() {
    const el = document.getElementById('bet-slip-body');
    const countEl = document.getElementById('slip-count');
    if (!el) return;
    if (countEl) countEl.textContent = betSlip.length;

    if (betSlip.length === 0) {
      el.innerHTML = '<div class="slip-empty">Seleziona le quote per scommettere</div>';
      return;
    }

    el.innerHTML = betSlip.map((b, i) => `
      <div class="slip-item">
        <button class="slip-remove" onclick="VirtualSports.removeBet(${i})">✕</button>
        <div class="slip-event">${b.label}</div>
        <div class="slip-bet">${b.betLabel}</div>
        <div class="slip-odds">${b.odds}x</div>
      </div>
    `).join('');

    const totalOdds = betSlip.reduce((p, b) => p * b.odds, 1).toFixed(2);
    document.getElementById('slip-total-odds').textContent = totalOdds;
  }

  function removeBet(idx) {
    betSlip.splice(idx, 1);
    renderSlip();
  }

  async function placeBet() {
    if (betSlip.length === 0) { showToast('Aggiungi scommesse al carnet', 'info'); return; }
    const amount = parseFloat(document.getElementById('slip-amount')?.value || 0);
    if (!amount || amount < 1) { showToast('Inserisci importo valido', 'info'); return; }
    if (!State.deductBalance(amount)) { showToast('Saldo insufficiente!', 'lose'); return; }

    const totalOdds = betSlip.reduce((p, b) => p * b.odds, 1);

    // Virtual simulation — random outcome weighted by odds
    const winChance = 1 / totalOdds;
    const won = Math.random() < (winChance * 0.92); // house edge

    await delay(1000);

    if (won) {
      const win = parseFloat((amount * totalOdds).toFixed(2));
      State.addBalance(win);
      showToast(`🏆 Scommessa vinta! +${formatCurrency(win)}`, 'win', 4000);
      State.recordHistory({ game: 'Virtual Sports', bet: amount, result: 'win', gain: win - amount });
    } else {
      showToast(`❌ Scommessa persa — ${formatCurrency(amount)}`, 'lose');
      State.recordHistory({ game: 'Virtual Sports', bet: amount, result: 'lose', gain: -amount });
    }

    betSlip = [];
    renderSlip();
  }

  // ============ RACE SIMULATION ============

  let raceRunning = false;
  let raceInterval = null;
  let raceProgress = [];
  let raceFinished = false;

  async function startRace(sport) {
    if (raceRunning) return;

    const betData = Object.entries(raceBets);
    if (betData.length === 0) { showToast('Scegli un partecipante da scommettere!', 'info'); return; }

    const totalBet = betData.reduce((s, [,a]) => s + a, 0);
    if (!State.deductBalance(totalBet)) { showToast('Saldo insufficiente!', 'lose'); return; }

    raceRunning = true;
    raceFinished = false;
    const btn = document.getElementById(`race-btn-${sport}`);
    if (btn) btn.disabled = true;

    const runners = parseInt(document.getElementById(`race-count-${sport}`)?.value || 8);
    raceProgress = Array(runners).fill(0);

    let ticker = setInterval(() => {
      let done = false;
      for (let i = 0; i < raceProgress.length; i++) {
        raceProgress[i] += Math.random() * 3.5 + 0.5;
        if (raceProgress[i] >= 100) { raceProgress[i] = 100; done = true; }
        const bar = document.getElementById(`runner-bar-${sport}-${i}`);
        if (bar) bar.style.width = raceProgress[i] + '%';
      }

      if (done && !raceFinished) {
        raceFinished = true;
        clearInterval(ticker);
        finishRace(sport);
      }
    }, 80);
  }

  function finishRace(sport) {
    const finishOrder = raceProgress
      .map((p, i) => ({ idx: i, progress: p }))
      .sort((a, b) => b.progress - a.progress);

    const winner = finishOrder[0];
    const winnerName = document.getElementById(`runner-name-${sport}-${winner.idx}`)?.textContent || `#${winner.idx+1}`;
    const winnerOdds = parseFloat(document.getElementById(`runner-odds-${sport}-${winner.idx}`)?.textContent || 2);

    // Mark winner
    const nameEl = document.getElementById(`runner-name-${sport}-${winner.idx}`);
    if (nameEl) nameEl.innerHTML += ' 🏆';

    let totalWin = 0;
    for (const [idxStr, amount] of Object.entries(raceBets)) {
      const idx = parseInt(idxStr);
      if (idx === winner.idx) {
        totalWin += amount * winnerOdds;
      }
    }
    totalWin = parseFloat(totalWin.toFixed(2));

    const totalBet = Object.values(raceBets).reduce((s, a) => s + a, 0);

    if (totalWin > 0) {
      State.addBalance(totalWin);
      showToast(`🏆 ${winnerName} vince! +${formatCurrency(totalWin)}`, 'win', 4000);
      State.recordHistory({ game: sport === 'cavalli' ? 'Corse Cavalli' : 'Corse Cani', bet: totalBet, result: 'win', gain: totalWin - totalBet });
    } else {
      showToast(`❌ Vince ${winnerName} — Scommessa persa`, 'lose');
      State.recordHistory({ game: sport === 'cavalli' ? 'Corse Cavalli' : 'Corse Cani', bet: totalBet, result: 'lose', gain: -totalBet });
    }

    raceBets = {};
    raceRunning = false;
    const btn = document.getElementById(`race-btn-${sport}`);
    if (btn) btn.disabled = false;
  }

  function setRaceBet(sport, idx, odds) {
    const bet = getBet(`race-${sport}`);
    if (!bet || bet < 1) { showToast('Imposta la puntata', 'info'); return; }
    raceBets[idx] = bet;
    showToast(`🎯 Scommessa su #${idx+1} @${odds}x`, 'info');
    document.querySelectorAll(`.runner-bet-btn[data-sport="${sport}"]`).forEach(b => b.classList.remove('selected'));
    const btn = document.querySelector(`.runner-bet-btn[data-sport="${sport}"][data-idx="${idx}"]`);
    if (btn) btn.classList.add('selected');
  }

  // ============ HTML RENDERERS ============

  function renderFootball() {
    const events = generateFootballEvents(6);
    return `
      <div class="game-section" style="max-width:100%">
        <div class="page-header">
          <h2 class="page-title">⚽ CALCIO VIRTUALE</h2>
          <p class="page-subtitle">Partite virtuali aggiornate ogni ciclo</p>
        </div>
        <div class="sports-grid">
          <div class="event-list">
            ${events.map(e => `
              <div class="sport-event">
                <div class="event-header">
                  <span class="event-league">${e.league}</span>
                  <span class="event-time">${e.time}</span>
                </div>
                <div class="event-teams">
                  <span class="team-name">${e.home}</span>
                  <span class="event-vs">VS</span>
                  <span class="team-name away">${e.away}</span>
                </div>
                <div class="event-odds">
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:''},'1 (${e.home})',${e.homeOdd})">
                    <span class="odd-label">1</span>
                    <span class="odd-value">${e.homeOdd}</span>
                  </button>
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:''},'X (Pareggio)',${e.drawOdd})">
                    <span class="odd-label">X</span>
                    <span class="odd-value">${e.drawOdd}</span>
                  </button>
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:''},'2 (${e.away})',${e.awayOdd})">
                    <span class="odd-label">2</span>
                    <span class="odd-value">${e.awayOdd}</span>
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
          ${renderBetSlip()}
        </div>
      </div>`;
  }

  function renderTennis() {
    const events = generateTennisEvents(4);
    return `
      <div class="game-section" style="max-width:100%">
        <div class="page-header">
          <h2 class="page-title">🎾 TENNIS VIRTUALE</h2>
          <p class="page-subtitle">Tornei virtuali in diretta</p>
        </div>
        <div class="sports-grid">
          <div class="event-list">
            ${events.map(e => `
              <div class="sport-event">
                <div class="event-header">
                  <span class="event-league">⚡ Virtuale — ${e.surface}</span>
                  <span class="event-time">${e.score}</span>
                </div>
                <div class="event-teams">
                  <span class="team-name">${e.p1}</span>
                  <span class="event-vs">VS</span>
                  <span class="team-name away">${e.p2}</span>
                </div>
                <div class="event-odds two-way">
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.p1}',away:'${e.p2}',p1:'${e.p1}',p2:'${e.p2}'},'${e.p1}',${e.p1Odd})">
                    <span class="odd-label">${e.p1.split(' ')[0]}</span>
                    <span class="odd-value">${e.p1Odd}</span>
                  </button>
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.p1}',away:'${e.p2}',p1:'${e.p1}',p2:'${e.p2}'},'${e.p2}',${e.p2Odd})">
                    <span class="odd-label">${e.p2.split(' ')[0]}</span>
                    <span class="odd-value">${e.p2Odd}</span>
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
          ${renderBetSlip()}
        </div>
      </div>`;
  }

  function renderBasket() {
    const events = generateBasketEvents(4);
    return `
      <div class="game-section" style="max-width:100%">
        <div class="page-header">
          <h2 class="page-title">🏀 BASKET VIRTUALE</h2>
          <p class="page-subtitle">NBA Virtual League</p>
        </div>
        <div class="sports-grid">
          <div class="event-list">
            ${events.map(e => `
              <div class="sport-event">
                <div class="event-header">
                  <span class="event-league">🏀 NBA Virtuale</span>
                  <span class="event-time">Over ${e.total}</span>
                </div>
                <div class="event-teams">
                  <span class="team-name">${e.home}</span>
                  <span class="event-vs">${e.spread > 0 ? '+' : ''}${e.spread}</span>
                  <span class="team-name away">${e.away}</span>
                </div>
                <div class="event-odds two-way">
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:''},'${e.home} Vince',${e.homeOdd})">
                    <span class="odd-label">Home</span>
                    <span class="odd-value">${e.homeOdd}</span>
                  </button>
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:''},'${e.away} Vince',${e.awayOdd})">
                    <span class="odd-label">Away</span>
                    <span class="odd-value">${e.awayOdd}</span>
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
          ${renderBetSlip()}
        </div>
      </div>`;
  }

  function renderRace(sport) {
    const count = 8;
    const runners = generateRace(sport, count);
    const icon = sport === 'cavalli' ? '🐎' : '🐕';
    const title = sport === 'cavalli' ? 'CORSE DI CAVALLI' : 'CORSE DI CANI';

    return `
      <div class="game-section" style="max-width:100%">
        <div class="page-header">
          <h2 class="page-title">${icon} ${title} VIRTUALI</h2>
          <p class="page-subtitle">Scommetti sul vincitore — Gara virtuale simulata in tempo reale</p>
        </div>
        <div class="race-track">
          ${runners.map((r, i) => `
            <div class="race-runner">
              <div class="runner-num" style="background:${r.color}20;color:${r.color};border:1px solid ${r.color}40">${r.id}</div>
              <div class="runner-name" id="runner-name-${sport}-${i}">${r.name}${r.jockey ? ` <small style="color:var(--text-3)">(${r.jockey})</small>` : ''}</div>
              <div class="runner-track">
                <div class="runner-progress" id="runner-bar-${sport}-${i}" style="background:${r.color}"></div>
              </div>
              <div class="runner-odds" id="runner-odds-${sport}-${i}">${r.odds}</div>
              <button class="runner-bet-btn" data-sport="${sport}" data-idx="${i}" onclick="VirtualSports.setRaceBet('${sport}',${i},${r.odds})">Scommetti</button>
            </div>
          `).join('')}
        </div>
        <input type="hidden" id="race-count-${sport}" value="${count}">
        ${createBetControls(`race-${sport}`, 10)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal" id="race-btn-${sport}" onclick="VirtualSports.startRace('${sport}')">🏁 LANCIA LA GARA!</button>
        </div>
      </div>`;
  }

  function renderBetSlip() {
    return `
      <div class="bet-slip">
        <div class="bet-slip-header">
          <span class="bet-slip-title">CARNET</span>
          <span class="bet-slip-count" id="slip-count">0</span>
        </div>
        <div class="bet-slip-body" id="bet-slip-body">
          <div class="slip-empty">Seleziona le quote per scommettere</div>
        </div>
        <div class="bet-slip-footer">
          <div class="slip-summary">
            <span>Quota totale:</span>
            <strong id="slip-total-odds">—</strong>
          </div>
          <div class="form-group">
            <label>Importo (€)</label>
            <input type="number" id="slip-amount" placeholder="0.00" min="1" />
          </div>
          <button class="btn-primary" onclick="VirtualSports.placeBet()">Conferma Scommessa</button>
          <button class="btn-ghost btn-full" onclick="VirtualSports.clearSlip()" style="margin-top:0.35rem">Cancella</button>
        </div>
      </div>`;
  }

  return {
    renderFootball, renderTennis, renderBasket, renderRace,
    addToBetSlip, removeBet, clearSlip, placeBet,
    setRaceBet, startRace
  };
})();
