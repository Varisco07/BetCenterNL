/**
 * BetCenterNL — API Client
 * Comunica con il backend Node.js
 */

const API = (() => {
  const BASE_URL = 'http://localhost:8080/api';
  let token = localStorage.getItem('betcenter_token') || null;

  function setToken(t) {
    token = t;
    if (t) {
      localStorage.setItem('betcenter_token', t);
    } else {
      localStorage.removeItem('betcenter_token');
    }
  }

  function getToken() {
    return token;
  }

  async function request(method, endpoint, body = null) {
    const url = `${BASE_URL}${endpoint}`;
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json'
      }
    };

    if (token) {
      options.headers['Authorization'] = `Bearer ${token}`;
    }

    if (body) {
      options.body = JSON.stringify(body);
    }

    try {
      const response = await fetch(url, options);
      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.error || 'API Error');
      }

      return data;
    } catch (e) {
      console.error(`[API] ${method} ${endpoint}:`, e.message);
      throw e;
    }
  }

  // ── AUTH ──
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

  // ── USER ──
  async function getProfile() {
    return request('GET', '/user/profile');
  }

  async function updateProfile(updates) {
    return request('PUT', '/user/profile', updates);
  }

  // ── GAMES ──
  async function spinSlots(bet) {
    return request('POST', '/games/slots/spin', { bet });
  }

  async function dealBlackjack(bet) {
    return request('POST', '/games/blackjack/deal', { bet });
  }

  async function spinRoulette(bet, betType, betValue) {
    return request('POST', '/games/roulette/spin', { bet, betType, betValue });
  }

  async function rollDadi(bet) {
    return request('POST', '/games/dadi/roll', { bet });
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

  async function playVirtualSport(sport, bet, prediction) {
    return request('POST', `/games/virtual/${sport}/match`, { bet, prediction });
  }

  async function getGameHistory(limit = 100) {
    return request('GET', `/games/history?limit=${limit}`);
  }

  // ── ACHIEVEMENTS ──
  async function getAchievements() {
    return request('GET', '/achievements');
  }

  // ── LIVE FEED ──
  async function getLiveFeed() {
    return request('GET', '/livefeed');
  }

  async function addLiveFeedActivity(username, game, amount) {
    return request('POST', '/livefeed/add', { username, game, amount });
  }

  // ── CHAT ──
  async function getChatMessages(limit = 50) {
    return request('GET', `/chat/messages?limit=${limit}`);
  }

  async function sendChatMessage(text) {
    return request('POST', '/chat/send', { text });
  }

  async function deleteChatMessage(messageId) {
    return request('DELETE', `/chat/messages/${messageId}`);
  }

  // ── WALLET ──
  async function getBalance() {
    return request('GET', '/wallet/balance');
  }

  async function deposit(amount) {
    return request('POST', '/wallet/deposit', { amount });
  }

  async function claimDailyBonus() {
    return request('POST', '/wallet/daily-bonus');
  }

  // ── LEADERBOARD ──
  async function getLeaderboard() {
    return request('GET', '/leaderboard');
  }

  async function getUserRank(userId) {
    return request('GET', `/leaderboard/rank/${userId}`);
  }

  return {
    setToken,
    getToken,
    register,
    login,
    verify,
    getProfile,
    updateProfile,
    spinSlots,
    dealBlackjack,
    spinRoulette,
    rollDadi,
    playBaccarat,
    dealPoker,
    drawPoker,
    playVirtualSport,
    getGameHistory,
    getBalance,
    deposit,
    claimDailyBonus,
    getLeaderboard,
    getUserRank,
    getAchievements,
    getLiveFeed,
    addLiveFeedActivity,
    getChatMessages,
    sendChatMessage,
    deleteChatMessage
  };
})();
