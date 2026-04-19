package games.virtual;

import java.util.*;
import core.*;

public class VirtualCalcio {
    
    // ANSI Color codes
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private List<Eventi> events = new ArrayList<>();
    private List<Double> bets = new ArrayList<>();
    private Random rand = new Random();

    public void generateEvents() {

        events.clear();

        List<String> leagues = new ArrayList<>(Competizioni.LEAGUES.keySet());

        for (int i = 0; i < 5; i++) {

            String league = leagues.get(rand.nextInt(leagues.size()));
            List<String> teams = Competizioni.LEAGUES.get(league);

            String home = teams.get(rand.nextInt(teams.size()));
            String away;

            do {
                away = teams.get(rand.nextInt(teams.size()));
            } while (away.equals(home));

            double[] odds = Quote.generate(home, away);

            events.add(new Eventi(
                    league, home, away,
                    odds[0], odds[1], odds[2]
            ));
        }
    }

    public List<Eventi> getEvents() {
        return events;
    }

    public void addBet(double odds) {
        bets.add(odds);
    }
    public void clearBets() {
        bets.clear();
    }
    private int generateGoals() {
        // calcio realistico: 0-3 gol, più probabilità su 0-1-2
        int r = rand.nextInt(100);

        if (r < 35) return 0;
        if (r < 70) return 1;
        if (r < 90) return 2;
        return 3;
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public void play(double amount) {

        if (bets.isEmpty()) {
            System.out.println("❌ Nessuna puntata!");
            return;
        }

        if (!State.deductBalance(amount)) {
            System.out.println("❌ Saldo insufficiente!");
            return;
        }

        double totalOdds = 1;
        for (double o : bets) totalOdds *= o;

        double chance = (1.0 / totalOdds) * 0.92;
        boolean win = rand.nextDouble() < chance;

        System.out.println("\n━━━━━━━━━━ ⚽ " + CYAN + BOLD + "RISULTATI PARTITE" + RESET + " ━━━━━━━━━━");

        // 🔥 risultati più realistici (pochi gol, stile calcio vero)
        for (Eventi e : events) {

            e.homeGoals = generateGoals();
            e.awayGoals = generateGoals();

            System.out.printf(
                    BOLD + "%s" + RESET + " %s%d" + RESET + " - %s%d" + RESET + " " + BOLD + "%s" + RESET,
                    e.home, 
                    (e.homeGoals > e.awayGoals ? GREEN : (e.homeGoals < e.awayGoals ? RED : YELLOW)), e.homeGoals,
                    (e.awayGoals > e.homeGoals ? GREEN : (e.awayGoals < e.homeGoals ? RED : YELLOW)), e.awayGoals,
                    e.away
            );

            if (e.homeGoals > e.awayGoals) {
                System.out.println("  👉 " + GREEN + "1" + RESET);
            } else if (e.homeGoals < e.awayGoals) {
                System.out.println("  👉 " + RED + "2" + RESET);
            } else {
                System.out.println("  👉 " + YELLOW + "X" + RESET);
            }
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.println("\n🎫 " + CYAN + "SCHEDINA" + RESET);

        for (double o : bets) {
            System.out.println("Quota: " + YELLOW + o + RESET);
        }

        System.out.println("Totale quote: " + BOLD + YELLOW + round(totalOdds) + RESET);

        System.out.println("\n💰 Puntata: " + CYAN + amount + RESET);

        if (win) {
            double payout = amount * totalOdds;
            State.addBalance(payout);

            System.out.println(GREEN + BOLD + "🎉 ESITO: VINTO" + RESET);
            System.out.println(GREEN + "💵 Vincita: " + round(payout) + RESET);

        } else {
            System.out.println(RED + BOLD + "❌ ESITO: PERSO" + RESET);
        }

        System.out.println(CYAN + "💳 Saldo: " + round(State.getBalance()) + RESET);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Registra il risultato nel database
        core.User user = core.Auth.getCurrentUser();
        if (user != null) {
            double gain = win ? (amount * totalOdds - amount) : -amount;
            core.GameRecord record = new core.GameRecord("Virtual Sports", amount, gain, win);
            core.Database.recordGameResult(user.getId(), record);
        }

        bets.clear();
        generateEvents();
    }
    
    public static void printGameRules() {
        final String RED = "\u001B[31m";
        final String GREEN = "\u001B[32m";
        final String YELLOW = "\u001B[33m";
        final String CYAN = "\u001B[36m";
        final String RESET = "\u001B[0m";
        final String BOLD = "\u001B[1m";
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          " + CYAN + BOLD + "⚽ VIRTUAL SPORTS - REGOLE DEL GIOCO" + RESET + "         ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Scegli uno o più eventi sportivi virtuali              ║");
        System.out.println("║  2. Per ogni evento, scommetti su 1 (casa), X (pareggio)  ║");
        System.out.println("║     o 2 (trasferta)                                        ║");
        System.out.println("║  3. Crea una schedina multipla (le quote si moltiplicano)  ║");
        System.out.println("║  4. Piazza la tua puntata                                  ║");
        System.out.println("║  5. Vinci se TUTTE le scommesse sono corrette              ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "TIPI DI SCOMMESSA:" + RESET + "                                        ║");
        System.out.println("║  • 1 (Casa)      → La squadra di casa vince                ║");
        System.out.println("║  • X (Pareggio)  → Le squadre pareggiano                   ║");
        System.out.println("║  • 2 (Trasferta) → La squadra ospite vince                 ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "SCHEDINA MULTIPLA:" + RESET + "                                        ║");
        System.out.println("║  • Quota totale = Quota1 × Quota2 × Quota3 × ...          ║");
        System.out.println("║  • Vincita = Puntata × Quota totale                        ║");
        System.out.println("║  • Esempio: 3 eventi con quote 2.0, 1.5, 3.0              ║");
        System.out.println("║    → Quota totale = 2.0 × 1.5 × 3.0 = 9.0                 ║");
        System.out.println("║    → Con €10 vinci €90 se indovini tutto                   ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "⚠️  GIOCO RESPONSABILE:" + RESET + "                                   ║");
        System.out.println("║  • Le schedine multiple sono più rischiose                 ║");
        System.out.println("║  • Basta un errore per perdere tutto                       ║");
        System.out.println("║  • Stabilisci un budget e non superarlo                    ║");
        System.out.println("║  • Non inseguire le perdite con puntate più alte           ║");
        System.out.println("║  • Il gioco deve essere un divertimento, non un'ossessione ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
