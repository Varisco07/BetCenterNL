// =============================================
// BetCenterNL — VIRTUAL SPORTS ENGINE
// Tutti gli eventi e risultati vengono dal backend Node.js
// =============================================

const VirtualSports = (() => {

  // ── Colori per le corse ───────────────────────────────────────────────────
  const RUNNER_COLORS = ['#e74c3c','#3498db','#2ecc71','#f39c12','#9b59b6','#1abc9c','#e67e22','#95a5a6'];

  // ── Stato ─────────────────────────────────────────────────────────────────
  let betSlip  = [];
  let raceBets = {};

  // ── Carnet ────────────────────────────────────────────────────────────────
  function clearSlip() { betSlip = []; renderSlip(); }

  function addToBetSlip(event, betLabel, odds, prediction) {
    betSlip = betSlip.filter(b => b.eventId !== event.id);

    const sport = event.sport || 'calcio';

    if (!prediction) {
      if (betLabel.startsWith('X') || betLabel.includes('Pareggio')) prediction = 'draw';
      else if (betLabel.startsWith('2 ')) prediction = 'away';
      else prediction = 'home';
    }

    betSlip.push({
      eventId: event.id,
      label:   `${event.home} vs ${event.away}`,
      betLabel, odds, sport, prediction,
      matchId: event.matchId || null
    });
    renderSlip();
    showToast(`✅ ${betLabel} @${odds} aggiunto al carnet`, 'info');
  }

  function renderSlip() {
    const el      = document.getElementById('bet-slip-body');
    const countEl = document.getElementById('slip-count');
    if (!el) return;
    if (countEl) countEl.textContent = betSlip.length;

    if (!betSlip.length) {
      el.innerHTML = '<div class="slip-empty">Seleziona le quote per scommettere</div>';
      return;
    }

    el.innerHTML = betSlip.map((b, i) => `
      <div class="slip-item">
        <button class="slip-remove" onclick="VirtualSports.removeBet(${i})">✕</button>
        <div class="slip-event">${b.label}</div>
        <div class="slip-bet">${b.betLabel}</div>
        <div class="slip-odds">${b.odds}x</div>
      </div>`).join('');

    document.getElementById('slip-total-odds').textContent =
      betSlip.reduce((p, b) => p * b.odds, 1).toFixed(2);
  }

  function removeBet(idx) { betSlip.splice(idx, 1); renderSlip(); }

  // ── Piazza scommessa ──────────────────────────────────────────────────────
  async function placeBet() {
    if (!betSlip.length) { showToast('Aggiungi scommesse al carnet', 'info'); return; }
    const amount = parseFloat(document.getElementById('slip-amount')?.value || 0);
    if (!amount || amount < 1) { showToast('Inserisci importo valido', 'info'); return; }
    if (State.balance < amount) { showToast('Saldo insufficiente!', 'lose'); return; }

    const { sport, prediction, matchId } = betSlip[0];
    const totalOdds = betSlip.reduce((p, b) => p * b.odds, 1);

    let serverResult;
    try {
      serverResult = matchId
        ? await API.betVirtualMatch(sport, amount, prediction, matchId)
        : await API.playVirtualSport(sport, amount, prediction);
    } catch (_) {
      showToast('Errore di connessione al server', 'lose');
      return;
    }

    const won = serverResult.win;
    await simulateBets(betSlip, won);
    await delay(1000);

    State.syncFromServer(serverResult.newBalance);

    if (won) {
      const win = parseFloat((amount * totalOdds).toFixed(2));
      showToast(`🏆 Scommessa vinta! +${formatCurrency(win - amount)}`, 'win', 4000);
      State.recordHistory({ game: `Virtual ${sport}`, bet: amount, result: 'win', gain: serverResult.gain });
    } else {
      showToast(`❌ Scommessa persa — ${formatCurrency(amount)}`, 'lose');
      State.recordHistory({ game: `Virtual ${sport}`, bet: amount, result: 'lose', gain: serverResult.gain });
    }

    betSlip = [];
    renderSlip();

    // Ricarica nuovi eventi dal server dopo la scommessa
    await loadServerEvents(sport);
  }

  // ── Animazione partita ────────────────────────────────────────────────────
  async function simulateBets(bets, won) {
    document.body.insertAdjacentHTML('beforeend', `
      <div class="simulation-overlay">
        <div class="simulation-container">
          <div class="simulation-header">📊 SIMULAZIONE PARTITE</div>
          <div class="simulation-list" id="simulation-list"></div>
        </div>
      </div>`);
    const overlay = document.querySelector('.simulation-overlay');
    overlay.style.opacity = '0';
    await delay(100);
    overlay.style.transition = 'opacity 0.5s ease-in-out';
    overlay.style.opacity = '1';

    const listEl = document.getElementById('simulation-list');
    for (let i = 0; i < bets.length; i++) {
      listEl.insertAdjacentHTML('beforeend', `
        <div class="simulation-item" id="sim-${i}">
          <div class="sim-match">${bets[i].label}</div>
          <div class="sim-bet">${bets[i].betLabel}</div>
          <div class="sim-progress" id="sim-progress-${i}"></div>
          <div class="sim-result"  id="sim-result-${i}"></div>
        </div>`);
      await simulateMatch(bets[i].sport, i, won && i === bets.length - 1);
      await delay(800);
    }
    await delay(2000);
    overlay.remove();
  }

  async function simulateMatch(sport, index, shouldWin) {
    const prog = document.getElementById(`sim-progress-${index}`);
    const res  = document.getElementById(`sim-result-${index}`);
    if (!prog || !res) return; // overlay già rimosso

    if (sport === 'calcio') {
      let h = 0, a = 0;
      for (let i = 0; i < 5; i++) {
        if (shouldWin) h += Math.random() > 0.5 ? 1 : 0;
        else           a += Math.random() > 0.5 ? 1 : 0;
        if (prog) prog.innerHTML = `⚽ ${h} - ${a}`;
        await delay(300);
      }
      if (res) res.innerHTML = `<span class="${shouldWin?'sim-win':'sim-lose'}">Risultato: ${h} - ${a}</span>`;
    } else if (sport === 'tennis') {
      let p1 = 0, p2 = 0;
      while (p1 < 3 && p2 < 3) {
        if (shouldWin) p1++; else p2++;
        if (prog) prog.innerHTML = `🎾 Set: ${p1}-${p2}`;
        await delay(500);
      }
      if (res) res.innerHTML = `<span class="${shouldWin?'sim-win':'sim-lose'}">Vincitore: ${shouldWin?'Giocatore 1':'Giocatore 2'} (${p1}-${p2})</span>`;
    } else {
      const quarters = shouldWin
        ? [{h:28,a:22},{h:55,a:48},{h:82,a:75},{h:110,a:98}]
        : [{h:22,a:28},{h:48,a:55},{h:75,a:82},{h:98,a:110}];
      for (const q of quarters) {
        if (prog) prog.innerHTML = `🏀 ${q.h} - ${q.a}`;
        await delay(350);
      }
      const last = quarters[3];
      if (res) res.innerHTML = `<span class="${shouldWin?'sim-win':'sim-lose'}">Finale: ${last.h} - ${last.a}</span>`;
    }
  }

  // ── Corse ─────────────────────────────────────────────────────────────────
  let raceRunning = false;

  function setRaceBet(sport, idx, odds) {
    const bet = getBet(`race-${sport}`);
    if (!bet || bet < 1) { showToast('Imposta la puntata', 'info'); return; }
    raceBets[idx] = { amount: bet, odds };
    showToast(`🎯 Scommessa su #${idx+1} @${odds}x`, 'info');
    document.querySelectorAll(`.runner-bet-btn[data-sport="${sport}"]`).forEach(b => b.classList.remove('selected'));
    const btn = document.querySelector(`.runner-bet-btn[data-sport="${sport}"][data-idx="${idx}"]`);
    if (btn) btn.classList.add('selected');
  }

  async function startRace(sport) {
    if (raceRunning) return;
    const betEntries = Object.entries(raceBets);
    if (!betEntries.length) { showToast('Scegli un partecipante da scommettere!', 'info'); return; }

    const [pickedIdxStr, betData] = betEntries[0];
    const pickedIdx = parseInt(pickedIdxStr);
    const betAmount = betData.amount;

    if (State.balance < betAmount) { showToast('Saldo insufficiente!', 'lose'); return; }

    raceRunning = true;
    const btn = document.getElementById(`race-btn-${sport}`);
    if (btn) btn.disabled = true;
    showToast('⏳ Simulazione gara in corso...', 'info');

    // Il backend simula la gara con le forze reali
    let sim;
    try {
      sim = await API.raceSimulate(sport, betAmount, pickedIdx, currentRaceId[sport] || null);
    } catch (_) {
      showToast('Errore di connessione al server', 'lose');
      raceRunning = false;
      if (btn) btn.disabled = false;
      return;
    }

    if (!sim.ok) {
      showToast('Errore nella simulazione', 'lose');
      raceRunning = false;
      if (btn) btn.disabled = false;
      return;
    }

    const { runners, frames, winnerIdx, won, gain, newBalance, winnerName } = sim;

    // Aggiorna nomi, quote e pulsanti nella UI con quelli reali del server
    runners.forEach((r, i) => {
      const nameEl = document.getElementById(`runner-name-${sport}-${i}`);
      const oddsEl = document.getElementById(`runner-odds-${sport}-${i}`);
      const betBtn = document.querySelector(`.runner-bet-btn[data-sport="${sport}"][data-idx="${i}"]`);
      if (nameEl) nameEl.textContent = r.name;
      if (oddsEl) oddsEl.textContent = r.odd;
      // Aggiorna la quota nel betBtn onclick
      if (betBtn) betBtn.setAttribute('onclick', `VirtualSports.setRaceBet('${sport}',${i},${r.odd})`);
    });

    // Aggiorna la quota salvata per il corridore scelto
    if (raceBets[pickedIdx]) {
      raceBets[pickedIdx].odds = runners[pickedIdx]?.odd || raceBets[pickedIdx].odds;
    }

    // Anima la gara seguendo i frame del backend
    await animateRace(sport, frames, runners.length);

    // Marca il vincitore
    const winnerEl = document.getElementById(`runner-name-${sport}-${winnerIdx}`);
    if (winnerEl) winnerEl.innerHTML += ' 🏆';

    // Costruisci finishOrder dal frame finale
    const lastFrame = frames[frames.length - 1];
    const finishOrder = lastFrame
      .map((pct, i) => ({ idx: i, progress: pct }))
      .sort((a, b) => b.progress - a.progress);

    await showPodium(finishOrder, sport, runners);
    await simulateResultReveal(won, winnerName, won ? betAmount * betData.odds : 0);

    State.syncFromServer(newBalance);

    if (won) {
      const winAmt = parseFloat((betAmount * betData.odds).toFixed(2));
      showToast(`🏆 ${winnerName} vince! +${formatCurrency(winAmt - betAmount)}`, 'win', 4000);
      State.recordHistory({ game: sport === 'cavalli' ? 'Corse Cavalli' : 'Corse Cani', bet: betAmount, result: 'win', gain });
    } else {
      showToast(`❌ Vince ${winnerName} — Scommessa persa`, 'lose');
      State.recordHistory({ game: sport === 'cavalli' ? 'Corse Cavalli' : 'Corse Cani', bet: betAmount, result: 'lose', gain });
    }

    raceBets = {};
    raceRunning = false;
    if (btn) btn.disabled = false;
    // Ricarica nuovi corridori per la prossima gara
    setTimeout(() => loadRaceFromServer(sport), 1000);
  }

  async function animateRace(sport, frames, numRunners) {
    // Distribuisce i frame in ~4 secondi
    const frameMs = Math.max(30, Math.floor(4000 / frames.length));
    for (const frame of frames) {
      for (let i = 0; i < numRunners; i++) {
        const bar = document.getElementById(`runner-bar-${sport}-${i}`);
        if (bar) bar.style.width = frame[i] + '%';
      }
      await delay(frameMs);
    }
    // Porta tutte le barre al 100%
    for (let i = 0; i < numRunners; i++) {
      const bar = document.getElementById(`runner-bar-${sport}-${i}`);
      if (bar) bar.style.width = '100%';
    }
  }

  async function showPodium(finishOrder, sport, runners) {
    const top3 = finishOrder.slice(0, 3);
    const getName = i => {
      if (runners && runners[i]) return runners[i].name;
      return (document.getElementById(`runner-name-${sport}-${i}`)?.textContent || 'N/A')
        .replace(/<[^>]*>/g,'').replace('🏆','').trim();
    };
    const icon  = sport === 'cavalli' ? '🐎' : '🐕';
    const title = sport === 'cavalli' ? 'CORSE DI CAVALLI' : 'CORSE DI CANI';

    document.body.insertAdjacentHTML('beforeend', `
      <div class="podium-overlay">
        <div class="podium-container">
          <div class="podium-title">${icon} PODIO ${title} ${icon}</div>
          <div class="podium-stand">
            <div class="podium-position second"><div class="podium-medal">🥈</div><div class="podium-name">${getName(top3[1]?.idx)}</div><div class="podium-rank">2°</div></div>
            <div class="podium-position first"> <div class="podium-medal">🥇</div><div class="podium-name">${getName(top3[0]?.idx)}</div><div class="podium-rank">1°</div></div>
            <div class="podium-position third"> <div class="podium-medal">🥉</div><div class="podium-name">${getName(top3[2]?.idx)}</div><div class="podium-rank">3°</div></div>
          </div>
          <button class="btn-primary" onclick="document.querySelector('.podium-overlay').remove()">Vedi Classifica</button>
        </div>
      </div>`);

    const overlay = document.querySelector('.podium-overlay');
    overlay.style.opacity = '0';
    await delay(100);
    overlay.style.transition = 'opacity 0.5s';
    overlay.style.opacity = '1';

    await new Promise(r => overlay.querySelector('.btn-primary').addEventListener('click', () => { overlay.remove(); r(); }, { once: true }));

    // Classifica completa
    document.body.insertAdjacentHTML('beforeend', `
      <div class="standings-overlay">
        <div class="standings-container">
          <div class="standings-title">${icon} CLASSIFICA FINALE ${title.split(' ').pop()}</div>
          <div class="standings-list">
            ${finishOrder.map((item, idx) => {
              const n = getName(item.idx);
              const medal = idx===0?'🥇':idx===1?'🥈':idx===2?'🥉':'';
              return `<div class="standings-row ${idx<3?'podium-position-row':''}">
                <span class="standings-pos">${medal||(idx+1)+'°'}</span>
                <span class="standings-name">${n}</span>
                <span class="standings-progress">${Math.round(item.progress)}%</span>
              </div>`;
            }).join('')}
          </div>
          <button class="btn-primary" onclick="document.querySelector('.standings-overlay').remove()">Chiudi</button>
        </div>
      </div>`);

    const so = document.querySelector('.standings-overlay');
    so.style.opacity = '0';
    await delay(100);
    so.style.transition = 'opacity 0.5s';
    so.style.opacity = '1';
    await new Promise(r => so.querySelector('.btn-primary').addEventListener('click', () => { so.remove(); r(); }, { once: true }));
  }

  async function simulateResultReveal(won, winnerName, totalWin) {
    document.body.insertAdjacentHTML('beforeend', `
      <div class="result-simulation">
        <div class="result-container">
          <div class="result-spinner">⏳</div>
          <div class="result-text">Elaborazione risultati...</div>
        </div>
      </div>`);
    const overlay = document.querySelector('.result-simulation');
    await delay(1500);
    overlay.innerHTML = `
      <div class="result-container">
        <div class="result-reveal ${won?'win':'lose'}">${won?'🎉':'😢'}</div>
        <div class="result-text">${won?'VINTO!':'PERSO!'}</div>
        ${won ? `<div class="result-amount">+${formatCurrency(totalWin)}</div>` : ''}
      </div>`;
    overlay.style.opacity = '1';
    await delay(2000);
    overlay.remove();
  }

  // ── Renderer sezioni ──────────────────────────────────────────────────────

  function renderFootball() {
    setTimeout(() => loadServerEvents('calcio'), 50);
    return buildShell('calcio', '⚽', 'CALCIO VIRTUALE', 'Partite virtuali — eventi generati dal server');
  }

  function renderTennis() {
    setTimeout(() => loadServerEvents('tennis'), 50);
    return buildShell('tennis', '🎾', 'TENNIS VIRTUALE', 'Tornei virtuali in diretta');
  }

  function renderBasket() {
    setTimeout(() => loadServerEvents('basket'), 50);
    return buildShell('basket', '🏀', 'BASKET VIRTUALE', 'NBA Virtual League');
  }

  function buildShell(sport, icon, title, subtitle) {
    return `
      <div class="game-section" style="max-width:100%">
        <div class="page-header">
          <h2 class="page-title">${icon} ${title}</h2>
          <p class="page-subtitle">${subtitle}</p>
        </div>
        <div class="sports-grid">
          <div class="event-list" id="virtual-event-list-${sport}">
            <div class="info-box text-center" style="margin:1rem">⏳ Caricamento eventi dal server...</div>
          </div>
          ${renderBetSlip()}
        </div>
      </div>`;
  }

  async function loadServerEvents(sport) {
    const count = sport === 'calcio' ? 6 : 4;
    const listEl = document.getElementById(`virtual-event-list-${sport}`);
    if (!listEl) return;

    listEl.innerHTML = '<div class="info-box text-center" style="margin:1rem">⏳ Caricamento eventi dal server...</div>';

    const events = [];
    try {
      for (let i = 0; i < count; i++) {
        const r = await API.generateVirtualMatch(sport);
        if (r.ok) events.push({ ...r.match, matchId: r.matchId, sport });
      }
    } catch (_) {
      listEl.innerHTML = '<div class="info-box text-center">⚠️ Impossibile caricare gli eventi. Riprova.</div>';
      return;
    }

    if (!events.length) return;
    listEl.innerHTML = events.map(e => buildEventHTML(e, sport)).join('');
  }

  function buildEventHTML(e, sport) {
    const mid = e.matchId || '';
    if (sport === 'calcio') {
      const ho = e.odds?.home || 2.0;
      const dr = e.odds?.draw || 3.0;
      const aw = e.odds?.away || 2.5;
      return `<div class="sport-event">
        <div class="event-header">
          <span class="event-league">Serie A Virtuale</span>
        </div>
        <div class="event-teams">
          <span class="team-name">${e.home}</span>
          <span class="event-vs">VS</span>
          <span class="team-name away">${e.away}</span>
        </div>
        <div class="event-odds">
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${e.home}',away:'${e.away}',sport:'calcio',matchId:'${mid}'},'1 (${e.home})',${ho},'home')">
            <span class="odd-label">1</span><span class="odd-value">${ho}</span>
          </button>
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${e.home}',away:'${e.away}',sport:'calcio',matchId:'${mid}'},'X (Pareggio)',${dr},'draw')">
            <span class="odd-label">X</span><span class="odd-value">${dr}</span>
          </button>
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${e.home}',away:'${e.away}',sport:'calcio',matchId:'${mid}'},'2 (${e.away})',${aw},'away')">
            <span class="odd-label">2</span><span class="odd-value">${aw}</span>
          </button>
        </div>
      </div>`;
    } else if (sport === 'tennis') {
      const p1 = e.home; const p2 = e.away;
      const o1 = e.odds?.home || 1.8;
      const o2 = e.odds?.away || 2.0;
      return `<div class="sport-event">
        <div class="event-header"><span class="event-league">⚡ ATP Virtuale</span></div>
        <div class="event-teams">
          <span class="team-name">${p1}</span>
          <span class="event-vs">VS</span>
          <span class="team-name away">${p2}</span>
        </div>
        <div class="event-odds two-way">
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${p1}',away:'${p2}',sport:'tennis',matchId:'${mid}'},'${p1}',${o1},'home')">
            <span class="odd-label">${p1.split(' ')[0]}</span><span class="odd-value">${o1}</span>
          </button>
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${p1}',away:'${p2}',sport:'tennis',matchId:'${mid}'},'${p2}',${o2},'away')">
            <span class="odd-label">${p2.split(' ')[0]}</span><span class="odd-value">${o2}</span>
          </button>
        </div>
      </div>`;
    } else {
      const ho = e.odds?.home || 1.85;
      const aw = e.odds?.away || 1.95;
      return `<div class="sport-event">
        <div class="event-header"><span class="event-league">🏀 NBA Virtuale</span></div>
        <div class="event-teams">
          <span class="team-name">${e.home}</span>
          <span class="event-vs">VS</span>
          <span class="team-name away">${e.away}</span>
        </div>
        <div class="event-odds two-way">
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${e.home}',away:'${e.away}',sport:'basket',matchId:'${mid}'},'${e.home} Vince',${ho},'home')">
            <span class="odd-label">Home</span><span class="odd-value">${ho}</span>
          </button>
          <button class="odd-btn" onclick="VirtualSports.addToBetSlip({id:'${mid}',home:'${e.home}',away:'${e.away}',sport:'basket',matchId:'${mid}'},'${e.away} Vince',${aw},'away')">
            <span class="odd-label">Away</span><span class="odd-value">${aw}</span>
          </button>
        </div>
      </div>`;
    }
  }

  function renderRace(sport) {
    // Mostra placeholder, poi carica i dati reali dal server
    const icon  = sport === 'cavalli' ? '🐎' : '🐕';
    const title = sport === 'cavalli' ? 'CORSE DI CAVALLI' : 'CORSE DI CANI';

    setTimeout(() => loadRaceFromServer(sport), 50);

    return `
      <div class="game-section" style="max-width:100%">
        <div class="page-header">
          <h2 class="page-title">${icon} ${title} VIRTUALI</h2>
          <p class="page-subtitle">Scommetti sul vincitore — Gara simulata in tempo reale</p>
        </div>
        <div id="race-container-${sport}">
          <div class="info-box text-center" style="margin:1rem">⏳ Caricamento corridori dal server...</div>
        </div>
      </div>`;
  }

  // raceId corrente per ogni sport
  const currentRaceId = {};

  async function loadRaceFromServer(sport) {
    const container = document.getElementById(`race-container-${sport}`);
    if (!container) return;

    container.innerHTML = '<div class="info-box text-center" style="margin:1rem">⏳ Caricamento corridori dal server...</div>';

    let preview;
    try {
      preview = await API.racePreview(sport);
    } catch (_) {
      container.innerHTML = '<div class="info-box text-center">⚠️ Impossibile caricare i corridori. Riprova.</div>';
      return;
    }

    if (!preview.ok) return;

    // Salva il raceId per usarlo nella simulazione
    currentRaceId[sport] = preview.raceId;
    const runners = preview.runners;
    const icon    = sport === 'cavalli' ? '🐎' : '🐕';
    const jockeys = ['M. Rossi','L. Ferrari','G. Bianchi','A. Ricci','F. Costa','R. Mancini'];

    container.innerHTML = `
      <div class="race-track">
        ${runners.map((r, i) => `
          <div class="race-runner">
            <div class="runner-num" style="background:${RUNNER_COLORS[i%RUNNER_COLORS.length]}20;color:${RUNNER_COLORS[i%RUNNER_COLORS.length]};border:1px solid ${RUNNER_COLORS[i%RUNNER_COLORS.length]}40">${r.idx+1}</div>
            <div class="runner-name" id="runner-name-${sport}-${i}">${r.name}${sport==='cavalli'?` <small style="color:var(--text-3)">(${jockeys[i%jockeys.length]})</small>`:''}</div>
            <div class="runner-track">
              <div class="runner-progress" id="runner-bar-${sport}-${i}" style="background:${RUNNER_COLORS[i%RUNNER_COLORS.length]}"></div>
            </div>
            <div class="runner-odds" id="runner-odds-${sport}-${i}">${r.odd}x</div>
            <button class="runner-bet-btn" data-sport="${sport}" data-idx="${i}" onclick="VirtualSports.setRaceBet('${sport}',${i},${r.odd})">Scommetti</button>
          </div>`).join('')}
      </div>
      <input type="hidden" id="race-count-${sport}" value="${runners.length}">
      ${createBetControls(`race-${sport}`, 10)}
      <div class="game-btn-row">
        <button class="btn-game btn-deal" id="race-btn-${sport}" onclick="VirtualSports.startRace('${sport}')">🏁 LANCIA LA GARA!</button>
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

  function delay(ms) { return new Promise(r => setTimeout(r, ms)); }

  return {
    renderFootball, renderTennis, renderBasket, renderRace,
    addToBetSlip, removeBet, clearSlip, placeBet,
    setRaceBet, startRace
  };
})();
