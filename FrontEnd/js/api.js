/**
 * BetCenterNL — API Client
 * Comunica con il backend Node.js su porta 3001
 */

const API = (() => {
  const BASE_URL = 'http://localhost:8080/api';
  let token = localStorage.getItem('betcenter_token') || null;

  function setToken(t) {
    token = t;
    if (t) localStorage.setItem('betcenter_token', t);
    else    localStorage.removeItem('betcenter_token');
  }

  function getToken() { return token; }

  async function request(method, endpoint, body = null) {
    const url = `${BASE_URL}${endpoint}`;
    const options = {
      method,
      headers: { 'Content-Type': 'application/json' }
    };
    if (token) options.headers['Authorization'] = `Bearer ${token}`;
    if (body)  options.body = JSON.stringify(body);

    try {
      const response = await fetch(url, options);
      let data;
      try {
        data = await response.json();
      } catch (parseErr) {
        // Risposta non-JSON dal server
        throw new Error(`Server error ${response.status}`);
      }

      if (!response.ok) {
        throw new Error(data.error || `HTTP ${response.status}`);
      }

      return data;
    } catch (e) {
      console.error(`[API] ${method} ${endpoint}:`, e.message);
      throw e;
    }
  }

  // ── AUTH ──────────────────────────────────────────────────────────────────
  async function register(userData) {
    const result = await request('POST', '/auth/register', userData);
    if (result.token) setToken(result.token);
    return result;
  }

  async function login(email, password) {
    const result = await request('POST', '/auth/login', { email, password });
    if (result.token) setToken(result.token);
    return result;
  }

  async function verify() {
    return request('GET', '/auth/verify');
  }

  // ── USER ──────────────────────────────────────────────────────────────────
  async function getProfile() {
    return request('GET', '/user/profile');
  }

  async function updateProfile(updates) {
    return request('PUT', '/user/profile', updates);
  }

  // ── WALLET ────────────────────────────────────────────────────────────────
  async function getBalance() {
    return request('GET', '/wallet/balance');
  }

  async function deposit(amount) {
    return request('POST', '/wallet/deposit', { amount });
  }

  async function claimDailyBonus() {
    return request('POST', '/wallet/daily-bonus');
  }

  // ── GAMES ─────────────────────────────────────────────────────────────────
  async function spinSlots(bet) {
    return request('POST', '/games/slots/spin', { bet });
  }

  async function dealBlackjack(bet) {
    return request('POST', '/games/blackjack/deal', { bet });
  }

  async function resolveBlackjack(bet, playerHand, dealerHand, isBlackjack = false) {
    return request('POST', '/games/blackjack/resolve', { bet, playerHand, dealerHand, isBlackjack });
  }

  // bets = array di { type, value, amount }
  async function spinRoulette(bets) {
    return request('POST', '/games/roulette/spin', { bets });
  }

  async function rollDadi(bet, betType) {
    return request('POST', '/games/dadi/roll', { bet, betType });
  }

  async function playBaccarat(bet, betType) {
    return request('POST', '/games/baccarat/play', { bet, betType });
  }

  async function dealPoker(bet) {
    return request('POST', '/games/poker/deal', { bet });
  }

  async function drawPoker(bet, hand, held) {
    return request('POST', '/games/poker/draw', { bet, hand, held });
  }

  async function chickenMove(bet, level, position) {
    return request('POST', '/games/chicken/move', { bet, level, position });
  }

  async function chickenCashout(bet, multiplier) {
    return request('POST', '/games/chicken/cashout', { bet, multiplier });
  }

  async function chickenGameover(bet) {
    return request('POST', '/games/chicken/gameover', { bet });
  }

  async function generateVirtualMatch(sport) {
    return request('GET', `/games/virtual/${sport}/generate`);
  }

  async function betVirtualMatch(sport, bet, prediction, matchId) {
    return request('POST', `/games/virtual/${sport}/bet`, { bet, prediction, matchId });
  }

  async function playVirtualSport(sport, bet, prediction) {
    return request('POST', `/games/virtual/${sport}/match`, { bet, prediction });
  }

  async function raceResult(sport, bet, won, winAmount) {
    return request('POST', '/games/virtual/race/result', { sport, bet, won, winAmount });
  }

  async function raceSimulate(sport, bet, pickedIdx, raceId) {
    return request('POST', '/games/virtual/race/simulate', { sport, bet, pickedIdx, raceId });
  }

  async function racePreview(sport) {
    return request('GET', `/games/virtual/race/preview?sport=${sport}`);
  }

  async function getGameHistory(limit = 100) {
    return request('GET', `/games/history?limit=${limit}`);
  }

  // ── LEADERBOARD ───────────────────────────────────────────────────────────
  async function getLeaderboard() {
    return request('GET', '/leaderboard');
  }

  async function getUserRank(userId) {
    return request('GET', `/leaderboard/rank/${userId}`);
  }

  // ── ACHIEVEMENTS ──────────────────────────────────────────────────────────
  async function getAchievements() {
    return request('GET', '/achievements');
  }

  // ── LIVE FEED ─────────────────────────────────────────────────────────────
  async function getLiveFeed() {
    return request('GET', '/livefeed');
  }

  async function addLiveFeedActivity(username, game, amount) {
    return request('POST', '/livefeed/add', { username, game, amount });
  }

  // ── CHAT ──────────────────────────────────────────────────────────────────
  async function getChatMessages(limit = 50) {
    return request('GET', `/chat/messages?limit=${limit}`);
  }

  async function sendChatMessage(text) {
    return request('POST', '/chat/send', { text });
  }

  async function deleteChatMessage(messageId) {
    return request('DELETE', `/chat/messages/${messageId}`);
  }

  // ── JACKPOT ───────────────────────────────────────────────────────────────
  async function getJackpot() {
    return request('GET', '/jackpot');
  }

  // ── SIMULAZIONE ───────────────────────────────────────────────────────────
  async function runSimulation() {
    return request('GET', '/simulation/run');
  }

  return {
    setToken, getToken,
    register, login, verify,
    getProfile, updateProfile,
    getBalance, deposit, claimDailyBonus,
    spinSlots,
    dealBlackjack, resolveBlackjack,
    spinRoulette,
    rollDadi,
    playBaccarat,
    dealPoker, drawPoker,
    chickenMove, chickenCashout, chickenGameover,
    playVirtualSport,
    getGameHistory,
    getLeaderboard, getUserRank,
    getAchievements,
    getLiveFeed, addLiveFeedActivity,
    getChatMessages, sendChatMessage, deleteChatMessage,
    getJackpot,
    generateVirtualMatch, betVirtualMatch,
    playVirtualSport, raceResult, raceSimulate,
    racePreview,
    runSimulation
  };
})();
