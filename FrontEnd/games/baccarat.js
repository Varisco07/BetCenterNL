// =============================================
// BetCenterNL — BACCARAT
// =============================================

const BaccaratGame = (() => {
  const SUITS = ['♠','♥','♦','♣'];
  const RANKS = ['A','2','3','4','5','6','7','8','9','10','J','Q','K'];
  let selectedBet = null; // 'player' | 'banker' | 'tie'
  let playing = false;

  function newDeck() {
    const d = [];
    for (const s of SUITS) for (const r of RANKS) d.push({ rank: r, suit: s });
    for (let i = d.length - 1; i > 0; i--) {
      const j = Math.floor(Math.random() * (i + 1));
      [d[i], d[j]] = [d[j], d[i]];
    }
    return d;
  }

  function cardVal(card) {
    if (['10','J','Q','K'].includes(card.rank)) return 0;
    if (card.rank === 'A') return 1;
    return parseInt(card.rank);
  }

  function handVal(hand) {
    return hand.reduce((s, c) => s + cardVal(c), 0) % 10;
  }

  function isRed(suit) { return suit === '♥' || suit === '♦'; }

  function cardHTML(card) {
    const color = isRed(card.suit) ? 'red' : 'black';
    return `<div class="playing-card ${color}">
      <div class="card-inner"><div class="card-rank">${card.rank}</div><div class="card-suit">${card.suit}</div></div>
    </div>`;
  }

  function render() {
    return `
      <div class="game-section">
        <div class="game-header">
          <h2 class="game-title">💎 BACCARAT</h2>
          <div class="game-balance" id="bc-bal">${formatCurrency(State.balance)}</div>
        </div>
        <div class="baccarat-table">
          <div class="baccarat-areas">
            <div class="baccarat-side">
              <div class="baccarat-side-label">GIOCATORE</div>
              <div class="card-row" id="bc-player-cards" style="justify-content:center;min-height:100px"></div>
              <div class="baccarat-score" id="bc-player-score">—</div>
            </div>
            <div class="baccarat-vs">VS</div>
            <div class="baccarat-side">
              <div class="baccarat-side-label">BANCO</div>
              <div class="card-row" id="bc-banker-cards" style="justify-content:center;min-height:100px"></div>
              <div class="baccarat-score" id="bc-banker-score">—</div>
            </div>
          </div>
        </div>
        <div id="bc-result"></div>
        <div class="baccarat-bets">
          <div class="bacc-bet-btn ${selectedBet==='player'?'selected':''}" onclick="BaccaratGame.selectBet('player')">
            <strong>GIOCATORE</strong><small>Paga 1:1</small>
          </div>
          <div class="bacc-bet-btn ${selectedBet==='tie'?'selected':''}" onclick="BaccaratGame.selectBet('tie')">
            <strong>PAREGGIO</strong><small>Paga 8:1</small>
          </div>
          <div class="bacc-bet-btn ${selectedBet==='banker'?'selected':''}" onclick="BaccaratGame.selectBet('banker')">
            <strong>BANCO</strong><small>Paga 0.95:1</small>
          </div>
        </div>
        ${createBetControls('bc', 10)}
        <div class="game-btn-row">
          <button class="btn-game btn-deal" onclick="BaccaratGame.play()">🃏 GIOCA</button>
        </div>
        <div class="info-box mt-2">
          <strong>Regole:</strong> Il valore delle carte 10/J/Q/K = 0, Asso = 1. La mano più vicina a 9 vince.
          Terza carta automatica secondo le regole standard. Commissione del 5% sul banco.
        </div>
      </div>`;
  }

  function selectBet(type) {
    selectedBet = type;
    document.querySelectorAll('.bacc-bet-btn').forEach(el => el.classList.remove('selected'));
    document.querySelectorAll('.bacc-bet-btn').forEach(el => {
      const t = { player: 'GIOCATORE', tie: 'PAREGGIO', banker: 'BANCO' }[type];
      if (el.textContent.includes(t)) el.classList.add('selected');
    });
  }

  async function play() {
    if (playing) return;
    if (!selectedBet) { showToast('Scegli su chi scommettere!', 'info'); return; }
    const bet = getBet('bc');
    if (!bet || bet < 1) { showToast('Inserisci una puntata', 'info'); return; }
    if (!State.deductBalance(bet)) { showToast('Saldo insufficiente!', 'lose'); return; }

    playing = true;
    document.getElementById('bc-result').innerHTML = '';
    const playerCards = document.getElementById('bc-player-cards');
    const bankerCards = document.getElementById('bc-banker-cards');
    if (playerCards) playerCards.innerHTML = '';
    if (bankerCards) bankerCards.innerHTML = '';
    document.getElementById('bc-player-score').textContent = '—';
    document.getElementById('bc-banker-score').textContent = '—';

    const deck = newDeck();
    let player = [deck.pop(), deck.pop()];
    let banker = [deck.pop(), deck.pop()];

    // Animate cards
    for (const c of player) {
      await delay(200);
      if (playerCards) playerCards.innerHTML += cardHTML(c);
    }
    for (const c of banker) {
      await delay(200);
      if (bankerCards) bankerCards.innerHTML += cardHTML(c);
    }

    let pv = handVal(player);
    let bv = handVal(banker);

    document.getElementById('bc-player-score').textContent = pv;
    document.getElementById('bc-banker-score').textContent = bv;

    // Natural check
    if (pv < 8 && bv < 8) {
      // Player third card rule
      if (pv <= 5) {
        await delay(500);
        player.push(deck.pop());
        if (playerCards) playerCards.innerHTML += cardHTML(player[2]);
        pv = handVal(player);
        document.getElementById('bc-player-score').textContent = pv;
      }
      // Banker third card rule (simplified)
      if (bv <= 5) {
        await delay(500);
        banker.push(deck.pop());
        if (bankerCards) bankerCards.innerHTML += cardHTML(banker[2]);
        bv = handVal(banker);
        document.getElementById('bc-banker-score').textContent = bv;
      }
    }

    await delay(400);
    pv = handVal(player);
    bv = handVal(banker);

    let winner = pv > bv ? 'player' : bv > pv ? 'banker' : 'tie';
    let winAmt = 0;
    let resultType = 'lose';

    if (selectedBet === winner) {
      const payouts = { player: 2, banker: 1.95, tie: 9 };
      winAmt = parseFloat((bet * payouts[winner]).toFixed(2));
      State.addBalance(winAmt);
      resultType = 'win';
    } else if (winner === 'tie' && selectedBet !== 'tie') {
      State.addBalance(bet); // Push on tie (non-tie bets)
      resultType = 'push';
    }

    const labels = { player: 'GIOCATORE', banker: 'BANCO', tie: 'PAREGGIO' };
    let resultMsg = '';
    if (resultType === 'win') resultMsg = `✅ ${labels[winner]} VINCE! +${formatCurrency(winAmt - bet)}`;
    else if (resultType === 'push') resultMsg = `🤝 PAREGGIO — Puntata restituita`;
    else resultMsg = `❌ ${labels[winner]} VINCE`;

    document.getElementById('bc-result').innerHTML = `<div class="result-banner result-${resultType}">${resultMsg}</div>`;
    showToast(resultMsg.replace(/✅|❌|🤝/g,'').trim(), resultType === 'win' ? 'win' : resultType === 'push' ? 'info' : 'lose');

    State.recordHistory({
      game: 'Baccarat', bet,
      result: resultType,
      gain: resultType === 'win' ? winAmt - bet : resultType === 'push' ? 0 : -bet
    });

    const balEl = document.getElementById('bc-bal');
    if (balEl) balEl.textContent = formatCurrency(State.balance);
    playing = false;
  }

  return { render, selectBet, play };
})();
