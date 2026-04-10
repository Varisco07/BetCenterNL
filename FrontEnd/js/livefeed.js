// =============================================
// BetCenterNL — LIVE ACTIVITY FEED
// =============================================

const LiveFeed = (() => {
  const FAKE_PLAYERS = [
    'Marco_R', 'Sofia99', 'Gianni_B', 'Laura_V', 'Alex_K',
    'Federica', 'Luca_M', 'Chiara_P', 'Dario_T', 'Elena_S',
    'Matteo_C', 'Valentina', 'Roberto_N', 'Anna_G', 'Simone_D',
    'Francesca', 'Antonio_L', 'Giulia_F', 'Paolo_Z', 'Marta_B'
  ];

  const ACTIVITIES = [
    (p, a) => ({ msg: `${p} ha vinto ${a} alle Slot! 🎰`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha fatto Blackjack! +${a} 🃏`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha vinto ${a} alla Roulette sul numero ${Math.floor(Math.random()*37)} ⭕`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha vinto ${a} al Video Poker con Full House ♠`, type: 'win' }),
    (p, a) => ({ msg: `${p} ha scommesso ${a} sul Calcio Virtuale ⚽`, type: 'bet' }),
    (p, a) => ({ msg: `${p} ha piazzato €${(Math.random()*50+10).toFixed(0)} sulle Corse Cavalli 🐎`, type: 'bet' }),
    (p, a) => ({ msg: `${p} si è iscritto! Benvenuto 👋`, type: 'info' }),
    (p, a) => ({ msg: `${p} ha raggiunto il Livello ${Math.floor(Math.random()*6+2)} ⭐`, type: 'level' }),
    (p, a) => ({ msg: `${p} ha sblocato il traguardo "High Roller" 🏅`, type: 'level' }),
    (p, a) => ({ msg: `${p} ha vinto ${a} al Baccarat 💎`, type: 'win' }),
  ];

  function randomAmount() {
    const amounts = [12, 25, 48, 75, 120, 200, 350, 500, 750, 1200, 2000];
    return `€${amounts[Math.floor(Math.random() * amounts.length)].toLocaleString('it-IT')}`;
  }

  function generateActivity() {
    const player = FAKE_PLAYERS[Math.floor(Math.random() * FAKE_PLAYERS.length)];
    const template = ACTIVITIES[Math.floor(Math.random() * ACTIVITIES.length)];
    return { ...template(player, randomAmount()), time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' }) };
  }

  let feedEl = null;
  let items = [];
  let interval = null;

  function pushItem(item) {
    items.unshift(item);
    if (items.length > 20) items = items.slice(0, 20);
    renderFeed();
  }

  function renderFeed() {
    if (!feedEl) feedEl = document.getElementById('live-feed-list');
    if (!feedEl) return;

    const icons = { win: '🏆', bet: '🎯', info: 'ℹ️', level: '⭐' };
    const colors = { win: 'var(--green)', bet: 'var(--accent)', info: 'var(--text-2)', level: '#facc15' };

    feedEl.innerHTML = items.map(item => `
      <div class="feed-item" style="animation:slide-in-right 0.3s ease">
        <span style="font-size:0.9rem">${icons[item.type] || '•'}</span>
        <span class="feed-msg" style="flex:1;font-size:0.8rem;color:${colors[item.type] || 'var(--text-1)'}">${item.msg}</span>
        <span class="feed-time">${item.time}</span>
      </div>
    `).join('');
  }

  function start() {
    if (interval) return;
    // Pre-populate with some fake history
    for (let i = 0; i < 8; i++) {
      const item = generateActivity();
      const mins = Math.floor(Math.random() * 30);
      item.time = new Date(Date.now() - mins * 60000).toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' });
      items.push(item);
    }
    renderFeed();

    interval = setInterval(() => {
      if (Math.random() > 0.3) { // 70% chance each tick
        pushItem(generateActivity());
        AudioEngine.play('notification');
      }
    }, 4000 + Math.random() * 3000);
  }

  function stop() {
    if (interval) { clearInterval(interval); interval = null; }
  }

  // Called from vfx.js addBalance patch to add real player wins
  function addRealWin(game, amount) {
    if (!State.user) return;
    pushItem({
      msg: `${State.user.username || State.user.nome} ha vinto ${formatCurrency(amount)} a ${game}!`,
      type: 'win',
      time: new Date().toLocaleTimeString('it-IT', { hour: '2-digit', minute: '2-digit' })
    });
  }

  function renderPanel() {
    return `
      <div class="live-feed-panel">
        <div class="live-feed-header">
          <div class="live-badge"><div class="live-dot"></div>LIVE FEED</div>
          <button class="feed-close" onclick="LiveFeed.togglePanel()">✕</button>
        </div>
        <div class="live-feed-list" id="live-feed-list"></div>
      </div>`;
  }

  let panelOpen = false;
  function togglePanel() {
    panelOpen = !panelOpen;
    let panel = document.getElementById('live-feed-panel');
    if (panelOpen) {
      if (!panel) {
        panel = document.createElement('div');
        panel.id = 'live-feed-panel';
        panel.innerHTML = renderPanel();
        document.body.appendChild(panel);
        feedEl = null;
        renderFeed();
        start();
      }
      panel.classList.add('open');
    } else {
      if (panel) panel.classList.remove('open');
    }
  }

  return { start, stop, pushItem, addRealWin, togglePanel, renderFeed };
})();
