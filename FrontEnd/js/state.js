// =============================================
// BetCenterNL — STATE MANAGEMENT
// =============================================

const State = {
  user: null,
  balance: 0,
  history: [],
  betSlip: [],
  currentSection: 'lobby',

  // Persist to localStorage
  save() {
    if (!this.user) return;
    const data = {
      balance: this.balance,
      history: this.history.slice(-100),
    };
    localStorage.setItem(`betcenter_${this.user.username}`, JSON.stringify(data));
  },

  load() {
    if (!this.user) return;
    const raw = localStorage.getItem(`betcenter_${this.user.username}`);
    if (raw) {
      try {
        const data = JSON.parse(raw);
        this.balance = data.balance ?? 1000;
        this.history = data.history ?? [];
      } catch {}
    }
  },

  addBalance(amount) {
    this.balance = Math.max(0, parseFloat((this.balance + amount).toFixed(2)));
    this.save();
    updateBalanceDisplay();
  },

  deductBalance(amount) {
    if (this.balance < amount) return false;
    this.balance = parseFloat((this.balance - amount).toFixed(2));
    this.save();
    updateBalanceDisplay();
    return true;
  },

  recordHistory(entry) {
    this.history.unshift({
      id: Date.now(),
      timestamp: new Date().toLocaleString('it-IT'),
      ...entry
    });
    if (this.history.length > 200) this.history = this.history.slice(0, 200);
    this.save();
  }
};

// User storage
const Users = {
  getAll() {
    const raw = localStorage.getItem('betcenter_users');
    return raw ? JSON.parse(raw) : {};
  },
  save(users) {
    localStorage.setItem('betcenter_users', JSON.stringify(users));
  },
  register(userData) {
    const users = this.getAll();
    if (users[userData.email]) return { ok: false, error: 'Email già registrata' };
    users[userData.email] = userData;
    this.save(users);
    return { ok: true };
  },
  find(email) {
    return this.getAll()[email] || null;
  }
};
