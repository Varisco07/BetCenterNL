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
    private List<BetSelection> selections = new ArrayList<>();
    private Random casuale = new Random();
    
    private static class BetSelection {
        final Eventi event;
        final int pick; // 1=home, 2=draw, 3=away
        final double odd;
        
        BetSelection(Eventi event, int pick, double odd) {
            this.event = event;
            this.pick = pick;
            this.odd = odd;
        }
    }

    public void generateEvents() {

        events.clear();

        List<String> leagues = new ArrayList<>(Competizioni.LEAGUES.keySet());

        for (int i = 0; i < 5; i++) {

            String league = leagues.get(casuale.nextInt(leagues.size()));
            List<String> teams = Competizioni.LEAGUES.get(league);

            String home = teams.get(casuale.nextInt(teams.size()));
            String away;

            do {
                away = teams.get(casuale.nextInt(teams.size()));
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

    public void addBet(Eventi event, int pick, double odd) {
        selections.add(new BetSelection(event, pick, odd));
    }
    
    public boolean hasBetForEvent(Eventi event) {
        for (BetSelection s : selections) {
            if (s.event == event) {
                return true;
            }
        }
        return false;
    }
    
    public void clearBets() {
        selections.clear();
    }
    private int generateGoals() {
        // calcio realistico: 0-3 gol, più probabilità su 0-1-2
        int r = casuale.nextInt(100);

        if (r < 35) return 0;
        if (r < 70) return 1;
        if (r < 90) return 2;
        return 3;
    }
    
    private String simulateOutcome(Eventi e) {
        int homeStrength = ForzaSquadra.get(e.home);
        int awayStrength = ForzaSquadra.get(e.away);
        double diff = homeStrength - awayStrength;
        
        double homeProb = 0.40 + (diff * 0.006);
        double awayProb = 0.32 - (diff * 0.006);
        double drawProb = 0.28;
        
        homeProb = clamp(homeProb, 0.18, 0.70);
        awayProb = clamp(awayProb, 0.15, 0.65);
        
        double sum = homeProb + awayProb + drawProb;
        homeProb /= sum;
        awayProb /= sum;
        drawProb /= sum;
        
        double r = casuale.nextDouble();
        if (r < homeProb) return "1";
        if (r < homeProb + drawProb) return "X";
        return "2";
    }
    
    private void applyScoreFromOutcome(Eventi e, String outcome) {
        if (outcome.equals("1")) {
            e.homeGoals = 1 + casuale.nextInt(3);
            e.awayGoals = casuale.nextInt(Math.max(1, e.homeGoals));
        } else if (outcome.equals("2")) {
            e.awayGoals = 1 + casuale.nextInt(3);
            e.homeGoals = casuale.nextInt(Math.max(1, e.awayGoals));
        } else {
            int g = generateGoals();
            e.homeGoals = g;
            e.awayGoals = g;
        }
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public void play(double amount) {

        if (selections.isEmpty()) {
            System.out.println("❌ Nessuna puntata!");
            return;
        }

        if (!State.deductBalance(amount)) {
            System.out.println("❌ Saldo insufficiente!");
            return;
        }

        double totalOdds = 1;
        for (BetSelection s : selections) totalOdds *= s.odd;

        System.out.println("\n━━━━━━━━━━ ⚽ " + CYAN + BOLD + "RISULTATI PARTITE" + RESET + " ━━━━━━━━━━");

        boolean haVinto = true;
        
        // risultati per le sole partite scelte in schedina
        for (BetSelection s : selections) {
            Eventi e = s.event;
            String outcome = simulateOutcome(e);
            applyScoreFromOutcome(e, outcome);

            System.out.printf(
                    BOLD + "%s" + RESET + " %s%d" + RESET + " - %s%d" + RESET + " " + BOLD + "%s" + RESET,
                    e.home, 
                    (e.homeGoals > e.awayGoals ? GREEN : (e.homeGoals < e.awayGoals ? RED : YELLOW)), e.homeGoals,
                    (e.awayGoals > e.homeGoals ? GREEN : (e.awayGoals < e.homeGoals ? RED : YELLOW)), e.awayGoals,
                    e.away
            );

            if (outcome.equals("1")) {
                System.out.println("  👉 " + GREEN + "1" + RESET);
                if (s.pick != 1) haVinto = false;
            } else if (outcome.equals("2")) {
                System.out.println("  👉 " + RED + "2" + RESET);
                if (s.pick != 3) haVinto = false;
            } else {
                System.out.println("  👉 " + YELLOW + "X" + RESET);
                if (s.pick != 2) haVinto = false;
            }
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.println("\n🎫 " + CYAN + "SCHEDINA" + RESET);

        for (BetSelection s : selections) {
            String pickLabel = (s.pick == 1) ? "1" : (s.pick == 2) ? "X" : "2";
            System.out.println(
                    s.event.home + " vs " + s.event.away +
                    "  [" + pickLabel + "]  Quota: " + YELLOW + s.odd + RESET
            );
        }

        System.out.println("Totale quote: " + BOLD + YELLOW + round(totalOdds) + RESET);

        System.out.println("\n💰 Puntata: " + CYAN + amount + RESET);

        if (haVinto) {
            double pagamento = amount * totalOdds;
            State.addBalance(pagamento);

            System.out.println(GREEN + BOLD + "🎉 ESITO: VINTO" + RESET);
            System.out.println(GREEN + "💵 Vincita: " + round(pagamento) + RESET);

        } else {
            System.out.println(RED + BOLD + "❌ ESITO: PERSO" + RESET);
        }

        System.out.println(CYAN + "💳 Saldo: " + round(State.getBalance()) + RESET);

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // Registra il risultato nel database
        core.User utente = core.Auth.getCurrentUser();
        if (utente != null) {
            double guadagno = haVinto ? (amount * totalOdds - amount) : -amount;
            core.GameRecord registrazione = new core.GameRecord("🥅 Virtual", amount, guadagno, haVinto);
            core.Database.recordGameResult(utente.getId(), registrazione);
        }

        selections.clear();
    }
    
    public static void printGameRules() {
        final String RED = "\u001B[31m";
        final String GREEN = "\u001B[32m";
        final String YELLOW = "\u001B[33m";
        final String CYAN = "\u001B[36m";
        final String RESET = "\u001B[0m";
        final String BOLD = "\u001B[1m";
        
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          " + CYAN + BOLD + "⚽ VIRTUAL FOOTBALL - REGOLE DEL GIOCO" + RESET + "            ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Scegli uno o più eventi sportivi virtuali              ║");
        System.out.println("║  2. Per ogni evento, scommetti su 1 (casa), X (pareggio)   ║");
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
        System.out.println("║  • Quota totale = Quota1 × Quota2 × Quota3 × ...           ║");
        System.out.println("║  • Vincita = Puntata × Quota totale                        ║");
        System.out.println("║  • Esempio: 3 eventi con quote 2.0, 1.5, 3.0               ║");
        System.out.println("║    → Quota totale = 2.0 × 1.5 × 3.0 = 9.0                  ║");
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
