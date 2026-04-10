// =============================================
// BetCenterNL — CRAPS / DADI
// =============================================

const DadiGame = (() => {
  let selectedBet = null;
  let phase = 'comeout'; // comeout | point
  let point = null;
  let rolling = false;

  const BET_TYPES = [
    { id: 'pass', label: 'Pass Line', desc: 'Vinci su 7/11, perdi su 2/3/12', payout: 1 },
    { id: 'dontpass', label: "Don't Pass", desc: "Vinci su 2/3, perdi su 7/11", payout: 1 },
    { id: 'field', label: 'Field Bet', desc: 'Vinci su 2,3,4,9,10,11,12', payout: 1.5 },
    { id: 'any7', label: 'Any Seven', desc: 'Vinci se il totale è 7', payout: 4 },
    { id: 'hardway8', label: 'Hardway 8', desc: 'Vinci solo su 4+4', payout: 9 },
    { id: 'hardway6', label: 'Hardway 6', desc: 'Vinci solo su 3+3', payout: 9 },
  ];

  function dieHTML(id) {
    return `<div class="die" id="die-${id}" data-value="1">
      <div class="dot dot-1"></div><div class="dot dot-2"></div>
      <div class="dot dot-3"></div><div class="dot dot-4"></div>
      <div class="dot dot-5"></div><div class="dot dot-6"></div>
    </div>`;
  }

  function render() {
    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">🎲 CRAPS / DADI</h2>
          <div class="game-balance" id="dice-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="dice-area">
          ${dieHTML(1)}
          <div class="dice-sum" id="dice-sum">—</div>
          ${dieHTML(2)}
        </div>
        <div id="dice-point" style="text-align:center;font-size:0.85rem;color:var(--text-2);margin-bottom:0.75rem"></div>
        <div id="dice-result"></div>
        <div class="craps-bets">
          ${BET_TYPES.map(b => `
            <div class="craps-bet-btn ${selectedBet === b.id ? 'selected' : ''}" onclick="DadiGame.selectBet('${b.id}')">
              <strong>${b.label}</strong>
              <small>${b.payout}x • ${b.desc}</small>
            </div>
          `).join('')}
        </div>
        ${createBetControls('dice', 10)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal" id="dice-roll-btn" onclick="DadiGame.roll()">🎲 Lancia i Dadi</button>
        </div>
      </div>`;
  }

  function selectBet(id) {
    selectedBet = id;
    document.querySelectorAll('.craps-bet-btn').forEach(el => el.classList.remove('selected'));
    document.querySelectorAll('.craps-bet-btn').forEach(el => {
      if (el.textContent.includes(BET_TYPES.find(b => b.id === id)?.label)) el.classList.add('selected');
    });
  }

  function setDie(id, val) {
    const die = document.getElementById(`die-${id}`);
    if (die) die.dataset.value = val;
  }

  async function roll() {
    if (rolling) return;
    if (!selectedBet) { showToast('Scegli un tipo di scommessa!', 'info'); return; }
    const bet = getBet('dice');
    if (!bet || bet < 1) { showToast('Inserisci una puntata valida', 'info'); return; }
    if (!State.deductBalance(bet)) { showToast('Saldo insufficiente!', 'lose'); return; }

    rolling = true;
    document.getElementById('dice-roll-btn').disabled = true;
    document.getElementById('dice-result').innerHTML = '';

    // Animate roll
    const d1 = document.getElementById('die-1');
    const d2 = document.getElementById('die-2');
    if (d1) d1.classList.add('rolling');
    if (d2) d2.classList.add('rolling');
    AudioEngine.play('diceRoll');

    await delay(800);

    const v1 = Math.ceil(Math.random() * 6);
    const v2 = Math.ceil(Math.random() * 6);
    const total = v1 + v2;

    if (d1) { d1.classList.remove('rolling'); setDie(1, v1); }
    if (d2) { d2.classList.remove('rolling'); setDie(2, v2); }
    document.getElementById('dice-sum').textContent = total;

    // Evaluate
    const betDef = BET_TYPES.find(b => b.id === selectedBet);
    let win = false;
    let winAmt = 0;

    switch (selectedBet) {
      case 'pass':
        win = (total === 7 || total === 11); break;
      case 'dontpass':
        win = (total === 2 || total === 3); break;
      case 'field':
        win = [2,3,4,9,10,11,12].includes(total); break;
      case 'any7':
        win = total === 7; break;
      case 'hardway8':
        win = (v1 === 4 && v2 === 4); break;
      case 'hardway6':
        win = (v1 === 3 && v2 === 3); break;
    }

    if (win) {
      winAmt = parseFloat((bet * (betDef.payout + 1)).toFixed(2));
      State.addBalance(winAmt);
      document.getElementById('dice-result').innerHTML = `<div class="result-banner result-win">🎲 ${total} — VINCI ${formatCurrency(winAmt - bet)}!</div>`;
      showToast(`🎲 ${v1}+${v2}=${total} — Vinto!`, 'win');
      State.recordHistory({ game: 'Dadi', bet, result: 'win', gain: winAmt - bet });
    } else {
      document.getElementById('dice-result').innerHTML = `<div class="result-banner result-lose">🎲 ${total} — Nessuna vincita</div>`;
      showToast(`🎲 ${v1}+${v2}=${total} — Perso`, 'lose');
      State.recordHistory({ game: 'Dadi', bet, result: 'lose', gain: -bet });
    }

    const balEl = document.getElementById('dice-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    rolling = false;
    document.getElementById('dice-roll-btn').disabled = false;
  }

  return { render, selectBet, roll };
})();
