// =============================================
// BetCenterNL — AUTH MODULE
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
  document.getElementById('auth-error').textContent = msg;
}

function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value;

  const user = Users.find(email);
  if (!user || user.password !== password) {
    setAuthError('Email o password non corretti');
    return;
  }

  loginUser(user);
}

function handleSignup(e) {
  e.preventDefault();
  const nome = document.getElementById('signup-nome').value.trim();
  const cognome = document.getElementById('signup-cognome').value.trim();
  const username = document.getElementById('signup-username').value.trim();
  const email = document.getElementById('signup-email').value.trim();
  const password = document.getElementById('signup-password').value;
  const dob = document.getElementById('signup-dob').value;

  // Age check
  if (dob) {
    const age = (new Date() - new Date(dob)) / (365.25 * 24 * 60 * 60 * 1000);
    if (age < 18) {
      setAuthError('Devi avere almeno 18 anni per registrarti');
      return;
    }
  }

  const result = Users.register({ nome, cognome, username, email, password, dob });
  if (!result.ok) {
    setAuthError(result.error);
    return;
  }

  const user = Users.find(email);
  // Give welcome bonus
  State.user = user;
  State.balance = 1000;
  State.history = [];
  State.save();
  loginUser(user);
  showToast('🎉 Benvenuto! Hai ricevuto €1.000 di bonus di benvenuto!', 'win', 4000);
}

function demoLogin() {
  const demoUser = {
    nome: 'Demo',
    cognome: 'Player',
    username: 'demo',
    email: 'demo@betcenter.nl',
    password: 'demo123'
  };
  loginUser(demoUser, true);
}

function loginUser(user, isDemo = false) {
  State.user = user;
  State.load();
  if (State.balance === 0 || isDemo) State.balance = 1000;

  // Update UI
  document.getElementById('user-display-name').textContent = user.nome || user.username;
  document.getElementById('user-avatar').textContent = (user.nome || user.username || '?')[0].toUpperCase();
  updateBalanceDisplay();

  // Switch views
  document.getElementById('auth-overlay').classList.add('hidden');
  document.getElementById('app').classList.remove('hidden');
  document.body.classList.remove('auth-page');

  // Init app
  initApp();
  AudioEngine.play('win');

  if (!isDemo) {
    showToast(`👋 Bentornato, ${user.nome}!`, 'info');
  } else {
    showToast('🎮 Modalità demo — Saldo: €1.000', 'info');
  }
}

function logout() {
  State.save();
  State.user = null;
  State.betSlip = [];
  document.getElementById('app').classList.add('hidden');
  document.getElementById('auth-overlay').classList.remove('hidden');
  document.body.classList.add('auth-page');
}

setupAuthTabs();
