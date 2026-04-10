// =============================================
// BetCenterNL — LIVE TICKER
// =============================================

const tickerEvents = [
  '⚽ Virtuale FC Roma 2-1 Milan Virtuale',
  '🎾 Djokovic Virtual vince set 6-4',
  '🐎 Tornado Express vince gara 3 a quota 4.20',
  '🏀 Lakers Virtual 98 — 94 Celtics Virtual',
  '🎰 JACKPOT! Un giocatore ha vinto €12.450 alle Slot',
  '⚽ Champions Virtual: Barcellona 1-0 Juventus (45\')',
  '🐕 Fulmine Nero primo al traguardo 7.50x',
  '🎾 Wimbledon Virtual: Murray avanza al secondo set',
  '🏀 NBA Virtual: Durant tripletta doppia',
  '⚽ Serie A Virtual: Napoli in testa alla classifica',
  '💎 Baccarat: Jackpot progressivo ora a €8.750',
  '🎲 Craps: Serie di 8 pass consecutivi registrata',
];

let tickerIndex = 0;

function initTicker() {
  const el = document.getElementById('ticker-text');
  if (!el) return;

  function updateTicker() {
    const span = document.createElement('span');
    span.textContent = tickerEvents.map(e => `   ⬥   ${e}`).join('');
    el.innerHTML = '';
    el.appendChild(span);
  }

  updateTicker();
  setInterval(() => updateTicker(), 35000);
}
