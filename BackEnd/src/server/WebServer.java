package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import core.*;
import games.slot.SlotMachine;
import games.BlackJack.modello.MazzoCarte;
import games.BlackJack.modello.Carta;
import games.roulette.ruotaRoulette;
import games.baccarat.Baccarat;
import games.poker.VideoPoker;
import games.chicken.ChickenGame;
import games.virtual.VirtualCalcio;
import games.virtual.Competizioni;
import games.virtual.Quote;
import games.virtual.ForzaSquadra;
import games.virtual.Eventi;
import games.virtualCavalli.VirtualCavalli;
import games.virtualCavalli.ForzaCavalli;
import games.virtualCavalli.EventoCorsa;
import games.virtualCani.VirtualCani;
import games.virtualCani.ForzaCani;
import games.virtualCani.EventoCani;

public class WebServer {
    private static final int PORT = 8080;

    // In-memory store for pending virtual matches
    private static final Map<String, Object[]> pendingMatches = new HashMap<>();

    public static void main(String[] args) throws IOException {
        startServer();
    }

    public static void startServer() throws IOException {
        // Forza il locale US per evitare virgola decimale italiana nel JSON
        java.util.Locale.setDefault(java.util.Locale.US);
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

        // Health
        server.createContext("/api/health",                    new HealthHandler());
        server.createContext("/api/jackpot",                   new JackpotHandler());

        // Auth
        server.createContext("/api/auth/register",             new RegisterHandler());
        server.createContext("/api/auth/login",                new LoginHandler());
        server.createContext("/api/auth/verify",               new VerifyHandler());

        // User
        server.createContext("/api/user/profile",              new ProfileHandler());

        // Wallet
        server.createContext("/api/wallet/balance",            new BalanceHandler());
        server.createContext("/api/wallet/deposit",            new DepositHandler());
        server.createContext("/api/wallet/daily-bonus",        new DailyBonusHandler());

        // Games
        server.createContext("/api/games/slots/spin",          new SlotsHandler());
        server.createContext("/api/games/blackjack/deal",      new BlackjackDealHandler());
        server.createContext("/api/games/blackjack/resolve",   new BlackjackResolveHandler());
        server.createContext("/api/games/roulette/spin",       new RouletteHandler());
        server.createContext("/api/games/dadi/roll",           new DadiHandler());
        server.createContext("/api/games/baccarat/play",       new BaccaratHandler());
        server.createContext("/api/games/poker/deal",          new PokerDealHandler());
        server.createContext("/api/games/poker/draw",          new PokerDrawHandler());
        server.createContext("/api/games/chicken/move",        new ChickenMoveHandler());
        server.createContext("/api/games/chicken/cashout",     new ChickenCashoutHandler());
        server.createContext("/api/games/chicken/gameover",    new ChickenGameoverHandler());
        server.createContext("/api/games/virtual/race/simulate", new VirtualRaceSimulateHandler());
        server.createContext("/api/games/virtual/race/preview",  new VirtualRacePreviewHandler());
        server.createContext("/api/games/virtual/",              new VirtualSportHandler());

        // History & Leaderboard & Simulation
        server.createContext("/api/games/history",             new HistoryHandler());
        server.createContext("/api/leaderboard",               new LeaderboardHandler());
        server.createContext("/api/simulation/run",            new SimulationHandler());

        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║   BetCenterNL — Java Backend Server    ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║  🚀  http://localhost:" + PORT + "              ║");
        System.out.println("║  ⌨️   Ctrl+C per fermare               ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITIES
    // ─────────────────────────────────────────────────────────────────────────

    private static void setCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type,Authorization");
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    }

    private static boolean handleOptions(HttpExchange ex) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
            setCors(ex);
            ex.sendResponseHeaders(204, -1);
            return true;
        }
        return false;
    }

    private static String readBody(HttpExchange ex) throws IOException {
        try (InputStream is = ex.getRequestBody();
             ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
            byte[] tmp = new byte[4096];
            int n;
            while ((n = is.read(tmp)) != -1) buf.write(tmp, 0, n);
            return buf.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static void send(HttpExchange ex, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void ok(HttpExchange ex, String json) throws IOException {
        send(ex, 200, json);
    }

    private static void err(HttpExchange ex, int status, String msg) throws IOException {
        send(ex, status, "{\"error\":\"" + esc(msg) + "\"}");
    }

    /** Escape a string for embedding in a JSON string literal. */
    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /** Very simple JSON string-value extractor. Works for flat objects. */
    private static String jsonStr(String json, String key) {
        String search = "\"" + key + "\"";
        int ki = json.indexOf(search);
        if (ki < 0) return null;
        int colon = json.indexOf(':', ki + search.length());
        if (colon < 0) return null;
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\t')) start++;
        if (start >= json.length()) return null;
        if (json.charAt(start) == '"') {
            // string value
            int end = start + 1;
            while (end < json.length()) {
                if (json.charAt(end) == '"' && json.charAt(end - 1) != '\\') break;
                end++;
            }
            return json.substring(start + 1, end);
        } else {
            // number / bool / null
            int end = start;
            while (end < json.length() && ",}]\n\r ".indexOf(json.charAt(end)) < 0) end++;
            return json.substring(start, end).trim();
        }
    }

    private static double jsonDouble(String json, String key, double def) {
        String v = jsonStr(json, key);
        if (v == null) return def;
        try { return Double.parseDouble(v); } catch (Exception e) { return def; }
    }

    private static int jsonInt(String json, String key, int def) {
        String v = jsonStr(json, key);
        if (v == null) return def;
        try { return Integer.parseInt(v); } catch (Exception e) { return def; }
    }

    private static boolean jsonBool(String json, String key, boolean def) {
        String v = jsonStr(json, key);
        if (v == null) return def;
        return "true".equalsIgnoreCase(v.trim());
    }

    /** Extract the Authorization header and return the user, or null. */
    private static User authUser(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth == null) return null;
        auth = auth.trim();
        if (auth.startsWith("Bearer ")) auth = auth.substring(7).trim();
        if (!auth.startsWith("betcenter-")) return null;
        String id = auth.replace("betcenter-", "");
        return Database.getUserById(id);
    }

    private static String userJson(User u) {
        return String.format(java.util.Locale.US,
            "{\"id\":\"%s\",\"email\":\"%s\",\"username\":\"%s\",\"nome\":\"%s\",\"cognome\":\"%s\",\"balance\":%.2f,\"xp\":%d,\"level\":%d,\"gamesPlayed\":%d,\"gamesWon\":%d,\"gamesLost\":%d,\"totalGain\":%.2f,\"winRate\":%.2f}",
            esc(u.getId()), esc(u.getEmail()), esc(u.getUsername()), esc(u.getNome()), esc(u.getCognome()),
            u.getSaldo(), u.getXp(), u.getCurrentLevel(),
            u.getGiociGiocati(), u.getGiociVinti(), u.getGiociPersi(), u.getGuadagnoTotale(), u.getWinRate()
        );
    }

    private static String cardJson(String rank, String suit) {
        return "{\"rank\":\"" + esc(rank) + "\",\"suit\":\"" + esc(suit) + "\"}";
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HEALTH
    // ─────────────────────────────────────────────────────────────────────────
    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            ok(ex, "{\"ok\":true}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JACKPOT
    // ─────────────────────────────────────────────────────────────────────────
    static class JackpotHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            ok(ex, String.format("{\"ok\":true,\"jackpot\":%.2f}", State.getJackpot()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH — REGISTER
    // ─────────────────────────────────────────────────────────────────────────
    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            Database.reload(); // Sincronizza con eventuali utenti creati da terminale
            String body = readBody(ex);
            String nome     = jsonStr(body, "nome");
            String cognome  = jsonStr(body, "cognome");
            String username = jsonStr(body, "username");
            String email    = jsonStr(body, "email");
            String password = jsonStr(body, "password");
            String dob      = jsonStr(body, "dob");
            if (nome == null || cognome == null || username == null || email == null || password == null) {
                err(ex, 400, "Missing required fields"); return;
            }
            if (email.trim().isEmpty() || !email.contains("@")) { err(ex, 400, "Invalid email"); return; }
            if (password.length() < 6) { err(ex, 400, "Password too short"); return; }
            if (Database.userExists(email)) { err(ex, 409, "Email already registered"); return; }
            User user = new User(nome, cognome, username, email, password, dob != null ? dob : "");
            Database.registerUser(user);
            String token = "betcenter-" + user.getId();
            ok(ex, "{\"ok\":true,\"user\":" + userJson(user) + ",\"token\":\"" + token + "\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH — LOGIN
    // ─────────────────────────────────────────────────────────────────────────
    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            Database.reload(); // Sincronizza con eventuali utenti creati da terminale
            String body = readBody(ex);
            String email    = jsonStr(body, "email");
            String password = jsonStr(body, "password");
            if (email == null || password == null) { err(ex, 400, "Missing credentials"); return; }
            User user = Database.getUserByEmail(email);
            if (user == null || !user.getPassword().equals(password)) {
                err(ex, 401, "Invalid credentials"); return;
            }
            String token = "betcenter-" + user.getId();
            ok(ex, "{\"ok\":true,\"user\":" + userJson(user) + ",\"token\":\"" + token + "\"}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AUTH — VERIFY
    // ─────────────────────────────────────────────────────────────────────────
    static class VerifyHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            ok(ex, "{\"ok\":true,\"user\":" + userJson(user) + "}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // USER — PROFILE
    // ─────────────────────────────────────────────────────────────────────────
    static class ProfileHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            ok(ex, "{\"ok\":true,\"user\":" + userJson(user) + "}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WALLET — BALANCE
    // ─────────────────────────────────────────────────────────────────────────
    static class BalanceHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            ok(ex, String.format("{\"ok\":true,\"balance\":%.2f}", user.getSaldo()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WALLET — DEPOSIT
    // ─────────────────────────────────────────────────────────────────────────
    static class DepositHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double amount = jsonDouble(body, "amount", 0);
            if (amount < 1 || amount > 100000) { err(ex, 400, "Invalid amount"); return; }
            user.setSaldo(user.getSaldo() + amount);
            Database.saveUsers();
            ok(ex, String.format("{\"ok\":true,\"amount\":%.2f,\"newBalance\":%.2f}", amount, user.getSaldo()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // WALLET — DAILY BONUS
    // ─────────────────────────────────────────────────────────────────────────
    static class DailyBonusHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            LocalDateTime lastBonus = user.getLastBonusDate();
            LocalDate today = LocalDate.now();
            if (lastBonus != null && lastBonus.toLocalDate().equals(today)) {
                err(ex, 400, "Bonus already claimed today"); return;
            }
            int streak = user.getBonusStreak();
            double bonusAmount = Math.min(50 + streak * 10, 500);
            user.setSaldo(user.getSaldo() + bonusAmount);
            user.setLastBonusDate(LocalDateTime.now());
            user.setBonusStreak(streak + 1);
            Database.saveUsers();
            ok(ex, String.format("{\"ok\":true,\"bonusAmount\":%.2f,\"newBalance\":%.2f,\"streak\":%d}",
                bonusAmount, user.getSaldo(), streak + 1));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — SLOTS
    // ─────────────────────────────────────────────────────────────────────────
    static class SlotsHandler implements HttpHandler {
        private static final String[] SYMBOLS = {"cherry","lemon","orange","grape","star","diamond","seven","bell"};
        private static final String[] EMOJIS  = {"🍒","🍋","🍊","🍇","⭐","💎","7️⃣","🔔"};
        private static final int[]    WEIGHTS = {20,18,16,14,10,8,6,4};
        private static final double[] PAYOUTS = {5,8,10,15,25,50,100,200};
        private static final Random rand = new Random();

        private int weightedIdx() {
            int total = 0;
            for (int w : WEIGHTS) total += w;
            int r = rand.nextInt(total);
            for (int i = 0; i < WEIGHTS.length; i++) {
                r -= WEIGHTS[i];
                if (r < 0) return i;
            }
            return 0;
        }

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            // Use State + SlotMachine for the actual spin logic
            State.setBalance(user.getSaldo());
            // Spin manually to capture result (SlotMachine uses System.out and sleep)
            int i0 = weightedIdx(), i1 = weightedIdx(), i2 = weightedIdx();
            String s0 = EMOJIS[i0], s1 = EMOJIS[i1], s2 = EMOJIS[i2];

            double multiplier = 0;
            if (i0 == i1 && i1 == i2) {
                multiplier = PAYOUTS[i0];
            } else if (i0 == 0 && i1 == 0) { // two cherries left
                multiplier = 1.5;
            } else if (i1 == 0 && i2 == 0) { // two cherries right
                multiplier = 1.5;
            }

            boolean win = multiplier > 0;
            double gain = win ? round2(bet * multiplier - bet) : -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();

            GameRecord rec = new GameRecord("Slot Machine", bet, gain, win);
            Database.recordGameResult(user.getId(), rec);

            // Aggiorna jackpot sul server
            boolean jackpotWon = false;
            if (multiplier == 200) {
                // Jackpot vinto! Resetta
                jackpotWon = true;
                State.resetJackpot();
            } else {
                // Ogni spin contribuisce al jackpot
                State.addToJackpot(bet);
            }
            double jackpot = State.getJackpot();

            ok(ex, String.format(
                "{\"ok\":true,\"reels\":[\"%s\",\"%s\",\"%s\"],\"win\":%b,\"multiplier\":%.2f,\"gain\":%.2f,\"newBalance\":%.2f,\"jackpot\":%.2f,\"jackpotWon\":%b}",
                esc(s0), esc(s1), esc(s2), win, multiplier, gain, newBalance, jackpot, jackpotWon));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — BLACKJACK DEAL
    // ─────────────────────────────────────────────────────────────────────────
    static class BlackjackDealHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            MazzoCarte mazzo = new MazzoCarte();
            Carta p1 = mazzo.pescaCarta();
            Carta p2 = mazzo.pescaCarta();
            Carta d1 = mazzo.pescaCarta();
            Carta d2 = mazzo.pescaCarta();

            int pv = bjValue(p1) + bjValue(p2);
            // Adjust for aces
            if (p1.isAsso() && pv > 21) pv -= 10;
            if (p2.isAsso() && pv > 21) pv -= 10;

            String gameId = String.valueOf(System.currentTimeMillis());

            String playerHand = "[" + cardJson(p1.getNome(), semeToSuit(p1.getSeme())) + "," + cardJson(p2.getNome(), semeToSuit(p2.getSeme())) + "]";
            String dealerVisible = "[" + cardJson(d1.getNome(), semeToSuit(d1.getSeme())) + ",{\"rank\":\"?\",\"suit\":\"?\"}]";
            String dealerFull = "[" + cardJson(d1.getNome(), semeToSuit(d1.getSeme())) + "," + cardJson(d2.getNome(), semeToSuit(d2.getSeme())) + "]";

            ok(ex, String.format(
                "{\"ok\":true,\"playerHand\":%s,\"dealerHand\":%s,\"dealerHandFull\":%s,\"playerValue\":%d,\"gameId\":\"%s\"}",
                playerHand, dealerVisible, dealerFull, pv, gameId));
        }

        private int bjValue(Carta c) {
            String n = c.getNome();
            if (n.equals("Jack") || n.equals("Queen") || n.equals("King")) return 10;
            if (n.equals("Asso")) return 11;
            try { return Integer.parseInt(n); } catch (Exception e) { return 0; }
        }

        private String semeToSuit(String seme) {
            switch (seme) {
                case "Cuori":  return "♥";
                case "Quadri": return "♦";
                case "Fiori":  return "♣";
                case "Picche": return "♠";
                default:       return seme;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — BLACKJACK RESOLVE
    // ─────────────────────────────────────────────────────────────────────────
    static class BlackjackResolveHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            int pv = jsonInt(body, "playerValue", 0);
            int dv = jsonInt(body, "dealerValue", 0);
            boolean isBlackjack = jsonBool(body, "isBlackjack", false);

            // Recalculate dealer value from dealerHand if provided
            // (client sends final dealer hand after dealer draws)
            String dealerHandRaw = extractArray(body, "dealerHand");
            if (dealerHandRaw != null) {
                dv = calcHandValueFromJson(dealerHandRaw);
            }
            String playerHandRaw = extractArray(body, "playerHand");
            if (playerHandRaw != null) {
                pv = calcHandValueFromJson(playerHandRaw);
            }

            double gain;
            String result;
            if (pv > 21) {
                gain = -bet; result = "lose";
            } else if (isBlackjack && pv == 21 && dv != 21) {
                gain = round2(bet * 1.5); result = "win";
            } else if (dv > 21 || pv > dv) {
                gain = bet; result = "win";
            } else if (pv == dv) {
                gain = 0; result = "push";
            } else {
                gain = -bet; result = "lose";
            }

            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Blackjack", bet, gain, gain > 0);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format(
                "{\"ok\":true,\"result\":\"%s\",\"gain\":%.2f,\"newBalance\":%.2f,\"playerValue\":%d,\"dealerValue\":%d}",
                result, gain, newBalance, pv, dv));
        }

        /** Extract a JSON array substring by key name. */
        private String extractArray(String json, String key) {
            String search = "\"" + key + "\"";
            int ki = json.indexOf(search);
            if (ki < 0) return null;
            int colon = json.indexOf(':', ki + search.length());
            if (colon < 0) return null;
            int start = colon + 1;
            while (start < json.length() && json.charAt(start) == ' ') start++;
            if (start >= json.length() || json.charAt(start) != '[') return null;
            int depth = 0, end = start;
            while (end < json.length()) {
                char c = json.charAt(end);
                if (c == '[') depth++;
                else if (c == ']') { depth--; if (depth == 0) { end++; break; } }
                end++;
            }
            return json.substring(start, end);
        }

        /** Calculate blackjack hand value from a JSON array of {rank,suit} objects. */
        private int calcHandValueFromJson(String arr) {
            int total = 0, aces = 0;
            // Extract all rank values
            int pos = 0;
            while (pos < arr.length()) {
                int ri = arr.indexOf("\"rank\"", pos);
                if (ri < 0) break;
                int colon = arr.indexOf(':', ri + 6);
                if (colon < 0) break;
                int vs = colon + 1;
                while (vs < arr.length() && arr.charAt(vs) == ' ') vs++;
                if (vs >= arr.length()) break;
                String rank;
                if (arr.charAt(vs) == '"') {
                    int ve = arr.indexOf('"', vs + 1);
                    rank = arr.substring(vs + 1, ve);
                    pos = ve + 1;
                } else {
                    int ve = vs;
                    while (ve < arr.length() && ",}] ".indexOf(arr.charAt(ve)) < 0) ve++;
                    rank = arr.substring(vs, ve).trim();
                    pos = ve;
                }
                if (rank.equals("?")) { pos++; continue; }
                int val;
                if (rank.equals("Jack") || rank.equals("Queen") || rank.equals("King") ||
                    rank.equals("J") || rank.equals("Q") || rank.equals("K")) {
                    val = 10;
                } else if (rank.equals("Asso") || rank.equals("A")) {
                    val = 11; aces++;
                } else {
                    try { val = Integer.parseInt(rank); } catch (Exception e) { val = 0; }
                }
                total += val;
            }
            while (total > 21 && aces > 0) { total -= 10; aces--; }
            return total;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — ROULETTE
    // ─────────────────────────────────────────────────────────────────────────
    static class RouletteHandler implements HttpHandler {
        private static final int[] RED_NUMS = {1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36};

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);

            // Parse bets array: [{type,value,amount}]
            List<double[]> betAmounts = new ArrayList<>(); // [amount, multiplier_if_win]
            List<String>   betTypes   = new ArrayList<>();
            List<Integer>  betValues  = new ArrayList<>();
            parseBets(body, betAmounts, betTypes, betValues);

            if (betAmounts.isEmpty()) { err(ex, 400, "No bets"); return; }

            double totalBet = 0;
            for (double[] b : betAmounts) totalBet += b[0];
            if (user.getSaldo() < totalBet) { err(ex, 400, "Insufficient balance"); return; }

            ruotaRoulette wheel = new ruotaRoulette();
            int number = wheel.spin();
            String color = wheel.getColor(number);

            double totalWin = 0;
            StringBuilder betResults = new StringBuilder("[");
            for (int i = 0; i < betAmounts.size(); i++) {
                double amount = betAmounts.get(i)[0];
                String type = betTypes.get(i);
                int value = betValues.get(i);
                double mult = getMultiplier(type, value, number, color);
                boolean win = mult > 0;
                double winAmt = win ? round2(amount * mult) : 0;
                totalWin += winAmt;
                if (i > 0) betResults.append(",");
                betResults.append(String.format(
                    "{\"type\":\"%s\",\"value\":%d,\"amount\":%.2f,\"win\":%b,\"multiplier\":%.2f,\"winAmt\":%.2f}",
                    esc(type), value, amount, win, mult, winAmt));
            }
            betResults.append("]");

            double gain = round2(totalWin - totalBet);
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Roulette", totalBet, gain, gain > 0);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format(
                "{\"ok\":true,\"spin\":{\"number\":%d,\"color\":\"%s\"},\"betResults\":%s,\"totalBet\":%.2f,\"totalWin\":%.2f,\"gain\":%.2f,\"newBalance\":%.2f}",
                number, esc(color), betResults.toString(), totalBet, totalWin, gain, newBalance));
        }

        private void parseBets(String body, List<double[]> amounts, List<String> types, List<Integer> values) {
            // Find the bets array
            int bi = body.indexOf("\"bets\"");
            if (bi < 0) return;
            int arrStart = body.indexOf('[', bi);
            if (arrStart < 0) return;
            int depth = 0, pos = arrStart;
            int arrEnd = arrStart;
            while (pos < body.length()) {
                char c = body.charAt(pos);
                if (c == '[' || c == '{') depth++;
                else if (c == ']' || c == '}') { depth--; if (depth == 0) { arrEnd = pos; break; } }
                pos++;
            }
            String arr = body.substring(arrStart, arrEnd + 1);
            // Split by objects
            int objStart = -1;
            int d2 = 0;
            for (int i = 0; i < arr.length(); i++) {
                char c = arr.charAt(i);
                if (c == '{') { if (d2 == 0) objStart = i; d2++; }
                else if (c == '}') {
                    d2--;
                    if (d2 == 0 && objStart >= 0) {
                        String obj = arr.substring(objStart, i + 1);
                        String type = jsonStr(obj, "type");
                        double amount = jsonDouble(obj, "amount", 0);
                        int value = jsonInt(obj, "value", -1);
                        if (type != null && amount > 0) {
                            amounts.add(new double[]{amount});
                            types.add(type);
                            values.add(value);
                        }
                        objStart = -1;
                    }
                }
            }
        }

        private double getMultiplier(String type, int value, int number, String color) {
            switch (type.toLowerCase()) {
                case "number": return (value == number) ? 36 : 0;
                case "red":    return color.equals("red") ? 1.9 : 0;
                case "black":  return color.equals("black") ? 1.9 : 0;
                case "even":   return (number > 0 && number % 2 == 0) ? 1.9 : 0;
                case "odd":    return (number > 0 && number % 2 != 0) ? 1.9 : 0;
                case "low":    return (number >= 1 && number <= 18) ? 1.9 : 0;
                case "high":   return (number >= 19 && number <= 36) ? 1.9 : 0;
                case "dozen1": return (number >= 1 && number <= 12) ? 2.9 : 0;
                case "dozen2": return (number >= 13 && number <= 24) ? 2.9 : 0;
                case "dozen3": return (number >= 25 && number <= 36) ? 2.9 : 0;
                case "column1": return (number > 0 && number % 3 == 1) ? 2.9 : 0;
                case "column2": return (number > 0 && number % 3 == 2) ? 2.9 : 0;
                case "column3": return (number > 0 && number % 3 == 0) ? 2.9 : 0;
                default: return 0;
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — DADI
    // ─────────────────────────────────────────────────────────────────────────
    static class DadiHandler implements HttpHandler {
        private static final Random rand = new Random();

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            String betType = jsonStr(body, "betType");
            if (betType == null) betType = "pass";
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            int d1 = 1 + rand.nextInt(6);
            int d2 = 1 + rand.nextInt(6);
            int total = d1 + d2;

            boolean win;
            double multiplier;
            switch (betType.toLowerCase()) {
                case "pass":      win = (total == 7 || total == 11); multiplier = 1; break;
                case "dontpass":  win = (total == 2 || total == 3);  multiplier = 1; break;
                case "field":     win = (total==2||total==3||total==4||total==9||total==10||total==11||total==12); multiplier = 1.5; break;
                case "any7":      win = (total == 7);  multiplier = 4; break;
                case "hardway8":  win = (d1 == 4 && d2 == 4); multiplier = 9; break;
                case "hardway6":  win = (d1 == 3 && d2 == 3); multiplier = 9; break;
                default:          win = (total == 7 || total == 11); multiplier = 1;
            }

            double gain = win ? round2(bet * multiplier) : -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Dadi", bet, gain, win);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format(
                "{\"ok\":true,\"dice\":[%d,%d],\"total\":%d,\"win\":%b,\"multiplier\":%.2f,\"gain\":%.2f,\"newBalance\":%.2f}",
                d1, d2, total, win, multiplier, gain, newBalance));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — BACCARAT
    // ─────────────────────────────────────────────────────────────────────────
    static class BaccaratHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            String betType = jsonStr(body, "betType");
            if (betType == null) betType = "player";
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            Baccarat.BaccaratResult result = Baccarat.play(betType);

            double gain;
            String resultType;
            if (betType.equals(result.winner)) {
                gain = round2(bet * result.payout - bet);
                resultType = "win";
            } else if (result.winner.equals("tie") && !betType.equals("tie")) {
                gain = 0;
                resultType = "push";
            } else {
                gain = -bet;
                resultType = "lose";
            }

            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Baccarat", bet, gain, gain > 0);
            Database.recordGameResult(user.getId(), rec);

            // Build hand JSON arrays
            StringBuilder ph = new StringBuilder("[");
            for (int i = 0; i < result.playerHand.size(); i++) {
                if (i > 0) ph.append(",");
                Baccarat.Card c = result.playerHand.get(i);
                ph.append(cardJson(c.rank, c.suit));
            }
            ph.append("]");
            StringBuilder bh = new StringBuilder("[");
            for (int i = 0; i < result.bankerHand.size(); i++) {
                if (i > 0) bh.append(",");
                Baccarat.Card c = result.bankerHand.get(i);
                bh.append(cardJson(c.rank, c.suit));
            }
            bh.append("]");

            ok(ex, String.format(
                "{\"ok\":true,\"playerHand\":%s,\"bankerHand\":%s,\"playerValue\":%d,\"bankerValue\":%d,\"winner\":\"%s\",\"resultType\":\"%s\",\"gain\":%.2f,\"newBalance\":%.2f}",
                ph, bh, result.playerValue, result.bankerValue, esc(result.winner), resultType, gain, newBalance));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — POKER DEAL
    // ─────────────────────────────────────────────────────────────────────────
    static class PokerDealHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            List<VideoPoker.Card> hand = VideoPoker.dealHand();
            StringBuilder handJson = new StringBuilder("[");
            for (int i = 0; i < hand.size(); i++) {
                if (i > 0) handJson.append(",");
                handJson.append(cardJson(hand.get(i).rank, hand.get(i).suit));
            }
            handJson.append("]");

            ok(ex, String.format("{\"ok\":true,\"hand\":%s,\"gameId\":\"%d\"}", handJson, System.currentTimeMillis()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — POKER DRAW
    // ─────────────────────────────────────────────────────────────────────────
    static class PokerDrawHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            // Parse hand array
            List<VideoPoker.Card> hand = parsePokerHand(body, "hand");
            if (hand == null || hand.size() != 5) { err(ex, 400, "Invalid hand"); return; }

            // Parse held array [bool,bool,bool,bool,bool]
            boolean[] held = parseHeld(body);

            List<VideoPoker.Card> newHand = VideoPoker.drawCards(hand, held);
            VideoPoker.PokerResult result = VideoPoker.evaluateHand(newHand);

            double gain = result.multiplier > 0 ? round2(bet * result.multiplier - bet) : -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Video Poker", bet, gain, gain > 0);
            Database.recordGameResult(user.getId(), rec);

            StringBuilder handJson = new StringBuilder("[");
            for (int i = 0; i < newHand.size(); i++) {
                if (i > 0) handJson.append(",");
                handJson.append(cardJson(newHand.get(i).rank, newHand.get(i).suit));
            }
            handJson.append("]");

            ok(ex, String.format(
                "{\"ok\":true,\"hand\":%s,\"evaluation\":{\"name\":\"%s\",\"mult\":%d},\"gain\":%.2f,\"newBalance\":%.2f}",
                handJson, esc(result.handName), result.multiplier, gain, newBalance));
        }

        private List<VideoPoker.Card> parsePokerHand(String body, String key) {
            String search = "\"" + key + "\"";
            int ki = body.indexOf(search);
            if (ki < 0) return null;
            int arrStart = body.indexOf('[', ki);
            if (arrStart < 0) return null;
            List<VideoPoker.Card> cards = new ArrayList<>();
            int pos = arrStart + 1;
            while (pos < body.length()) {
                int objStart = body.indexOf('{', pos);
                if (objStart < 0) break;
                int objEnd = body.indexOf('}', objStart);
                if (objEnd < 0) break;
                String obj = body.substring(objStart, objEnd + 1);
                String rank = jsonStr(obj, "rank");
                String suit = jsonStr(obj, "suit");
                if (rank != null && suit != null) cards.add(new VideoPoker.Card(rank, suit));
                pos = objEnd + 1;
                if (pos < body.length() && body.charAt(pos) == ']') break;
            }
            return cards;
        }

        private boolean[] parseHeld(String body) {
            boolean[] held = new boolean[5];
            String search = "\"held\"";
            int ki = body.indexOf(search);
            if (ki < 0) return held;
            int arrStart = body.indexOf('[', ki);
            if (arrStart < 0) return held;
            int arrEnd = body.indexOf(']', arrStart);
            if (arrEnd < 0) return held;
            String arr = body.substring(arrStart + 1, arrEnd);
            String[] parts = arr.split(",");
            for (int i = 0; i < Math.min(parts.length, 5); i++) {
                held[i] = "true".equalsIgnoreCase(parts[i].trim());
            }
            return held;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — CHICKEN MOVE
    // ─────────────────────────────────────────────────────────────────────────
    static class ChickenMoveHandler implements HttpHandler {
        private static final Random rand = new Random();

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            int level    = jsonInt(body, "level", 0);
            int position = jsonInt(body, "position", 0);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }

            // Generate cars for this level using ChickenGame logic
            int numCars = Math.min(1 + level / 2, 4);
            boolean[] cars = new boolean[5];
            List<Integer> positions = new ArrayList<>();
            for (int i = 0; i < 5; i++) positions.add(i);
            Collections.shuffle(positions, rand);
            for (int i = 0; i < numCars; i++) cars[positions.get(i)] = true;

            boolean hit = (position >= 0 && position < 5) && cars[position];

            StringBuilder carsJson = new StringBuilder("[");
            for (int i = 0; i < 5; i++) {
                if (i > 0) carsJson.append(",");
                carsJson.append(cars[i]);
            }
            carsJson.append("]");

            ok(ex, String.format("{\"ok\":true,\"cars\":%s,\"hit\":%b}", carsJson, hit));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — CHICKEN CASHOUT
    // ─────────────────────────────────────────────────────────────────────────
    static class ChickenCashoutHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            double multiplier = jsonDouble(body, "multiplier", 1);
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            double winAmt = round2(bet * multiplier);
            double gain   = round2(winAmt - bet);
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Chicken Road", bet, gain, gain >= 0);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format("{\"ok\":true,\"winAmt\":%.2f,\"gain\":%.2f,\"newBalance\":%.2f}", winAmt, gain, newBalance));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — CHICKEN GAMEOVER
    // ─────────────────────────────────────────────────────────────────────────
    static class ChickenGameoverHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);

            double gain = -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Chicken Road", bet, gain, false);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format("{\"ok\":true,\"gain\":%.2f,\"newBalance\":%.2f}", gain, newBalance));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — VIRTUAL SPORTS (football, basket, tennis)
    // Handles: POST /api/games/virtual/:sport/generate
    //          POST /api/games/virtual/:sport/bet
    // ─────────────────────────────────────────────────────────────────────────
    static class VirtualSportHandler implements HttpHandler {
        private static final Random rand = new Random();

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            String path = ex.getRequestURI().getPath();
            // /api/games/virtual/{sport}/generate  or  /api/games/virtual/{sport}/bet
            // /api/games/virtual/race/simulate  (handled by VirtualRaceSimulateHandler)
            String[] parts = path.split("/");
            // parts: ["","api","games","virtual","{sport}","{action}"]
            if (parts.length < 6) { err(ex, 404, "Not found"); return; }
            String sport  = parts[4];
            String action = parts[5];

            // Race simulate is handled by VirtualRaceSimulateHandler — skip here
            if ("race".equals(sport)) { err(ex, 404, "Not found"); return; }

            if ("generate".equals(action)) {
                handleGenerate(ex, sport);
            } else if ("bet".equals(action)) {
                handleBet(ex, sport);
            } else if ("match".equals(action)) {
                // Legacy endpoint — treat as bet with random matchId
                handleLegacyMatch(ex, sport);
            } else {
                err(ex, 404, "Not found");
            }
        }

        private void handleGenerate(HttpExchange ex, String sport) throws IOException {
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }

            String home, away;
            double homeOdd, drawOdd = 0, awayOdd;
            String result;

            if ("tennis".equals(sport)) {
                // Tennis: usa giocatori reali
                String[] players = {"Djokovic","Alcaraz","Sinner","Medvedev","Nadal","Federer","Zverev","Tsitsipas"};
                int i1 = rand.nextInt(players.length);
                int i2;
                do { i2 = rand.nextInt(players.length); } while (i2 == i1);
                home = players[i1]; away = players[i2];
                homeOdd = Math.max(1.3, Math.round((1.4 + rand.nextDouble() * 1.2) * 100.0) / 100.0);
                awayOdd = Math.max(1.3, Math.round((1.4 + rand.nextDouble() * 1.2) * 100.0) / 100.0);
                result = rand.nextDouble() < (homeOdd < awayOdd ? 0.55 : 0.45) ? "home" : "away";
            } else if ("basket".equals(sport)) {
                // Basket: usa squadre NBA
                String[] teams = {"Lakers","Celtics","Warriors","Bulls","Heat","Nets","Bucks","Suns"};
                int i1 = rand.nextInt(teams.length);
                int i2;
                do { i2 = rand.nextInt(teams.length); } while (i2 == i1);
                home = teams[i1]; away = teams[i2];
                homeOdd = Math.max(1.3, Math.round((1.5 + rand.nextDouble() * 0.8) * 100.0) / 100.0);
                awayOdd = Math.max(1.3, Math.round((1.5 + rand.nextDouble() * 0.8) * 100.0) / 100.0);
                result = rand.nextDouble() < 0.5 ? "home" : "away";
            } else {
                // Calcio: usa VirtualCalcio con ForzaSquadra
                VirtualCalcio vc = new VirtualCalcio();
                vc.generateEvents();
                List<Eventi> events = vc.getEvents();
                if (events.isEmpty()) { err(ex, 500, "No events generated"); return; }
                Eventi e = events.get(0);
                home = e.home; away = e.away;
                homeOdd = e.homeOdd; drawOdd = e.drawOdd; awayOdd = e.awayOdd;
                result = simulateFootballResult(e);
            }

            String matchId = UUID.randomUUID().toString();
            pendingMatches.put(matchId, new Object[]{home, away, homeOdd, drawOdd, awayOdd, result, sport});

            // Pulisci match vecchi
            long now = System.currentTimeMillis();
            pendingMatches.entrySet().removeIf(entry -> {
                Object[] v = entry.getValue();
                return v.length > 7 && (long)v[7] < now - 600000;
            });

            String oddsJson = "tennis".equals(sport) || "basket".equals(sport)
                ? String.format("{\"home\":%.2f,\"away\":%.2f}", homeOdd, awayOdd)
                : String.format("{\"home\":%.2f,\"draw\":%.2f,\"away\":%.2f}", homeOdd, drawOdd, awayOdd);

            ok(ex, String.format(
                "{\"ok\":true,\"matchId\":\"%s\",\"match\":{\"home\":\"%s\",\"away\":\"%s\",\"odds\":%s}}",
                matchId, esc(home), esc(away), oddsJson));
        }

        private void handleBet(HttpExchange ex, String sport) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            String prediction = jsonStr(body, "prediction");
            String matchId    = jsonStr(body, "matchId");
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }
            if (matchId == null) { err(ex, 400, "Missing matchId"); return; }

            Object[] pending = pendingMatches.remove(matchId);
            if (pending == null) { err(ex, 400, "Match not found or expired"); return; }

            String home    = (String) pending[0];
            String away    = (String) pending[1];
            double homeOdd = (double) pending[2];
            double drawOdd = (double) pending[3];
            double awayOdd = (double) pending[4];
            String result  = (String) pending[5];

            // Normalizza prediction: tennis/basket non hanno draw
            String normalizedPrediction = prediction;
            if ("draw".equals(prediction) && drawOdd == 0) normalizedPrediction = "away";

            double odds;
            if ("home".equals(normalizedPrediction))      odds = homeOdd;
            else if ("draw".equals(normalizedPrediction)) odds = drawOdd;
            else                                          odds = awayOdd;

            boolean win = result.equals(normalizedPrediction);
            double gain = win ? round2(bet * odds - bet) : -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Virtual " + sport, bet, gain, win);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format(
                "{\"ok\":true,\"match\":{\"home\":\"%s\",\"away\":\"%s\",\"odds\":{\"home\":%.2f,\"draw\":%.2f,\"away\":%.2f}},\"win\":%b,\"gain\":%.2f,\"newBalance\":%.2f,\"result\":\"%s\"}",
                esc(home), esc(away), homeOdd, drawOdd, awayOdd, win, gain, newBalance, esc(result)));
        }

        private void handleLegacyMatch(HttpExchange ex, String sport) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            double bet = jsonDouble(body, "bet", 0);
            String prediction = jsonStr(body, "prediction");
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            VirtualCalcio vc = new VirtualCalcio();
            vc.generateEvents();
            List<Eventi> events = vc.getEvents();
            if (events.isEmpty()) { err(ex, 500, "No events"); return; }
            Eventi e = events.get(0);

            String result = simulateFootballResult(e);
            double homeOdd = e.homeOdd, drawOdd = e.drawOdd, awayOdd = e.awayOdd;
            double odds;
            if ("home".equals(prediction))      odds = homeOdd;
            else if ("draw".equals(prediction)) odds = drawOdd;
            else                                odds = awayOdd;

            boolean win = result.equals(prediction);
            double gain = win ? round2(bet * odds - bet) : -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            GameRecord rec = new GameRecord("Virtual " + sport, bet, gain, win);
            Database.recordGameResult(user.getId(), rec);

            ok(ex, String.format(
                "{\"ok\":true,\"match\":{\"home\":\"%s\",\"away\":\"%s\",\"odds\":{\"home\":%.2f,\"draw\":%.2f,\"away\":%.2f}},\"win\":%b,\"gain\":%.2f,\"newBalance\":%.2f,\"result\":\"%s\"}",
                esc(e.home), esc(e.away), homeOdd, drawOdd, awayOdd, win, gain, newBalance, esc(result)));
        }

        private String simulateFootballResult(Eventi e) {
            int homeStr = ForzaSquadra.get(e.home);
            int awayStr = ForzaSquadra.get(e.away);
            double diff = homeStr - awayStr;
            double homeProb = Math.max(0.18, Math.min(0.70, 0.40 + diff * 0.006));
            double awayProb = Math.max(0.15, Math.min(0.65, 0.32 - diff * 0.006));
            double drawProb = 0.28;
            double sum = homeProb + awayProb + drawProb;
            homeProb /= sum; awayProb /= sum;
            double r = rand.nextDouble();
            if (r < homeProb) return "home";
            if (r < homeProb + drawProb) return "draw";
            return "away";
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — VIRTUAL RACE PREVIEW (genera corridori con quote, senza simulare)
    // ─────────────────────────────────────────────────────────────────────────
    static class VirtualRacePreviewHandler implements HttpHandler {
        private static final Random rand = new Random();

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }

            String path = ex.getRequestURI().getPath();
            // /api/games/virtual/race/preview?sport=cavalli
            String query = ex.getRequestURI().getQuery();
            String sport = "cavalli";
            if (query != null && query.contains("sport=")) {
                sport = query.replace("sport=", "").split("&")[0];
            }

            boolean isCavalli = "cavalli".equals(sport);
            List<String> allNames = isCavalli ? ForzaCavalli.horses() : ForzaCani.dogs();
            List<String> shuffled = new ArrayList<>(allNames);
            Collections.shuffle(shuffled, rand);
            List<String> selected = shuffled.subList(0, Math.min(6, shuffled.size()));

            double totalStrength = 0;
            int[] strengths = new int[selected.size()];
            for (int i = 0; i < selected.size(); i++) {
                int s = isCavalli ? ForzaCavalli.get(selected.get(i)) : ForzaCani.get(selected.get(i));
                strengths[i] = s;
                totalStrength += s;
            }

            double[] odds = new double[selected.size()];
            StringBuilder runnersJson = new StringBuilder("[");
            for (int i = 0; i < selected.size(); i++) {
                double prob = (strengths[i] / totalStrength) * 0.92;
                odds[i] = Math.max(1.20, round2(1.0 / prob));
                if (i > 0) runnersJson.append(",");
                runnersJson.append(String.format("{\"idx\":%d,\"name\":\"%s\",\"strength\":%d,\"odd\":%.2f}",
                    i, esc(selected.get(i)), strengths[i], odds[i]));
            }
            runnersJson.append("]");

            // Salva la lista in pendingMatches con un raceId
            String raceId = UUID.randomUUID().toString();
            // Salva: [selected list, strengths, odds, sport, isCavalli]
            pendingMatches.put("race-" + raceId, new Object[]{
                new ArrayList<>(selected), strengths, odds, sport, isCavalli
            });

            ok(ex, String.format("{\"ok\":true,\"raceId\":\"%s\",\"runners\":%s}", raceId, runnersJson));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — VIRTUAL RACE SIMULATE (cavalli / cani)
    // ─────────────────────────────────────────────────────────────────────────
    static class VirtualRaceSimulateHandler implements HttpHandler {
        private static final Random rand = new Random();

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) { err(ex, 405, "Method not allowed"); return; }
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }
            String body = readBody(ex);
            String sport = jsonStr(body, "sport");
            double bet   = jsonDouble(body, "bet", 0);
            int pickedIdx = jsonInt(body, "pickedIdx", 0);
            String raceId = jsonStr(body, "raceId");
            if (bet < 1) { err(ex, 400, "Invalid bet"); return; }
            if (user.getSaldo() < bet) { err(ex, 400, "Insufficient balance"); return; }

            boolean isCavalli;
            List<String> selected;
            int[] strengths;
            double[] odds;

            // Se c'è un raceId, usa la lista pre-generata
            if (raceId != null && pendingMatches.containsKey("race-" + raceId)) {
                Object[] saved = (Object[]) pendingMatches.remove("race-" + raceId);
                selected  = (List<String>) saved[0];
                strengths = (int[]) saved[1];
                odds      = (double[]) saved[2];
                sport     = (String) saved[3];
                isCavalli = (boolean) saved[4];
            } else {
                // Fallback: genera nuova lista
                isCavalli = "cavalli".equals(sport);
                List<String> allNames = isCavalli ? ForzaCavalli.horses() : ForzaCani.dogs();
                List<String> shuffled = new ArrayList<>(allNames);
                Collections.shuffle(shuffled, rand);
                selected = shuffled.subList(0, Math.min(6, shuffled.size()));

                double totalStrength = 0;
                strengths = new int[selected.size()];
                for (int i = 0; i < selected.size(); i++) {
                    int s = isCavalli ? ForzaCavalli.get(selected.get(i)) : ForzaCani.get(selected.get(i));
                    strengths[i] = s;
                    totalStrength += s;
                }
                odds = new double[selected.size()];
                for (int i = 0; i < selected.size(); i++) {
                    double prob = (strengths[i] / totalStrength) * 0.92;
                    odds[i] = Math.max(1.20, round2(1.0 / prob));
                }
            }

            StringBuilder runnersJson = new StringBuilder("[");
            for (int i = 0; i < selected.size(); i++) {
                if (i > 0) runnersJson.append(",");
                runnersJson.append(String.format("{\"idx\":%d,\"name\":\"%s\",\"strength\":%d,\"odd\":%.2f}",
                    i, esc(selected.get(i)), strengths[i], odds[i]));
            }
            runnersJson.append("]");

            // Simulate race
            int RACE_LENGTH   = isCavalli ? 120 : 100;
            int BASE_STRENGTH = isCavalli ? 86  : 84;
            int BASE_STEP     = isCavalli ? 4   : 5;
            int RAND_RANGE    = isCavalli ? 5   : 6;
            int[] distance = new int[selected.size()];
            List<String> frames = new ArrayList<>();
            int winnerIdx = -1;

            while (winnerIdx == -1) {
                for (int i = 0; i < selected.size(); i++) {
                    int step = BASE_STEP + rand.nextInt(RAND_RANGE) + (strengths[i] - BASE_STRENGTH) / 6;
                    distance[i] = Math.min(RACE_LENGTH, distance[i] + Math.max(1, step));
                }
                StringBuilder frame = new StringBuilder("[");
                for (int i = 0; i < distance.length; i++) {
                    if (i > 0) frame.append(",");
                    frame.append(String.format("%.1f", (distance[i] * 100.0) / RACE_LENGTH));
                }
                frame.append("]");
                frames.add(frame.toString());

                for (int i = 0; i < selected.size(); i++) {
                    if (distance[i] >= RACE_LENGTH && winnerIdx == -1) winnerIdx = i;
                }
            }

            int picked = Math.max(0, Math.min(pickedIdx, selected.size() - 1));
            boolean won = (winnerIdx == picked);
            double pickedOdd = odds[picked];
            double gain = won ? round2(bet * pickedOdd - bet) : -bet;
            double newBalance = round2(user.getSaldo() + gain);
            user.setSaldo(newBalance);
            Database.saveUsers();
            String gameName = isCavalli ? "Corse Cavalli" : "Corse Cani";
            GameRecord rec = new GameRecord(gameName, bet, gain, won);
            Database.recordGameResult(user.getId(), rec);

            StringBuilder framesJson = new StringBuilder("[");
            for (int i = 0; i < frames.size(); i++) {
                if (i > 0) framesJson.append(",");
                framesJson.append(frames.get(i));
            }
            framesJson.append("]");

            ok(ex, String.format(
                "{\"ok\":true,\"runners\":%s,\"frames\":%s,\"winnerIdx\":%d,\"won\":%b,\"gain\":%.2f,\"newBalance\":%.2f,\"winnerName\":\"%s\",\"pickedName\":\"%s\"}",
                runnersJson, framesJson, winnerIdx, won, gain, newBalance,
                esc(selected.get(winnerIdx)), esc(selected.get(picked))));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GAMES — HISTORY
    // ─────────────────────────────────────────────────────────────────────────
    static class HistoryHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;
            User user = authUser(ex);
            if (user == null) { err(ex, 401, "Unauthorized"); return; }

            List<GameRecord> history = Database.getGameHistory(user.getId());
            StringBuilder arr = new StringBuilder("[");
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < history.size(); i++) {
                if (i > 0) arr.append(",");
                GameRecord r = history.get(i);
                arr.append(String.format(
                    "{\"id\":\"%s\",\"game\":\"%s\",\"bet\":%.2f,\"gain\":%.2f,\"win\":%b,\"timestamp\":\"%s\"}",
                    esc(r.getId()), esc(r.getGame()), r.getBet(), r.getGain(), r.isWin(),
                    r.getTimestamp() != null ? r.getTimestamp().format(fmt) : ""));
            }
            arr.append("]");
            ok(ex, "{\"ok\":true,\"history\":" + arr + "}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LEADERBOARD
    // ─────────────────────────────────────────────────────────────────────────
    static class LeaderboardHandler implements HttpHandler {
        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            List<User> users = new ArrayList<>(Database.getAllUsers());
            users.sort((a, b) -> Double.compare(b.getSaldo(), a.getSaldo()));

            StringBuilder arr = new StringBuilder("[");
            for (int i = 0; i < users.size(); i++) {
                if (i > 0) arr.append(",");
                User u = users.get(i);
                arr.append(String.format(
                    "{\"rank\":%d,\"username\":\"%s\",\"nome\":\"%s\",\"balance\":%.2f,\"level\":%d,\"xp\":%d,\"winRate\":%.2f,\"wins\":%d,\"gamesPlayed\":%d,\"totalGain\":%.2f}",
                    i + 1, esc(u.getUsername()), esc(u.getNome()), u.getSaldo(),
                    u.getCurrentLevel(), u.getXp(), u.getWinRate(),
                    u.getGiociVinti(), u.getGiociGiocati(), u.getGuadagnoTotale()));
            }
            arr.append("]");
            ok(ex, "{\"ok\":true,\"leaderboard\":" + arr + "}");
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIMULATION — 100 partite per gioco (Blackjack, Dadi, Roulette)
    // ─────────────────────────────────────────────────────────────────────────
    static class SimulationHandler implements HttpHandler {
        private static final int NUM = 100;
        private static final double BET = 10.0;
        private static final Random rand = new Random();

        public void handle(HttpExchange ex) throws IOException {
            setCors(ex);
            if (handleOptions(ex)) return;

            int bjVinte = 0, bjPerse = 0, bjPari = 0;
            double bjGain = 0;
            int dadiVinte = 0, dadiPerse = 0;
            double dadiGain = 0;
            int rouVinte = 0, rouPerse = 0;
            double rouGain = 0;

            // Blackjack — strategia base: stai su 17+
            for (int i = 0; i < NUM; i++) {
                MazzoCarte mazzo = new MazzoCarte();
                games.BlackJack.giocatori.GiocatoreUmano g =
                    new games.BlackJack.giocatori.GiocatoreUmano("Sim", 100000);
                games.BlackJack.giocatori.Banco b = new games.BlackJack.giocatori.Banco();
                g.aggiungiCarta(mazzo.pescaCarta()); b.aggiungiCarta(mazzo.pescaCarta());
                g.aggiungiCarta(mazzo.pescaCarta()); b.aggiungiCarta(mazzo.pescaCarta());
                int pv0 = g.valoreMano(), dv0 = b.valoreMano();
                if (pv0 == 21 && dv0 == 21) { bjPari++; continue; }
                if (pv0 == 21) { bjVinte++; bjGain += BET * 1.5; continue; }
                if (dv0 == 21) { bjPerse++; bjGain -= BET; continue; }
                while (g.valoreMano() < 17 && !g.haSballato()) g.aggiungiCarta(mazzo.pescaCarta());
                if (g.haSballato()) { bjPerse++; bjGain -= BET; continue; }
                while (b.devePescare()) b.aggiungiCarta(mazzo.pescaCarta());
                int pv = g.valoreMano(), dv = b.valoreMano();
                if (dv > 21 || pv > dv) { bjVinte++; bjGain += BET; }
                else if (pv < dv)        { bjPerse++; bjGain -= BET; }
                else                     { bjPari++; }
            }

            // Dadi — Pass Line
            for (int i = 0; i < NUM; i++) {
                int sum = (1 + rand.nextInt(6)) + (1 + rand.nextInt(6));
                if (sum == 7 || sum == 11) { dadiVinte++; dadiGain += BET; }
                else                       { dadiPerse++; dadiGain -= BET; }
            }

            // Roulette — sempre sul rosso, payout 1.9x
            ruotaRoulette wheel = new ruotaRoulette();
            for (int i = 0; i < NUM; i++) {
                int n = wheel.spin();
                String color = wheel.getColor(n);
                if ("red".equals(color)) { rouVinte++; rouGain += BET * 0.9; }
                else                     { rouPerse++; rouGain -= BET; }
            }

            double totGain = round2(bjGain + dadiGain + rouGain);
            double investito = NUM * BET * 3;
            double roi = round2((totGain / investito) * 100);

            ok(ex, String.format(
                "{\"ok\":true,\"numPartite\":%d,\"bet\":%.0f," +
                "\"giochi\":{" +
                  "\"blackjack\":{\"vinte\":%d,\"perse\":%d,\"pari\":%d,\"gain\":%.2f,\"nome\":\"Blackjack\",\"strategia\":\"Strategia base: stai su 17+\"}," +
                  "\"dadi\":{\"vinte\":%d,\"perse\":%d,\"gain\":%.2f,\"nome\":\"Dadi (Pass Line)\",\"strategia\":\"Vinci con 7 o 11\"}," +
                  "\"roulette\":{\"vinte\":%d,\"perse\":%d,\"gain\":%.2f,\"nome\":\"Roulette (rosso)\",\"strategia\":\"Payout 1.9x, prob. ~48.6%%\"}" +
                "}," +
                "\"totale\":{\"investito\":%.2f,\"gain\":%.2f,\"roi\":%.2f}}",
                NUM, BET,
                bjVinte, bjPerse, bjPari, round2(bjGain),
                dadiVinte, dadiPerse, round2(dadiGain),
                rouVinte, rouPerse, round2(rouGain),
                investito, totGain, roi));
        }
    }
}
