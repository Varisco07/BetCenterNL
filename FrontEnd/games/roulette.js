// =============================================
// BetCenterNL — ROULETTE (collegata al backend)
// =============================================

const RouletteGame = (() => {
  const RED_NUMS = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];

  let selectedBets = []; // [{type, value, label, odds, amount}]
  let spinning = false;

  function getColor(n) {
    if (n === 0) return 'green';
    return RED_NUMS.includes(n) ? 'red' : 'black';
  }

  function numbersGrid() {
    let html = '';
    const nums = [0,3,6,9,12,15,18,21,24,27,30,33,36,
                  2,5,8,11,14,17,20,23,26,29,32,35,
                  1,4,7,10,13,16,19,22,25,28,31,34];
    for (const n of nums) {
      html += `<div class="roul-num ${getColor(n)}" onclick="RouletteGame.betNum(${n})" title="${n}">${n}</div>`;
    }
    return html;
  }

  function generateWheelStrip() {
    const numbers = [0,26,3,35,12,28,7,29,18,22,9,31,14,20,1,33,16,24,5,34,17,6,27,13,36,11,30,8,23,10,32,15,19,4,21,2,25];
    let html = '';
    for (let r = 0; r < 4; r++) {
      for (const num of numbers) {
        const color = getColor(num);
        const bg = color==='red'?'#e74c3c':color==='black'?'#000':'#1abc9c';
        html += `<div class="strip-number" style="background:${bg}">${num}</div>`;
      }
    }
    return html;
  }

  function render() {
    return `
      <div class="game-section" style="max-width:1000px">
        <div class="game-header">
          <h2 class="game-title">⭕ ROULETTE EUROPEA</h2>
          <div class="game-balance" id="rl-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="roulette-container">
          <div class="roulette-wheel-area">
            <div class="roulette-strip-wrapper">
              <div class="roulette-pointer-h"></div>
              <div class="roulette-strip" id="rl-wheel">${generateWheelStrip()}</div>
            </div>
            <div class="roulette-result-num" id="rl-result-num">—</div>
            <div id="rl-result"></div>
          </div>
          <div class="roulette-table">
            <div class="roulette-numbers" id="rl-numbers">${numbersGrid()}</div>
            <div style="margin:0.5rem 0;font-size:0.75rem;color:var(--text-2)">Scommesse esterne:</div>
            <div class="roulette-outside-bets">
              <div class="outside-bet" onclick="RouletteGame.betOutside('red','Rosso',1.9)">🔴 Rosso</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('black','Nero',1.9)">⚫ Nero</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('even','Pari',1.9)">Pari</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('odd','Dispari',1.9)">Dispari</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('low','1-18',1.9)">1–18</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('high','19-36',1.9)">19–36</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('dozen1','1ª Dozzina',2.9)">1–12</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('dozen2','2ª Dozzina',2.9)">13–24</div>
              <div class="outside-bet" onclick="RouletteGame.betOutside('dozen3','3ª Dozzina',2.9)">25–36</div>
            </div>
            <div class="section-divider"></div>
            <div id="rl-bet-list" style="font-size:0.8rem;color:var(--text-1);min-height:40px;margin-bottom:0.5rem"></div>
            ${createBetControls('rl', 5)}
            <div class="game-btn-row">
              <button class="btn-game btn-deal" id="rl-spin" onclick="RouletteGame.spin()">🎯 LANCIA!</button>
              <button class="btn-game btn-clear" onclick="RouletteGame.clearBets()">Cancella</button>
            </div>
          </div>
        </div>
      </div>`;
  }

  function updateBetList() {
    const el = document.getElementById('rl-bet-list');
    if (!el) return;
    if (!selectedBets.length) { el.textContent = 'Nessuna scommessa selezionata'; return; }
    el.innerHTML = selectedBets.map((b, i) =>
      `<span style="background:var(--bg-3);border:1px solid var(--border);border-radius:4px;padding:0.2rem 0.5rem;margin:2px;display:inline-block">
        ${b.label} (${b.odds}x) €${b.amount}
        <span onclick="RouletteGame.removeBet(${i})" style="cursor:pointer;color:var(--red);margin-left:4px">✕</span>
      </span>`
    ).join('');
  }

  function betNum(n) {
    const bet = getBet('rl');
    if (!bet || bet < 1) { showToast('Imposta la puntata prima', 'info'); return; }
    selectedBets.push({ type: 'number', value: n, label: `Num ${n}`, odds: 36, amount: bet });
    updateBetList();
    document.querySelectorAll('.roul-num').forEach(el => {
      if (parseInt(el.title) === n) el.classList.toggle('selected');
    });
  }

  function betOutside(type, label, odds) {
    const bet = getBet('rl');
    if (!bet || bet < 1) { showToast('Imposta la puntata prima', 'info'); return; }
    selectedBets = selectedBets.filter(b => b.type !== type);
    selectedBets.push({ type, label, odds, amount: bet });
    updateBetList();
    document.querySelectorAll('.outside-bet').forEach(el => {
      el.classList.toggle('selected', el.textContent.includes(label));
    });
  }

  function removeBet(idx) { selectedBets.splice(idx, 1); updateBetList(); }

  function clearBets() {
    selectedBets = [];
    updateBetList();
    document.querySelectorAll('.roul-num, .outside-bet').forEach(el => el.classList.remove('selected'));
  }

  async function spin() {
    if (spinning) return;
    if (!selectedBets.length) { showToast('Piazza almeno una scommessa!', 'info'); return; }

    const totalBet = selectedBets.reduce((s, b) => s + b.amount, 0);
    if (State.balance < totalBet) { showToast('Saldo insufficiente!', 'lose'); return; }

    spinning = true;
    document.getElementById('rl-spin').disabled = true;
    try { AudioEngine.play('rouletteSpin'); } catch (_) {}

    // Animazione ruota
    const wheel = document.getElementById('rl-wheel');
    const numbers = [0,26,3,35,12,28,7,29,18,22,9,31,14,20,1,33,16,24,5,34,17,6,27,13,36,11,30,8,23,10,32,15,19,4,21,2,25];
    if (wheel) {
      wheel.style.transition = 'none';
      wheel.style.transform  = 'translateX(0px)';
      wheel.offsetHeight;
      setTimeout(() => {
        wheel.style.transition = 'transform 3s cubic-bezier(0.25,0.1,0.25,1)';
        wheel.style.transform  = `translateX(-${numbers.length * 60 * 2}px)`;
      }, 50);
    }

    const resultNumEl = document.getElementById('rl-result-num');
    if (resultNumEl) resultNumEl.innerHTML = '<span class="spinning-indicator">🎰 Girando...</span>';

    // Chiama il server
    let serverResult;
    try {
      serverResult = await API.spinRoulette(selectedBets);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      spinning = false;
      document.getElementById('rl-spin').disabled = false;
      return;
    }

    await delay(3200);

    const { spin: spinResult, betResults, totalWin, gain, newBalance } = serverResult;
    const { number, color } = spinResult;
    const colorEmoji = { red:'🔴', black:'⚫', green:'🟢' }[color];

    // Ferma ruota sul numero corretto
    if (wheel) {
      const idx = numbers.indexOf(number);
      const finalPos = -(numbers.length * 60 + idx * 60) + 300;
      wheel.style.transition = 'transform 0.5s ease-out';
      wheel.style.transform  = `translateX(${finalPos}px)`;
    }

    if (resultNumEl) resultNumEl.innerHTML = `<span class="result-number-animate">${colorEmoji} ${number}</span>`;

    State.syncBalance(newBalance);

    const resultEl = document.getElementById('rl-result');
    if (totalWin > 0) {
      if (resultEl) resultEl.innerHTML = `<div class="result-banner result-win">✅ VINCI ${formatCurrency(totalWin)}!</div>`;
      showToast(`🎯 Numero ${number} — Vinto ${formatCurrency(totalWin)}!`, 'win');
      if (totalWin > 50) try { VFX.celebrate(); } catch (_) {}
      State.recordHistory({ game: 'Roulette', bet: totalBet, result: 'win', gain });
    } else {
      if (resultEl) resultEl.innerHTML = `<div class="result-banner result-lose">❌ Numero ${number} — Nessuna vincita</div>`;
      showToast(`⭕ Numero ${number} — Perso`, 'lose');
      try { VFX.screenShake(); } catch (_) {}
      State.recordHistory({ game: 'Roulette', bet: totalBet, result: 'lose', gain });
    }

    const balEl = document.getElementById('rl-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    spinning = false;
    clearBets();
    document.getElementById('rl-spin').disabled = false;

    setTimeout(() => {
      if (wheel) { wheel.style.transition='transform 0.8s ease-out'; wheel.style.transform='translateX(0px)'; }
    }, 3000);
    setTimeout(() => {
      if (resultEl)    resultEl.innerHTML = '';
      if (resultNumEl) resultNumEl.innerHTML = '—';
    }, 5000);
  }

  return { render, betNum, betOutside, removeBet, clearBets, spin };
})();

if (typeof RouletteGame === 'undefined') {
  console.error('❌ RouletteGame non caricato');
} else {
  console.log('✅ RouletteGame caricato');
}
