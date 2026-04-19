// =============================================
// BetCenterNL — CRAPS / DADI (collegato al backend)
// =============================================

const DadiGame = (() => {
  let selectedBet = null;
  let rolling = false;

  const BET_TYPES = [
    { id: 'pass',      label: 'Pass Line',   desc: 'Vinci su 7/11, perdi su 2/3/12', payout: 1   },
    { id: 'dontpass',  label: "Don't Pass",  desc: "Vinci su 2/3, perdi su 7/11",    payout: 1   },
    { id: 'field',     label: 'Field Bet',   desc: 'Vinci su 2,3,4,9,10,11,12',      payout: 1.5 },
    { id: 'any7',      label: 'Any Seven',   desc: 'Vinci se il totale è 7',          payout: 4   },
    { id: 'hardway8',  label: 'Hardway 8',   desc: 'Vinci solo su 4+4',               payout: 9   },
    { id: 'hardway6',  label: 'Hardway 6',   desc: 'Vinci solo su 3+3',               payout: 9   },
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
        <div id="dice-result"></div>
        <div class="craps-bets">
          ${BET_TYPES.map(b => `
            <div class="craps-bet-btn ${selectedBet===b.id?'selected':''}" onclick="DadiGame.selectBet('${b.id}')">
              <strong>${b.label}</strong>
              <small>${b.payout}x • ${b.desc}</small>
            </div>`).join('')}
        </div>
        ${createBetControls('dice', 10)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal" id="dice-roll-btn" onclick="DadiGame.roll()">🎲 Lancia i Dadi</button>
        </div>
      </div>`;
  }

  function selectBet(id) {
    selectedBet = id;
    document.querySelectorAll('.craps-bet-btn').forEach(el => {
      const label = BET_TYPES.find(b => b.id === id)?.label;
      el.classList.toggle('selected', el.querySelector('strong')?.textContent === label);
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
    if (State.balance < bet) { showToast('Saldo insufficiente!', 'lose'); return; }

    rolling = true;
    document.getElementById('dice-roll-btn').disabled = true;
    document.getElementById('dice-result').innerHTML = '';

    const d1El = document.getElementById('die-1');
    const d2El = document.getElementById('die-2');
    if (d1El) d1El.classList.add('rolling');
    if (d2El) d2El.classList.add('rolling');
    try { AudioEngine.play('diceRoll'); } catch (_) {}

    await delay(800);

    let result;
    try {
      result = await API.rollDadi(bet, selectedBet);
    } catch (err) {
      showToast('Errore di connessione al server', 'lose');
      if (d1El) d1El.classList.remove('rolling');
      if (d2El) d2El.classList.remove('rolling');
      rolling = false;
      document.getElementById('dice-roll-btn').disabled = false;
      return;
    }

    const [v1, v2] = result.dice;
    if (d1El) { d1El.classList.remove('rolling'); setDie(1, v1); }
    if (d2El) { d2El.classList.remove('rolling'); setDie(2, v2); }
    document.getElementById('dice-sum').textContent = result.total;

    State.syncFromServer(result.newBalance);

    if (result.win) {
      const netGain = parseFloat((bet * result.multiplier).toFixed(2));
      document.getElementById('dice-result').innerHTML =
        `<div class="result-banner result-win">🎲 ${result.total} — VINCI ${formatCurrency(netGain)}!</div>`;
      showToast(`🎲 ${v1}+${v2}=${result.total} — Vinto!`, 'win');
      State.recordHistory({ game: 'Dadi', bet, result: 'win', gain: result.gain });
    } else {
      document.getElementById('dice-result').innerHTML =
        `<div class="result-banner result-lose">🎲 ${result.total} — Nessuna vincita</div>`;
      showToast(`🎲 ${v1}+${v2}=${result.total} — Perso`, 'lose');
      State.recordHistory({ game: 'Dadi', bet, result: 'lose', gain: result.gain });
    }

    const balEl = document.getElementById('dice-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);

    rolling = false;
    document.getElementById('dice-roll-btn').disabled = false;
  }

  return { render, selectBet, roll };
})();
