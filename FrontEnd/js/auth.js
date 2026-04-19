// =============================================
// BetCenterNL — AUTH MODULE (collegato al backend)
// =============================================

function setupAuthTabs() {
  document.querySelectorAll('.auth-tab').forEach(tab => {
    tab.addEventListener('click', () => {
      document.querySelectorAll('.auth-tab').forEach(t => t.classList.remove('active'));
      document.querySelectorAll('.auth-form').forEach(f => f.classList.remove('active'));
      tab.classList.add('active');
      document.getElementById(`${tab.dataset.tab}-form`).classList.add('active');
      setAuthError('');
    });
  });
}

function setAuthError(msg) {
  const el = document.getElementById('auth-error');
  if (el) el.textContent = msg;
}

function setAuthLoading(loading) {
  document.querySelectorAll('.auth-form button[type="submit"], .auth-form .btn-primary')
    .forEach(btn => { btn.disabled = loading; });
}

// ── LOGIN ─────────────────────────────────────────────────────────────────────
async function handleLogin(e) {
  e.preventDefault();
  const email    = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;

  if (!email || !password) { setAuthError('Inserisci email e password'); return; }

  setAuthLoading(true);
  setAuthError('');

  try {
    const result = await API.login(email, password);
    loginUser(result.user);
  } catch (err) {
    setAuthError(err.message === 'Invalid credentials'
      ? 'Email o password non corretti'
      : 'Errore di connessione al server. Assicurati che il backend sia avviato.');
  } finally {
    setAuthLoading(false);
  }
}

// ── REGISTRAZIONE ─────────────────────────────────────────────────────────────
async function handleSignup(e) {
  e.preventDefault();
  const nome     = document.getElementById('signup-nome').value.trim();
  const cognome  = document.getElementById('signup-cognome').value.trim();
  const username = document.getElementById('signup-username').value.trim();
  const email    = document.getElementById('signup-email').value.trim();
  const password = document.getElementById('signup-password').value;
  const dob      = document.getElementById('signup-dob').value;

  if (dob) {
    const age = (new Date() - new Date(dob)) / (365.25 * 24 * 60 * 60 * 1000);
    if (age < 18) { setAuthError('Devi avere almeno 18 anni per registrarti'); return; }
  }

  setAuthLoading(true);
  setAuthError('');

  try {
    const result = await API.register({ nome, cognome, username, email, password, dob });
    loginUser(result.user, true);
    showToast('🎉 Benvenuto! Hai ricevuto €1.000 di bonus di benvenuto!', 'win', 4000);
  } catch (err) {
    setAuthError(err.message.includes('already')
      ? 'Email già registrata'
      : 'Errore di connessione al server. Assicurati che il backend sia avviato.');
  } finally {
    setAuthLoading(false);
  }
}

// ── DEMO LOGIN ────────────────────────────────────────────────────────────────
async function demoLogin() {
  setAuthLoading(true);
  setAuthError('');
  try {
    // Prova login demo, se non esiste lo crea
    let result;
    try {
      result = await API.login('demo@betcenter.nl', 'demo123');
    } catch {
      result = await API.register({
        nome: 'Demo', cognome: 'Player', username: 'demo',
        email: 'demo@betcenter.nl', password: 'demo123', dob: '1990-01-01'
      });
    }
    loginUser(result.user, true);
    showToast('🎮 Modalità demo — Saldo: €1.000', 'info');
  } catch (err) {
    setAuthError('Errore di connessione. Assicurati che il backend sia avviato (porta 3001).');
  } finally {
    setAuthLoading(false);
  }
}

// ── LOGIN UTENTE ──────────────────────────────────────────────────────────────
function loginUser(user, isNew = false) {
  State.user    = user;
  State.balance = user.balance ?? 1000;
  State.history = [];

  // Carica storico dal server in background
  API.getGameHistory(100).then(result => {
    if (!result.ok || !result.history) return;
    State.history = result.history.map(h => ({
      id:        h.id,
      timestamp: new Date(h.timestamp).toLocaleString('it-IT'),
      game:      h.game,
      bet:       h.bet,
      result:    h.result,
      gain:      h.gain
    }));
  }).catch(() => {});

  // Aggiorna UI
  const nameEl   = document.getElementById('user-display-name');
  const avatarEl = document.getElementById('user-avatar');
  if (nameEl)   nameEl.textContent   = user.nome || user.username;
  if (avatarEl) avatarEl.textContent = (user.nome || user.username || '?')[0].toUpperCase();
  updateBalanceDisplay();

  // Cambia vista
  document.getElementById('auth-overlay').classList.add('hidden');
  document.getElementById('app').classList.remove('hidden');
  document.body.classList.remove('auth-page');

  initApp();
  try { AudioEngine.play('win'); } catch (_) {}

  if (!isNew) showToast(`👋 Bentornato, ${user.nome || user.username}!`, 'info');
}

// ── LOGOUT ────────────────────────────────────────────────────────────────────
function logout() {
  API.setToken(null);
  State.user    = null;
  State.balance = 0;
  State.betSlip = [];
  document.getElementById('app').classList.add('hidden');
  document.getElementById('auth-overlay').classList.remove('hidden');
  document.body.classList.add('auth-page');
}

// ── AUTO-LOGIN se token valido ────────────────────────────────────────────────
async function tryAutoLogin() {
  if (!API.getToken()) return false;
  try {
    const result = await API.verify();
    if (result.ok && result.user) {
      loginUser(result.user);
      return true;
    }
  } catch (err) {
    // 401 = token scaduto/invalido, non è un errore di connessione
    if (err.message && (err.message.includes('401') || err.message.includes('Unauthorized'))) {
      API.setToken(null);
    }
  }
  return false;
}

setupAuthTabs();
