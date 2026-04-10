// =============================================
// BetCenterNL — ROULETTE EUROPEA
// =============================================

const RouletteGame = (() => {
  const RED_NUMS = [1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36];
  const BLACK_NUMS = [2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35];

  let selectedBets = []; // [{type, value, label, odds}]
  let spinning = false;
  let lastResult = null;

  function getColor(n) {
    if (n === 0) return 'green';
    return RED_NUMS.includes(n) ? 'red' : 'black';
  }

  function numbersGrid() {
    let html = '';
    const allNums = [0,3,6,9,12,15,18,21,24,27,30,33,36,
                     2,5,8,11,14,17,20,23,26,29,32,35,
                     1,4,7,10,13,16,19,22,25,28,31,34];
    for (const n of allNums) {
      const col = getColor(n);
      html += `<div class="roul-num ${col}" onclick="RouletteGame.betNum(${n})" title="${n}">${n}</div>`;
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
              <div class="roulette-strip" id="rl-wheel">
                ${generateWheelStrip()}
              </div>
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
              <button class="btn-game btn-clear" onclick="RouletteGame.clearBets()">Cancella Scommesse</button>
            </div>
          </div>
        </div>
      </div>`;
  }

  function generateWheelStrip() {
    const numbers = [0,26,3,35,12,28,7,29,18,22,9,31,14,20,1,33,16,24,5,34,17,6,27,13,36,11,30,8,23,10,32,15,19,4,21,2,25];
    let html = '';
    for (let i = 0; i < numbers.length; i++) {
      const num = numbers[i];
      const color = getColor(num);
      const bgColor = color === 'red' ? '#e74c3c' : color === 'black' ? '#000' : '#1abc9c';
      html += `<div class="strip-number" style="background:${bgColor}">${num}</div>`;
    }
    return html;
  }

  function updateBetList() {
    const el = document.getElementById('rl-bet-list');
    if (!el) return;
    if (selectedBets.length === 0) {
      el.textContent = 'Nessuna scommessa selezionata';
      return;
    }
    el.innerHTML = selectedBets.map((b, i) =>
      `<span style="background:var(--bg-3);border:1px solid var(--border);border-radius:4px;padding:0.2rem 0.5rem;margin:2px;display:inline-block">
        ${b.label} (${b.odds}x) <span onclick="RouletteGame.removeBet(${i})" style="cursor:pointer;color:var(--red);margin-left:4px">✕</span>
      </span>`
    ).join('');
  }

  function betNum(n) {
    const bet = getBet('rl');
    if (!bet || bet < 1) { showToast('Imposta la puntata prima', 'info'); return; }
    selectedBets.push({ type: 'number', value: n, label: `Num ${n}`, odds: 36, amount: bet });
    updateBetList();
    // Visual feedback
    document.querySelectorAll('.roul-num').forEach(el => {
      if (parseInt(el.title) === n) el.classList.toggle('selected');
    });
  }

  function betOutside(type, label, odds) {
    const bet = getBet('rl');
    if (!bet || bet < 1) { showToast('Imposta la puntata prima', 'info'); return; }
    // Remove duplicate
    selectedBets = selectedBets.filter(b => b.type !== type);
    selectedBets.push({ type, label, odds, amount: bet });
    updateBetList();
    document.querySelectorAll('.outside-bet').forEach(el => el.classList.remove('selected'));
    document.querySelectorAll('.outside-bet').forEach(el => {
      if (el.textContent.includes(label)) el.classList.add('selected');
    });
  }

  function removeBet(idx) {
    selectedBets.splice(idx, 1);
    updateBetList();
  }

  function clearBets() {
    selectedBets = [];
    updateBetList();
    document.querySelectorAll('.roul-num, .outside-bet').forEach(el => el.classList.remove('selected'));
  }

  async function spin() {
    if (spinning) return;
    if (selectedBets.length === 0) { showToast('Piazza almeno una scommessa!', 'info'); return; }

    const totalBet = selectedBets.reduce((s, b) => s + b.amount, 0);
    if (!State.deductBalance(totalBet)) { showToast('Saldo insufficiente!', 'lose'); return; }

    spinning = true;
    document.getElementById('rl-spin').disabled = true;
    AudioEngine.play('rouletteSpin');

    // Animate wheel with acceleration and deceleration
    const wheel = document.getElementById('rl-wheel');
    const result = Math.floor(Math.random() * 37); // 0-36
    const numbers = [0,26,3,35,12,28,7,29,18,22,9,31,14,20,1,33,16,24,5,34,17,6,27,13,36,11,30,8,23,10,32,15,19,4,21,2,25];
    const resultIndex = numbers.indexOf(result);
    const offset = -(resultIndex * 60) + 300; // 60px per numero, centered
    
    if (wheel) {
      wheel.style.transition = 'transform 5s cubic-bezier(0.25, 0.46, 0.45, 0.94)';
      wheel.style.transform = `translateX(${offset}px)`;
    }

    await delay(5200);

    // Calculate wins
    let totalWin = 0;
    const winBets = [];
    for (const b of selectedBets) {
      if (checkBetWin(b, result)) {
        const win = parseFloat((b.amount * b.odds).toFixed(2));
        totalWin += win;
        winBets.push(b.label);
      }
    }

    // Update result with animation
    const color = getColor(result);
    const colorEmoji = { red: '🔴', black: '⚫', green: '🟢' }[color];
    const resultNumEl = document.getElementById('rl-result-num');
    
    resultNumEl.innerHTML = `<span class="result-number-animate">${colorEmoji} ${result}</span>`;
    resultNumEl.classList.add('result-pop');

    await delay(500);

    const resultEl = document.getElementById('rl-result');
    if (totalWin > 0) {
      State.addBalance(totalWin);
      resultEl.innerHTML = `<div class="result-banner result-win">✅ VINCI ${formatCurrency(totalWin)}!</div>`;
      showToast(`🎯 Numero ${result} — Vinto ${formatCurrency(totalWin)}!`, 'win');
      if (totalWin > 50) VFX.celebrate();
      State.recordHistory({ game: 'Roulette', bet: totalBet, result: 'win', gain: totalWin - totalBet });
    } else {
      resultEl.innerHTML = `<div class="result-banner result-lose">❌ Numero ${result} — Nessuna vincita</div>`;
      showToast(`⭕ Numero ${result} — Perso`, 'lose');
      VFX.screenShake();
      State.recordHistory({ game: 'Roulette', bet: totalBet, result: 'lose', gain: -totalBet });
    }

    const balEl = document.getElementById('rl-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    spinning = false;
    clearBets();
    document.getElementById('rl-spin').disabled = false;
    
    // Reset wheel smoothly
    await delay(2000);
    if (wheel) {
      wheel.style.transition = 'transform 1s ease-out';
      wheel.style.transform = 'translateX(0)';
    }
    resultNumEl.classList.remove('result-pop');
  }

  function checkBetWin(bet, result) {
    const color = getColor(result);
    switch (bet.type) {
      case 'number': return bet.value === result;
      case 'red': return color === 'red';
      case 'black': return color === 'black';
      case 'even': return result > 0 && result % 2 === 0;
      case 'odd': return result > 0 && result % 2 !== 0;
      case 'low': return result >= 1 && result <= 18;
      case 'high': return result >= 19 && result <= 36;
      case 'dozen1': return result >= 1 && result <= 12;
      case 'dozen2': return result >= 13 && result <= 24;
      case 'dozen3': return result >= 25 && result <= 36;
      default: return false;
    }
  }

  return { render, betNum, betOutside, removeBet, clearBets, spin };
})();
