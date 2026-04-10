// =============================================
// BetCenterNL — VIRTUAL SPORTS ENGINE
// =============================================

const VirtualSports = (() => {

  // ============ DATA GENERATORS ============

  function rnd(min, max) { return parseFloat((Math.random() * (max - min) + min).toFixed(2)); }
  function pick(arr) { return arr[Math.floor(Math.random() * arr.length)]; }
  function fmt2(n) { return n.toFixed(2); }

  const FOOTBALL_TEAMS = [
    'FC Roma', 'Milan United', 'Juventus City', 'Inter Blue',
    'Napoli Stars', 'Lazio FC', 'Fiorentina', 'Torino FC',
    'Atalanta', 'Sampdoria', 'Genoa', 'Bologna FC',
    'Verona FC', 'Spezia', 'Venezia', 'Empoli'
  ];

  const TENNIS_PLAYERS = [
    'Djokovic', 'Federer', 'Nadal', 'Murray',
    'Alcaraz', 'Sinner', 'Medvedev', 'Zverev',
    'Tsitsipas', 'Rublev', 'Berrettini', 'Shapovalov'
  ];

  const BASKET_TEAMS = [
    'Lakers', 'Celtics', 'Bulls', 'Warriors',
    'Heat', 'Nets', 'Knicks', 'Bucks'
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
    
    // Determine sport from event.sport if available, otherwise detect
    let sport = event.sport || 'calcio';
    if (!event.sport) {
      if (event.p1 && event.p2) sport = 'tennis';
      if (event.total !== undefined) sport = 'basket';
    }
    
    betSlip.push({
      eventId: event.id,
      label: `${event.home || event.p1} vs ${event.away || event.p2}`,
      betLabel, odds, sport,
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

    // Show simulation for each bet
    await simulateBets(betSlip, won);

    await delay(1000);

    if (won) {
      const win = parseFloat((amount * totalOdds).toFixed(2));
      State.addBalance(win);
      showToast(`🏆 Scommessa vinta! +${formatCurrency(win - amount)}`, 'win', 4000);
      State.recordHistory({ game: 'Virtual Sports', bet: amount, result: 'win', gain: win - amount });
    } else {
      showToast(`❌ Scommessa persa — ${formatCurrency(amount)}`, 'lose');
      State.recordHistory({ game: 'Virtual Sports', bet: amount, result: 'lose', gain: -amount });
    }

    betSlip = [];
    renderSlip();
  }

  async function simulateBets(bets, won) {
    const simulationHTML = `
      <div class="simulation-overlay">
        <div class="simulation-container">
          <div class="simulation-header">📊 SIMULAZIONE PARTITE</div>
          <div class="simulation-list" id="simulation-list"></div>
        </div>
      </div>
    `;

    document.body.insertAdjacentHTML('beforeend', simulationHTML);
    const overlay = document.querySelector('.simulation-overlay');
    overlay.style.opacity = '0';
    await delay(100);
    overlay.style.transition = 'opacity 0.5s ease-in-out';
    overlay.style.opacity = '1';

    const listEl = document.getElementById('simulation-list');

    for (let i = 0; i < bets.length; i++) {
      const bet = bets[i];
      const sport = bet.sport || 'calcio';
      
      const simHTML = `
        <div class="simulation-item" id="sim-${i}">
          <div class="sim-match">${bet.label}</div>
          <div class="sim-bet">${bet.betLabel}</div>
          <div class="sim-progress" id="sim-progress-${i}"></div>
          <div class="sim-result" id="sim-result-${i}"></div>
        </div>
      `;
      
      listEl.insertAdjacentHTML('beforeend', simHTML);
      const isLastBet = i === bets.length - 1;
      await simulateMatch(sport, i, won && isLastBet);
      await delay(800);
    }

    await delay(2000);
    overlay.remove();
  }

  async function simulateMatch(sport, index, shouldWin) {
    const progressEl = document.getElementById(`sim-progress-${index}`);
    const resultEl = document.getElementById(`sim-result-${index}`);

    if (sport === 'calcio') {
      // Football simulation - goals
      let homeGoals = 0, awayGoals = 0;
      
      for (let i = 0; i < 5; i++) {
        if (shouldWin) {
          homeGoals += Math.random() > 0.5 ? 1 : 0;
        } else {
          awayGoals += Math.random() > 0.5 ? 1 : 0;
        }
        
        progressEl.innerHTML = `⚽ ${homeGoals} - ${awayGoals}`;
        await delay(300);
      }
      
      resultEl.innerHTML = `<span class="${shouldWin ? 'sim-win' : 'sim-lose'}">Risultato: ${homeGoals} - ${awayGoals}</span>`;
    } 
    else if (sport === 'tennis') {
      // Tennis simulation - sets (best of 5, winner gets 3 sets first)
      let p1Sets = 0, p2Sets = 0;
      
      while (p1Sets < 3 && p2Sets < 3) {
        if (shouldWin) {
          p1Sets++;
        } else {
          p2Sets++;
        }
        progressEl.innerHTML = `🏆 Set: ${p1Sets}-${p2Sets}`;
        await delay(500);
      }
      
      const winner = shouldWin ? 'Giocatore 1' : 'Giocatore 2';
      resultEl.innerHTML = `<span class="${shouldWin ? 'sim-win' : 'sim-lose'}">Vincitore: ${winner} (${p1Sets}-${p2Sets})</span>`;
    }
    else if (sport === 'basket') {
      // Basketball simulation - 4 quarters with realistic scores
      let homeScore = 0, awayScore = 0;
      const quarters = shouldWin ? 
        [
          { home: 28, away: 22 },
          { home: 55, away: 48 },
          { home: 82, away: 75 },
          { home: 110, away: 98 }
        ] :
        [
          { home: 22, away: 28 },
          { home: 48, away: 55 },
          { home: 75, away: 82 },
          { home: 98, away: 110 }
        ];
      
      for (let q of quarters) {
        homeScore = q.home;
        awayScore = q.away;
        progressEl.innerHTML = `🏀 ${homeScore} - ${awayScore}`;
        await delay(350);
      }
      
      resultEl.innerHTML = `<span class="${shouldWin ? 'sim-win' : 'sim-lose'}">Finale: ${homeScore} - ${awayScore}</span>`;
    }
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

    let ticker = setInterval(async () => {
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
        await finishRace(sport);
      }
    }, 80);
  }

  async function finishRace(sport) {
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
    let hasWinningBet = false;
    
    for (const [idxStr, amount] of Object.entries(raceBets)) {
      const idx = parseInt(idxStr);
      if (idx === winner.idx) {
        totalWin += amount * winnerOdds;
        hasWinningBet = true;
      }
    }
    totalWin = parseFloat(totalWin.toFixed(2));

    const totalBet = Object.values(raceBets).reduce((s, a) => s + a, 0);
    const won = hasWinningBet && totalWin > totalBet;

    // Show podium for dog races
    if (sport === 'cani') {
      await showPodium(finishOrder, sport);
    }

    // Simulate result reveal with animation
    await simulateResultReveal(won, winnerName, totalWin, totalBet, sport);

    if (won) {
      State.addBalance(totalWin);
      showToast(`🏆 ${winnerName} vince! +${formatCurrency(totalWin - totalBet)}`, 'win', 4000);
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

  async function showPodium(finishOrder, sport) {
    const top3 = finishOrder.slice(0, 3);
    const first = top3[0] ? document.getElementById(`runner-name-${sport}-${top3[0].idx}`)?.textContent || 'N/A' : 'N/A';
    const second = top3[1] ? document.getElementById(`runner-name-${sport}-${top3[1].idx}`)?.textContent || 'N/A' : 'N/A';
    const third = top3[2] ? document.getElementById(`runner-name-${sport}-${top3[2].idx}`)?.textContent || 'N/A' : 'N/A';
    
    const podiumHTML = `
      <div class="podium-overlay">
        <div class="podium-container">
          <div class="podium-title">🏆 PODIO 🏆</div>
          <div class="podium-stand">
            <div class="podium-position second">
              <div class="podium-medal">🥈</div>
              <div class="podium-name">${second}</div>
              <div class="podium-rank">2°</div>
            </div>
            <div class="podium-position first">
              <div class="podium-medal">🥇</div>
              <div class="podium-name">${first}</div>
              <div class="podium-rank">1°</div>
            </div>
            <div class="podium-position third">
              <div class="podium-medal">🥉</div>
              <div class="podium-name">${third}</div>
              <div class="podium-rank">3°</div>
            </div>
          </div>
          <button class="btn-primary" onclick="document.querySelector('.podium-overlay').remove()">Continua</button>
        </div>
      </div>
    `;

    document.body.insertAdjacentHTML('beforeend', podiumHTML);
    
    // Animate podium entrance
    const overlay = document.querySelector('.podium-overlay');
    overlay.style.opacity = '0';
    await delay(100);
    overlay.style.transition = 'opacity 0.5s ease-in-out';
    overlay.style.opacity = '1';

    // Wait for user to close
    await new Promise(resolve => {
      const btn = overlay.querySelector('.btn-primary');
      btn.addEventListener('click', resolve, { once: true });
    });

    // Show full standings
    await showFullStandings(finishOrder, sport);
  }

  async function showFullStandings(finishOrder, sport) {
    const standingsHTML = `
      <div class="standings-overlay">
        <div class="standings-container">
          <div class="standings-title">📊 CLASSIFICA COMPLETA</div>
          <div class="standings-list">
            ${finishOrder.map((item, idx) => {
              const name = document.getElementById(`runner-name-${sport}-${item.idx}`)?.textContent || 'N/A';
              return `
              <div class="standings-row" style="animation-delay: ${idx * 0.1}s">
                <span class="standings-pos">${idx + 1}°</span>
                <span class="standings-name">${name}</span>
                <span class="standings-progress">${Math.round(item.progress)}%</span>
              </div>
            `;
            }).join('')}
          </div>
          <button class="btn-primary" onclick="document.querySelector('.standings-overlay').remove()">Chiudi</button>
        </div>
      </div>
    `;

    document.body.insertAdjacentHTML('beforeend', standingsHTML);
    
    const overlay = document.querySelector('.standings-overlay');
    overlay.style.opacity = '0';
    await delay(100);
    overlay.style.transition = 'opacity 0.5s ease-in-out';
    overlay.style.opacity = '1';

    await new Promise(resolve => {
      const btn = overlay.querySelector('.btn-primary');
      btn.addEventListener('click', resolve, { once: true });
    });
  }

  async function simulateResultReveal(won, winnerName, totalWin, totalBet, sport) {
    // Create result simulation overlay
    const resultHTML = `
      <div class="result-simulation">
        <div class="result-container">
          <div class="result-spinner">⏳</div>
          <div class="result-text">Elaborazione risultati...</div>
        </div>
      </div>
    `;

    document.body.insertAdjacentHTML('beforeend', resultHTML);
    const overlay = document.querySelector('.result-simulation');
    
    await delay(1500);

    // Reveal result
    overlay.innerHTML = `
      <div class="result-container">
        <div class="result-reveal ${won ? 'win' : 'lose'}">
          ${won ? '🎉' : '😢'}
        </div>
        <div class="result-text">${won ? 'VINTO!' : 'PERSO!'}</div>
        ${won ? `<div class="result-amount">+${formatCurrency(totalWin)}</div>` : ''}
      </div>
    `;

    overlay.style.opacity = '1';
    await delay(2000);
    overlay.remove();
  }

  function delay(ms) {
    return new Promise(resolve => setTimeout(resolve, ms));
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
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:'',sport:'basket'},'${e.home} Vince',${e.homeOdd})">
                    <span class="odd-label">Home</span>
                    <span class="odd-value">${e.homeOdd}</span>
                  </button>
                  <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${e.id}',home:'${e.home}',away:'${e.away}',p1:'',p2:'',sport:'basket'},'${e.away} Vince',${e.awayOdd})">
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
