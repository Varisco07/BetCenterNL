// =============================================
// BetCenterNL — SECTIONS RENDERER (FINAL)
// =============================================

const Sections = {

  lobby() {
    const cards = [
      { section:'slots',     icon:'🎰', name:'Slot Machine',   desc:'8 simboli, auto-spin, jackpot progressivo fino a 200x.',                tag:'tag-hot',    tagLabel:'🔥 HOT' },
      { section:'blackjack', icon:'🃏', name:'Blackjack',      desc:'Classico 21 con hit, stand e doppio. Blackjack paga 3:2.',              tag:'tag-casino', tagLabel:'Casino' },
      { section:'poker',     icon:'♠',  name:'Video Poker',    desc:'Jacks or Better. Royal Flush paga 800x!',                              tag:'tag-casino', tagLabel:'Casino' },
      { section:'roulette',  icon:'⭕', name:'Roulette',       desc:'Europea (zero singolo). Numeri, colori, dozzine, colonne.',             tag:'tag-casino', tagLabel:'Casino' },
      { section:'dadi',      icon:'🎲', name:'Craps',          desc:'Pass Line, Field Bet, Hardways. Il dado porta fortuna.',               tag:'tag-casino', tagLabel:'Casino' },
      { section:'baccarat',  icon:'💎', name:'Baccarat',       desc:'Giocatore vs Banco. Più vicino a 9. Tie paga 8:1.',                    tag:'tag-casino', tagLabel:'Casino' },
      { section:'chicken',   icon:'🐔', name:'Chicken Dash',   desc:'Torre verticale con 10 livelli. Sali evitando le auto. Moltiplicatore crescente!', tag:'tag-hot',    tagLabel:'🔥 HOT' },
      { section:'calcio',    icon:'⚽', name:'Calcio Virtual', desc:'Serie A, Champions, Premier virtuali. Quote 1X2 live.',               tag:'tag-sport',  tagLabel:'Virtual' },
      { section:'tennis',    icon:'🎾', name:'Tennis Virtual', desc:'Cemento, erba, terra. I tuoi campioni preferiti.',                    tag:'tag-sport',  tagLabel:'Virtual' },
      { section:'basket',    icon:'🏀', name:'Basket Virtual', desc:'NBA virtuale, spread e totale. Le migliori franchigie.',              tag:'tag-sport',  tagLabel:'Virtual' },
      { section:'cavalli',   icon:'🐎', name:'Corse Cavalli',  desc:'8 cavalli, gara animata in tempo reale. Quote variabili.',            tag:'tag-sport',  tagLabel:'Virtual' },
      { section:'cani',      icon:'🐕', name:'Corse Cani',     desc:'8 greyhound, simulazione live. Quote fino a 12x.',                   tag:'tag-sport',  tagLabel:'Virtual' },
    ];

    const wins     = State.history.filter(h => h.result === 'win').length;
    const played   = State.history.length;
    const netGain  = State.history.reduce((s, h) => s + (h.gain || 0), 0);
    const casino   = cards.filter(c => c.tag !== 'tag-sport');
    const sport    = cards.filter(c => c.tag === 'tag-sport');

    return `<div>
      <div class="lobby-hero">
        <div class="hero-text">
          <h2>Bentornato,<br>${State.user?.nome || 'Giocatore'}!</h2>
          <p>${new Date().toLocaleDateString('it-IT',{weekday:'long',day:'numeric',month:'long',year:'numeric'})}</p>
        </div>
        <div class="hero-stats">
          <div class="hero-stat"><span class="hero-stat-value">${formatCurrency(State.balance)}</span><span class="hero-stat-label">Saldo</span></div>
          <div class="hero-stat"><span class="hero-stat-value">${played}</span><span class="hero-stat-label">Partite</span></div>
          <div class="hero-stat"><span class="hero-stat-value" style="color:${netGain>=0?'var(--green)':'var(--red)'}">${netGain>=0?'+':''}${formatCurrency(netGain)}</span><span class="hero-stat-label">Netto</span></div>
        </div>
      </div>

      ${LevelSystem.getLevelWidget()}

      <div class="promo-banner">
        <div class="promo-icon">🎁</div>
        <div class="promo-text">
          <strong>Bonus Giornaliero Disponibile!</strong>
          <span>Ritira ogni giorno per premi crescenti. Serie massima: €500/giorno.</span>
        </div>
        <button class="promo-action" onclick="navigateTo('bonus')">Ritira</button>
      </div>

      <div style="font-size:0.72rem;font-weight:600;letter-spacing:0.12em;text-transform:uppercase;color:var(--text-3);margin-bottom:0.75rem">🎰 GIOCHI CASINO</div>
      <div class="lobby-grid" style="margin-bottom:2rem">
        ${casino.map(c=>`<div class="game-card" onclick="navigateTo('${c.section}')">
          <span class="game-card-icon">${c.icon}</span>
          <div class="game-card-name">${c.name}</div>
          <div class="game-card-desc">${c.desc}</div>
          <span class="game-card-tag ${c.tag}">${c.tagLabel}</span>
        </div>`).join('')}
      </div>

      <div class="section-divider"></div>

      <div style="font-size:0.72rem;font-weight:600;letter-spacing:0.12em;text-transform:uppercase;color:var(--text-3);margin:1.5rem 0 0.75rem">⚡ SCOMMESSE VIRTUALI</div>
      <div class="lobby-grid" style="margin-bottom:2rem">
        ${sport.map(c=>`<div class="game-card" onclick="navigateTo('${c.section}')">
          <span class="game-card-icon">${c.icon}</span>
          <div class="game-card-name">${c.name}</div>
          <div class="game-card-desc">${c.desc}</div>
          <span class="game-card-tag ${c.tag}">${c.tagLabel}</span>
        </div>`).join('')}
      </div>

      ${LevelSystem.getAchievementsHTML()}

      <div class="resp-gambling-footer">
        <strong>⚠️ Gioco Responsabile</strong><br>
        Questo è un sito dimostrativo. Nessuna transazione reale. Il gioco d'azzardo può causare dipendenza.<br>
        <a href="#" onclick="navigateTo('responsible');return false">Informazioni sul gioco responsabile</a>
      </div>
    </div>`;
  },

  wallet() {
    // Carica dati aggiornati dal server in background, poi ri-renderizza
    Promise.all([
      API.getGameHistory(100),
      API.getProfile()
    ]).then(([histResult, profileResult]) => {
      let changed = false;
      if (histResult.ok && histResult.history) {
        State.history = histResult.history.map(h => ({
          id:        h.id,
          timestamp: new Date(h.timestamp).toLocaleString('it-IT'),
          game:      h.game, bet: h.bet, result: h.result, gain: h.gain
        }));
        changed = true;
      }
      if (profileResult.ok && profileResult.user) {
        State.user = { ...State.user, ...profileResult.user };
        changed = true;
      }
      // Aggiorna solo le stat card senza ricaricare tutto
      if (changed && State.currentSection === 'wallet') {
        Sections._updateWalletStats();
      }
    }).catch(() => {});

    // Usa statistiche dal profilo utente (server) se disponibili, altrimenti da history locale
    const user = State.user || {};
    const wins   = user.gamesWon   ?? State.history.filter(h => h.result==='win').length;
    const loses  = user.gamesLost  ?? State.history.filter(h => h.result==='lose').length;
    const played = user.gamesPlayed ?? State.history.length;
    const total  = user.totalGain  ?? State.history.reduce((s,h)=>s+(h.gain||0),0);
    const pushes = State.history.filter(h => h.result==='push').length;
    const bet    = State.history.reduce((s,h)=>s+(h.bet||0),0);
    const wr     = user.winRate != null
      ? parseFloat(user.winRate).toFixed(1)
      : (played ? ((wins/played)*100).toFixed(1) : '0.0');
    const avg    = State.history.length ? (bet/State.history.length).toFixed(2) : '0.00';
    const xp     = user.xp ?? LevelSystem.getTotalXP();
    const level  = LevelSystem.getCurrentLevel(xp);

    const byGame = {};
    State.history.forEach(h=>{
      if(!byGame[h.game]) byGame[h.game]={plays:0,gain:0,wins:0};
      byGame[h.game].plays++;
      byGame[h.game].gain += h.gain||0;
      if(h.result==='win') byGame[h.game].wins++;
    });

    const last10 = State.history.slice(0,10).reverse();
    const miniChart = last10.length ? `
      <div class="mini-chart">
        ${last10.map(h=>`<div class="mini-bar ${h.result==='win'?'win':'lose'}" style="height:${Math.min(100,Math.max(10,Math.abs(h.gain||0)/2))}%"></div>`).join('')}
      </div>
      <div style="font-size:0.7rem;color:var(--text-3);margin-top:0.25rem">Ultime ${last10.length} partite</div>
    ` : '';

    return `<div>
      <div class="page-header">
        <h2 class="page-title">💳 PORTAFOGLIO & STATISTICHE</h2>
        <p class="page-subtitle">Profilo completo del giocatore</p>
      </div>
      ${LevelSystem.getLevelWidget()}
      <div class="wallet-grid">
        <div class="stat-card">
          <div class="stat-label">Saldo Attuale</div>
          <div class="stat-value text-accent">${formatCurrency(State.balance)}</div>
          <div class="stat-change ${total>=0?'up':'down'}">${total>=0?'▲':'▼'} ${total>=0?'+':''}${formatCurrency(total)} netto</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">Partite Totali</div>
          <div class="stat-value">${played}</div>
          <div style="color:var(--text-2);font-size:0.75rem;margin-top:0.25rem">Media puntata: ${formatCurrency(parseFloat(avg))}</div>
          ${miniChart}
        </div>
        <div class="stat-card">
          <div class="stat-label">Win Rate</div>
          <div class="stat-value text-green">${wr}%</div>
          <div style="font-size:0.78rem;margin-top:0.25rem">✅ ${wins} &nbsp;❌ ${loses}${pushes>0?` &nbsp;🤝 ${pushes}`:''}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">XP & Livello</div>
          <div class="stat-value text-accent">${xp.toLocaleString('it-IT')}</div>
          <div style="color:var(--text-2);font-size:0.75rem;margin-top:0.25rem">${level.icon} ${level.name} · Lv.${level.level}</div>
        </div>
      </div>
      <div class="game-btn-row" style="margin-bottom:1.5rem">
        <button class="btn-primary" style="max-width:200px" onclick="showDeposit()">+ Deposita Fondi</button>
        <button class="btn-ghost" style="max-width:200px" onclick="showToast('Prelievo disponibile in versione reale','info')">− Preleva</button>
      </div>
      ${Object.keys(byGame).length>0 ? `
        <div style="font-family:var(--font-display);font-size:1.4rem;letter-spacing:0.05em;margin-bottom:0.75rem">PER GIOCO</div>
        <div style="overflow-x:auto;margin-bottom:2rem">
          <table class="history-table">
            <thead><tr><th>Gioco</th><th>Partite</th><th>Vinte</th><th>Win%</th><th>Guadagno</th></tr></thead>
            <tbody>
              ${Object.entries(byGame).sort((a,b)=>b[1].plays-a[1].plays).map(([g,s])=>`
                <tr>
                  <td style="font-weight:500">${g}</td>
                  <td>${s.plays}</td>
                  <td class="text-green">${s.wins}</td>
                  <td>${((s.wins/s.plays)*100).toFixed(1)}%</td>
                  <td class="${s.gain>=0?'text-green':'text-red'}" style="font-family:var(--font-mono)">${s.gain>=0?'+':''}${formatCurrency(s.gain)}</td>
                </tr>`).join('')}
            </tbody>
          </table>
        </div>` : ''}
      ${LevelSystem.getAchievementsHTML()}
      <div class="section-divider"></div>
      <div class="info-box" style="margin-top:1rem">
        ⚠️ <strong>Gioco Responsabile:</strong> Sito dimostrativo — nessuna transazione reale.
        Il saldo è virtuale. Il gioco d'azzardo può causare dipendenza. Gioca sempre con consapevolezza.
      </div>
    </div>`;
  },

  // Aggiorna le stat card del portafoglio senza ricaricare la pagina
  _updateWalletStats() {
    const user   = State.user || {};
    const wins   = user.gamesWon   ?? State.history.filter(h => h.result==='win').length;
    const loses  = user.gamesLost  ?? State.history.filter(h => h.result==='lose').length;
    const played = user.gamesPlayed ?? State.history.length;
    const total  = user.totalGain  ?? State.history.reduce((s,h)=>s+(h.gain||0),0);
    const wr     = user.winRate != null
      ? parseFloat(user.winRate).toFixed(1)
      : (played ? ((wins/played)*100).toFixed(1) : '0.0');

    // Aggiorna i valori nelle stat card se esistono nel DOM
    const cards = document.querySelectorAll('.stat-card');
    if (cards.length >= 3) {
      // Saldo
      const saldoVal = cards[0].querySelector('.stat-value');
      if (saldoVal) saldoVal.textContent = formatCurrency(State.balance);
      // Partite
      const partiteVal = cards[1].querySelector('.stat-value');
      if (partiteVal) partiteVal.textContent = played;
      // Win rate
      const wrVal = cards[2].querySelector('.stat-value');
      if (wrVal) wrVal.textContent = wr + '%';
      const wrSub = cards[2].querySelector('div[style]');
      if (wrSub) wrSub.innerHTML = `✅ ${wins} &nbsp;❌ ${loses}`;
    }
  },

  history() {
    // Carica sempre storico aggiornato dal server
    API.getGameHistory(100).then(result => {
      if (!result.ok || !result.history) return;
      const serverHistory = result.history.map(h => ({
        id:        h.id,
        timestamp: new Date(h.timestamp).toLocaleString('it-IT'),
        game:      h.game,
        bet:       h.bet,
        result:    h.result,
        gain:      h.gain
      }));
      State.history = serverHistory;
      if (State.currentSection === 'history') {
        const ca = document.getElementById('content-area');
        if (ca) ca.innerHTML = Sections.historyHTML(serverHistory);
      }
    }).catch(() => {});

    return Sections.historyHTML(State.history);
  },

  historyHTML(h) {
    const total = h.reduce((s, item) => s + (item.gain || 0), 0);
    return `<div>
      <div class="page-header">
        <h2 class="page-title">📋 STORICO SCOMMESSE</h2>
        <p class="page-subtitle">Ultimi ${h.length} movimenti · Netto: <span class="${total>=0?'text-green':'text-red'}">${total>=0?'+':''}${formatCurrency(total)}</span></p>
      </div>
      ${h.length===0
        ? `<div class="info-box text-center">⏳ Caricamento storico dal server...</div>`
        : `<div style="overflow-x:auto">
        <table class="history-table">
          <thead><tr><th>Data/Ora</th><th>Gioco</th><th>Puntata</th><th>Esito</th><th>Guadagno</th></tr></thead>
          <tbody>
            ${h.map(item=>`<tr>
              <td style="font-family:var(--font-mono);font-size:0.78rem">${item.timestamp}</td>
              <td>${item.game}</td>
              <td style="font-family:var(--font-mono)">${formatCurrency(item.bet)}</td>
              <td><span class="badge badge-${item.result}">${item.result==='win'?'✅ Vinta':item.result==='push'?'🤝 Pareggio':'❌ Persa'}</span></td>
              <td class="${item.gain>0?'text-green':item.gain<0?'text-red':'text-accent'}" style="font-family:var(--font-mono)">${item.gain>0?'+':''}${formatCurrency(item.gain)}</td>
            </tr>`).join('')}
          </tbody>
        </table>
      </div>`}
    </div>`;
  },

  leaderboard() {
    return `<div>
      <div class="page-header">
        <h2 class="page-title">🏆 CLASSIFICA</h2>
        <p class="page-subtitle">I migliori giocatori di BetCenterNL</p>
      </div>
      ${Leaderboard.getHTML()}
    </div>`;
  },

  responsible() {
    return `<div style="max-width:700px">
      <div class="page-header">
        <h2 class="page-title">⚠️ GIOCO RESPONSABILE</h2>
        <p class="page-subtitle">La tua sicurezza è la nostra priorità</p>
      </div>

      <div class="info-box" style="margin-bottom:1.5rem;border-color:var(--accent)">
        <strong style="color:var(--accent)">⚠️ Importante:</strong> Questo è un sito <strong>dimostrativo</strong>.
        Nessuna transazione reale viene effettuata. Il saldo è completamente virtuale.
        Creato a scopo educativo/tecnico.
      </div>

      <div style="display:flex;flex-direction:column;gap:1rem">
        ${[
          { icon:'🎯', title:'Gioca con Limiti', text:'Stabilisci sempre un budget prima di giocare. Non superare mai le tue possibilità economiche.' },
          { icon:'⏰', title:'Gestisci il Tempo', text:'Limita il tempo dedicato al gioco. Fai pause regolari e non giocare quando sei stanco o stressato.' },
          { icon:'🧠', title:'Gioca per Divertimento', text:'Il gioco deve essere un\'attività di svago, non un modo per guadagnare denaro o risolvere problemi finanziari.' },
          { icon:'🚫', title:'Segnali di Attenzione', text:'Se senti il bisogno compulsivo di giocare, nascondi le perdite, o sacrifichi necessità per giocare, cerca aiuto.' },
          { icon:'📞', title:'Aiuto Disponibile', text:'In Italia: Giocatori Anonimi +39 02 8361 4480 · Linea Amica 1500 · www.giocaresponsabile.it' },
          { icon:'🔞', title:'Gioco Vietato ai Minori', text:'Il gioco d\'azzardo è vietato ai minori di 18 anni. BetCenterNL verifica l\'età in fase di registrazione.' },
        ].map(item=>`
          <div style="background:var(--bg-2);border:1px solid var(--border);border-radius:var(--radius);padding:1.25rem;display:flex;gap:1rem;align-items:flex-start">
            <span style="font-size:1.8rem;flex-shrink:0">${item.icon}</span>
            <div>
              <div style="font-weight:600;margin-bottom:0.3rem">${item.title}</div>
              <div style="font-size:0.85rem;color:var(--text-2);line-height:1.6">${item.text}</div>
            </div>
          </div>`).join('')}
      </div>

      <div style="margin-top:2rem;text-align:center">
        <button class="btn-primary" onclick="navigateTo('lobby')">Torna alla Lobby</button>
      </div>
    </div>`;
  }
};
