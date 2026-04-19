// =============================================
// BetCenterNL — UI UTILITIES
// =============================================

function updateBalanceDisplay() {
  const fmt = formatCurrency(State.balance);
  const els = document.querySelectorAll('#top-balance, #sidebar-balance');
  els.forEach(el => {
    if (el) el.textContent = fmt;
  });
}

function formatCurrency(amount) {
  return `€ ${Number(amount).toLocaleString('it-IT', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
}

function showToast(message, type = 'info', duration = 3000) {
  const container = document.getElementById('toast-container');
  const icons = { win: '✅', lose: '❌', info: 'ℹ️' };
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span class="toast-icon">${icons[type] || 'ℹ️'}</span><span>${message}</span>`;
  container.appendChild(toast);
  setTimeout(() => {
    toast.classList.add('fading');
    setTimeout(() => toast.remove(), 300);
  }, duration);
}

function toggleSidebar() {
  document.getElementById('sidebar').classList.toggle('open');
}

function showDeposit() {
  document.getElementById('deposit-modal').classList.remove('hidden');
}

function closeDeposit() {
  document.getElementById('deposit-modal').classList.add('hidden');
}

function setDepositAmount(amount) {
  document.getElementById('deposit-amount').value = amount;
}

async function processDeposit() {
  const amount = parseFloat(document.getElementById('deposit-amount').value);
  if (!amount || amount < 1) { showToast('Inserisci un importo valido', 'info'); return; }
  if (amount > 10000)        { showToast('Importo massimo: €10.000', 'info'); return; }

  try {
    const result = await API.deposit(amount);
    State.syncBalance(result.newBalance);
    closeDeposit();
    showToast(`💳 Deposito di ${formatCurrency(amount)} effettuato!`, 'win');
  } catch (err) {
    showToast('Errore nel deposito. Riprova.', 'lose');
  }
}

// Close sidebar on outside click (mobile)
document.addEventListener('click', (e) => {
  const sidebar = document.getElementById('sidebar');
  const toggle = document.querySelector('.menu-toggle');
  if (sidebar && !sidebar.contains(e.target) && toggle && !toggle.contains(e.target)) {
    sidebar.classList.remove('open');
  }
});

// Chip selection helper
function createBetControls(gameId, defaultBet = 10) {
  return `
    <div class="bet-controls">
      <div class="bet-chips">
        <button class="chip chip-1" onclick="setChip('${gameId}',1)">1</button>
        <button class="chip chip-5" onclick="setChip('${gameId}',5)">5</button>
        <button class="chip chip-10" onclick="setChip('${gameId}',10)">10</button>
        <button class="chip chip-25" onclick="setChip('${gameId}',25)">25</button>
        <button class="chip chip-50" onclick="setChip('${gameId}',50)">50</button>
        <button class="chip chip-100" onclick="setChip('${gameId}',100)">100</button>
      </div>
      <div class="bet-input-group">
        <button class="bet-adjust" onclick="adjustBet('${gameId}',-1)">−</button>
        <input type="number" id="${gameId}-bet" value="${defaultBet}" min="1" max="10000" />
        <button class="bet-adjust" onclick="adjustBet('${gameId}',1)">+</button>
      </div>
    </div>`;
}

function setChip(gameId, value) {
  const input = document.getElementById(`${gameId}-bet`);
  if (input) input.value = value;
}

function adjustBet(gameId, delta) {
  const input = document.getElementById(`${gameId}-bet`);
  if (!input) return;
  const val = parseInt(input.value) || 0;
  input.value = Math.max(1, Math.min(10000, val + delta));
}

function getBet(gameId) {
  const input = document.getElementById(`${gameId}-bet`);
  const val = parseFloat(input ? input.value : 0);
  return isNaN(val) ? 0 : Math.max(0, val);
}
