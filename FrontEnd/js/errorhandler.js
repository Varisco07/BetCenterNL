// =============================================
// BetCenterNL — GLOBAL ERROR HANDLER
// Previene crash totali dell'app
// =============================================

// ── GLOBAL UNCAUGHT ERROR ──
window.addEventListener('error', (e) => {
  console.error('[BetCenterNL] Errore non gestito:', e.message, e.filename, e.lineno);
  // Don't show toast for minor errors (font loading, etc.)
  if (e.filename && e.filename.includes('fonts.googleapis')) return;
  if (e.message && e.message.includes('Script error')) return;
  // Only show for our own JS errors
  if (e.filename && (e.filename.includes('betcenter') || e.filename.includes('localhost'))) {
    try {
      showToast('⚠️ Si è verificato un errore. Ricarica la pagina se necessario.', 'info', 4000);
    } catch(_) {}
  }
});

window.addEventListener('unhandledrejection', (e) => {
  console.warn('[BetCenterNL] Promise non gestita:', e.reason);
  e.preventDefault(); // Don't crash
});

// ── SAFE WRAPPER for game functions ──
function safeCall(fn, fallback = null) {
  try {
    return fn();
  } catch (e) {
    console.error('[BetCenterNL] safeCall error:', e);
    return fallback;
  }
}

// ── NULL-SAFE DOM helpers ──
function safeEl(id) { return document.getElementById(id); }
function safeSet(id, value) { const el = safeEl(id); if (el) el.textContent = value; }
function safeHTML(id, html) { const el = safeEl(id); if (el) el.innerHTML = html; }
function safeDisable(id, val) { const el = safeEl(id); if (el) el.disabled = val; }

// ── SAFE formatCurrency (in case State not loaded) ──
if (typeof formatCurrency !== 'function') {
  window.formatCurrency = (n) => `€ ${Number(n || 0).toLocaleString('it-IT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

// ── SAFE delay (already defined in slots.js, re-export if needed) ──
if (typeof delay !== 'function') {
  window.delay = (ms) => new Promise(r => setTimeout(r, ms));
}

// ── ANTI-FREEZE: reset stuck game states ──
let lastInteraction = Date.now();
document.addEventListener('click', () => { lastInteraction = Date.now(); });

setInterval(() => {
  // If page has been idle for 5+ minutes and a game is running, auto-save
  if (Date.now() - lastInteraction > 5 * 60 * 1000) {
    try { if (State.user) State.save(); } catch(_) {}
  }
}, 60 * 1000);

// ── PREVENT double-click spam on buttons ──
document.addEventListener('click', (e) => {
  const btn = e.target.closest('button.btn-game, button.btn-primary');
  if (!btn || btn.disabled) return;
  btn.style.pointerEvents = 'none';
  setTimeout(() => { btn.style.pointerEvents = ''; }, 300);
}, true);

// ── CONSOLE branding ──
console.log(
  '%c🎰 BetCenterNL %cv1.0.0 ',
  'background:#00d4ff;color:#000;font-weight:700;font-size:14px;padding:4px 8px;border-radius:4px 0 0 4px',
  'background:#1a1a22;color:#00d4ff;font-size:14px;padding:4px 8px;border-radius:0 4px 4px 0;border:1px solid #00d4ff'
);
console.log('%cCentro Scommesse Virtuale — 30 file · 7400+ righe', 'color:#74748a;font-size:11px');
