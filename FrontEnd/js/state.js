// =============================================
// BetCenterNL — STATE MANAGEMENT
// Saldo e storico sincronizzati con il backend
// =============================================

const State = {
  user:           null,
  balance:        0,
  history:        [],
  betSlip:        [],
  currentSection: 'lobby',

  // ── Saldo ──────────────────────────────────────────────────────────────────
  addBalance(amount) {
    this.balance = Math.max(0, parseFloat((this.balance + amount).toFixed(2)));
    updateBalanceDisplay();
  },

  deductBalance(amount) {
    if (this.balance < amount) return false;
    this.balance = parseFloat((this.balance - amount).toFixed(2));
    updateBalanceDisplay();
    return true;
  },

  /** Aggiorna il saldo locale con quello restituito dal server */
  syncBalance(newBalance) {
    if (typeof newBalance === 'number') {
      this.balance = parseFloat(newBalance.toFixed(2));
      updateBalanceDisplay();
    }
  },

  /** Aggiorna saldo E statistiche utente dal server */
  syncFromServer(newBalance) {
    if (typeof newBalance === 'number') {
      this.balance = parseFloat(newBalance.toFixed(2));
      updateBalanceDisplay();
    }
    // Aggiorna le stats utente in background
    API.getProfile().then(result => {
      if (result.ok && result.user) {
        this.user = { ...this.user, ...result.user };
      }
    }).catch(() => {});
  },

  // ── Storico locale (per la UI) ─────────────────────────────────────────────
  recordHistory(entry) {
    this.history.unshift({
      id:        Date.now(),
      timestamp: new Date().toLocaleString('it-IT'),
      ...entry
    });
    if (this.history.length > 200) this.history = this.history.slice(0, 200);
    // Aggiorna il portafoglio se è aperto
    if (typeof State !== 'undefined' && State.currentSection === 'wallet') {
      const ca = document.getElementById('content-area');
      if (ca && typeof Sections !== 'undefined') {
        // Aggiorna solo le stats senza ricaricare tutto
        try { Sections._refreshWalletStats(); } catch (_) {}
      }
    }
  },

  // ── Persistenza leggera (solo per sessione corrente) ──────────────────────
  save() {
    if (!this.user) return;
    try {
      localStorage.setItem(`betcenter_session_${this.user.id}`, JSON.stringify({
        balance: this.balance,
        history: this.history.slice(0, 50)
      }));
    } catch (_) {}
  },

  load() {
    if (!this.user) return;
    try {
      const raw = localStorage.getItem(`betcenter_session_${this.user.id}`);
      if (raw) {
        const data = JSON.parse(raw);
        // Il saldo viene sempre dal server — usiamo quello locale solo come fallback
        if (!this.balance) this.balance = data.balance ?? 1000;
        this.history = data.history ?? [];
      }
    } catch (_) {}
  }
};

// ── Compatibilità con il vecchio Users (non più usato) ────────────────────────
const Users = {
  getAll()          { return {}; },
  save()            {},
  register()        { return { ok: true }; },
  find()            { return null; }
};
