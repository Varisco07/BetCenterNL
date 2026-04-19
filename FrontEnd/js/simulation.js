// =============================================
// BetCenterNL — SIMULAZIONE STATISTICA
// =============================================

const SimulationGame = (() => {

  let running = false;

  function render() {
    return `
      <div class="game-section" style="max-width:700px">
        <div class="page-header">
          <h2 class="page-title">📊 SIMULAZIONE STATISTICA</h2>
          <p class="page-subtitle">
            100 partite per gioco · puntata fissa €10 · dimostra che alla lunga il banco vince sempre
          </p>
        </div>

        <div class="info-box" style="margin-bottom:1.5rem;border-color:var(--accent)">
          <strong style="color:var(--accent)">ℹ️ Come funziona:</strong>
          Il server simula 100 partite in parallelo per 3 giochi diversi usando la stessa logica
          del backend. I risultati mostrano il guadagno/perdita netto e il ROI reale.
        </div>

        <div id="sim-result" style="margin-bottom:1.5rem"></div>

        <div class="game-btn-row">
          <button class="btn-game btn-deal" id="sim-btn" onclick="SimulationGame.run()" style="max-width:300px">
            <span class="btn-icon">▶️</span>
            <span class="btn-text">AVVIA SIMULAZIONE</span>
          </button>
        </div>

        <div style="margin-top:2rem">
          <div style="font-size:0.8rem;color:var(--text-3);line-height:1.7">
            <strong>Giochi simulati:</strong><br>
            🃏 <strong>Blackjack</strong> — strategia base: stai su 17+, pesca altrimenti<br>
            🎲 <strong>Dadi (Pass Line)</strong> — vinci con 7 o 11<br>
            ⭕ <strong>Roulette (rosso)</strong> — payout 1.9x, probabilità ~48.6%<br><br>
            <strong>Perché si perde sempre?</strong><br>
            Ogni gioco ha un <em>house edge</em> integrato: la roulette paga 1.9x invece di 2x (house edge ~2.7%),
            i dadi hanno più combinazioni perdenti che vincenti sul Pass Line (house edge ~1.4%),
            il blackjack con strategia base ha house edge ~0.5% ma la varianza è alta.
            Su 100 partite la varianza può mascherarlo,
            ma su migliaia di partite il risultato converge sempre verso la perdita.
          </div>
        </div>
      </div>`;
  }

  async function run() {
    if (running) return;
    running = true;

    const btn = document.getElementById('sim-btn');
    const resultEl = document.getElementById('sim-result');
    if (btn) { btn.disabled = true; btn.innerHTML = '<span class="btn-icon">⏳</span><span class="btn-text">Simulazione in corso...</span>'; }
    if (resultEl) resultEl.innerHTML = '<div class="info-box text-center">⏳ Calcolo in corso sul server...</div>';

    let data;
    try {
      data = await API.runSimulation();
    } catch (err) {
      if (resultEl) resultEl.innerHTML = '<div class="info-box text-center">❌ Errore di connessione al server.</div>';
      running = false;
      if (btn) { btn.disabled = false; btn.innerHTML = '<span class="btn-icon">▶️</span><span class="btn-text">AVVIA SIMULAZIONE</span>'; }
      return;
    }

    const { giochi, totale, numPartite, bet } = data;
    const roiColor = totale.roi >= 0 ? 'var(--green)' : 'var(--red)';
    const gainColor = totale.gain >= 0 ? 'var(--green)' : 'var(--red)';

    function gameRow(g) {
      const gainCol = g.gain >= 0 ? 'var(--green)' : 'var(--red)';
      const gainStr = (g.gain >= 0 ? '+' : '') + formatCurrency(g.gain);
      const pariStr = g.pari !== undefined ? `<span>🤝 Pari: <strong>${g.pari}</strong></span>` : '';
      return `
        <div style="background:var(--bg-2);border:1px solid var(--border);border-radius:var(--radius);padding:1rem;margin-bottom:0.75rem">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:0.5rem">
            <strong>${g.nome}</strong>
            <span style="color:${gainCol};font-family:var(--font-mono);font-size:1.1rem">${gainStr}</span>
          </div>
          <div style="font-size:0.8rem;color:var(--text-2);margin-bottom:0.5rem">${g.strategia}</div>
          <div style="display:flex;gap:1.5rem;font-size:0.82rem;flex-wrap:wrap">
            <span>✅ Vinte: <strong style="color:var(--green)">${g.vinte}</strong></span>
            <span>❌ Perse: <strong style="color:var(--red)">${g.perse}</strong></span>
            ${pariStr}
            <span>📊 Win rate: <strong>${((g.vinte / numPartite) * 100).toFixed(1)}%</strong></span>
          </div>
        </div>`;
    }

    resultEl.innerHTML = `
      <div style="border:1px solid var(--border);border-radius:var(--radius);overflow:hidden">
        <div style="background:var(--bg-3);padding:0.75rem 1rem;font-family:var(--font-display);font-size:1.1rem;letter-spacing:0.05em">
          📊 RISULTATI — ${numPartite} partite · €${bet} per partita
        </div>
        <div style="padding:1rem">
          ${gameRow(giochi.blackjack)}
          ${gameRow(giochi.dadi)}
          ${gameRow(giochi.roulette)}

          <div style="background:var(--bg-3);border:1px solid var(--border);border-radius:var(--radius-sm);padding:1rem;margin-top:0.5rem">
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:1rem;text-align:center">
              <div>
                <div style="font-size:0.75rem;color:var(--text-3);margin-bottom:0.25rem">TOTALE INVESTITO</div>
                <div style="font-family:var(--font-mono);font-size:1rem">${formatCurrency(totale.investito)}</div>
              </div>
              <div>
                <div style="font-size:0.75rem;color:var(--text-3);margin-bottom:0.25rem">GUADAGNO/PERDITA</div>
                <div style="font-family:var(--font-mono);font-size:1rem;color:${gainColor}">
                  ${totale.gain >= 0 ? '+' : ''}${formatCurrency(totale.gain)}
                </div>
              </div>
              <div>
                <div style="font-size:0.75rem;color:var(--text-3);margin-bottom:0.25rem">ROI</div>
                <div style="font-family:var(--font-mono);font-size:1rem;color:${roiColor}">
                  ${totale.roi >= 0 ? '+' : ''}${totale.roi}%
                </div>
              </div>
            </div>
          </div>

          <div style="margin-top:1rem;padding:0.75rem;background:var(--accent-bg);border:1px solid var(--accent);border-radius:var(--radius-sm);font-size:0.82rem;color:var(--text-2)">
            ⚠️ <strong>Alla lunga il banco vince sempre.</strong>
            Gioca solo per divertimento e con limiti di spesa.
          </div>
        </div>
      </div>`;

    running = false;
    if (btn) { btn.disabled = false; btn.innerHTML = '<span class="btn-icon">🔄</span><span class="btn-text">RIESEGUI SIMULAZIONE</span>'; }
  }

  return { render, run };
})();
