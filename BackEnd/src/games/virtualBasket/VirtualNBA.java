package games.virtualBasket;

import core.State;
import core.Auth;
import core.Database;
import core.GameRecord;
import core.User;

import java.util.*;

public class VirtualNBA {
    
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private List<EventiNBA> events = new ArrayList<>();

    private List<EventiNBA> schedinaEvents = new ArrayList<>();
    private List<Double> schedinaOdds = new ArrayList<>();
    private List<Integer> schedinaChoices = new ArrayList<>();

    private Random casuale = new Random();

    public void generateEvents() {

        events.clear();

        List<String> teams = List.of(
                "Boston Celtics", "Denver Nuggets", "Milwaukee Bucks", "Golden State Warriors",
                "Phoenix Suns", "Los Angeles Lakers", "Los Angeles Clippers", "Philadelphia 76ers",
                "Miami Heat", "Dallas Mavericks", "New York Knicks", "Toronto Raptors",
                "Chicago Bulls", "San Antonio Spurs", "Atlanta Hawks"
        );

        for (int i = 0; i < 5; i++) {

            String home = teams.get(casuale.nextInt(teams.size()));
            String away;

            do {
                away = teams.get(casuale.nextInt(teams.size()));
            } while (away.equals(home));

            double[] odds = betNBA.generate(home, away);
            double homeOdd = odds[0];
            double awayOdd = odds[1];

            events.add(new EventiNBA("NBA", home, away, homeOdd, awayOdd));
        }
    }

    public List<EventiNBA> getEvents() {
        return events;
    }

    public void addToSchedina(EventiNBA e, double odd, int choice) {
        schedinaEvents.add(e);
        schedinaOdds.add(odd);
        schedinaChoices.add(choice);
    }
    
    public boolean hasEventInSchedina(EventiNBA e) {
        return schedinaEvents.contains(e);
    }

    public void clearSchedina() {
        schedinaEvents.clear();
        schedinaOdds.clear();
        schedinaChoices.clear();
    }

    public void playSchedina(double amount) {

        if (schedinaEvents.isEmpty()) {
            System.out.println("Nessuna partita selezionata!");
            return;
        }

        if (!State.deductBalance(amount)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        System.out.println("\n━━━━━━━━━━ 🏀 " + CYAN + BOLD + "SIMULAZIONE PARTITE" + RESET + " ━━━━━━━━━━\n");

        double totalOdds = 1;
        boolean haVinto = true;

        for (int i = 0; i < schedinaEvents.size(); i++) {

            EventiNBA e = schedinaEvents.get(i);

            int winner = simulateWinner(e); // 1 casa, 2 trasferta
            int[] scores = generateScoreline(winner);
            int homeScore = scores[0];
            int awayScore = scores[1];

            int scelta = schedinaChoices.get(i);

            boolean esito;

            // SOLO 1 o 2
            if (winner == 1) {
                esito = (scelta == 1);
            } else {
                esito = (scelta == 2);
            }

            String outcome = winner == 1 ? "1" : "2";
            String coloredOutcome = outcome.equals("1") ? GREEN + "1" + RESET : RED + "2" + RESET;

            System.out.println(
                    BOLD + e.home + RESET + " " + homeScore + " - " + awayScore + " " + BOLD + e.away + RESET +
                            "  👉 " + coloredOutcome +
                            (esito ? " " + GREEN + "✔" + RESET : " " + RED + "❌" + RESET)
            );

            totalOdds *= schedinaOdds.get(i);

            if (!esito) haVinto = false;
        }

        System.out.println("\n━━━━━━━━━━ " + CYAN + BOLD + "RISULTATO SCHEDINA" + RESET + " ━━━━━━━━━━");

        if (haVinto) {
            double pagamento = amount * totalOdds;
            State.addBalance(pagamento);

            System.out.println(GREEN + BOLD + "🎫 VINCENTE!" + RESET);
            System.out.println("Quota totale: " + YELLOW + round(totalOdds) + RESET);
            System.out.println("Vincita: " + GREEN + round(pagamento) + RESET);

        } else {
            System.out.println(RED + BOLD + "💀 PERSA" + RESET);
        }

        System.out.println(CYAN + "💳 Saldo: " + round(State.getBalance()) + RESET);
        
        // Storico: registra l'esito come negli altri giochi virtual
        User utente = Auth.getCurrentUser();
        if (utente != null) {
            double guadagno = haVinto ? (amount * totalOdds - amount) : -amount;
            GameRecord registrazione = new GameRecord("🏀 Virtual", amount, guadagno, haVinto);
            Database.recordGameResult(utente.getId(), registrazione);
        }

        clearSchedina();
        generateEvents();
    }
    
    private int simulateWinner(EventiNBA e) {
        int homeStrength = forzaNBA.get(e.home);
        int awayStrength = forzaNBA.get(e.away);
        double diff = homeStrength - awayStrength;
        double homeWinProb = clamp(0.50 + (diff * 0.006), 0.20, 0.80);
        return casuale.nextDouble() < homeWinProb ? 1 : 2;
    }
    
    private int[] generateScoreline(int winner) {
        int loserScore = 80 + casuale.nextInt(31);   // 80-110
        int margin = 1 + casuale.nextInt(21);        // 1-21
        int winnerScore = Math.min(140, loserScore + margin);
        if (winner == 1) {
            return new int[]{winnerScore, loserScore};
        }
        return new int[]{loserScore, winnerScore};
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
    
    public static void printGameRules() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║          " + CYAN + BOLD + "🏀 VIRTUAL BASKETBALL - REGOLE" + RESET + "                    ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Scegli uno o più match NBA virtuali                    ║");
        System.out.println("║  2. Per ogni match scegli 1 (casa) oppure 2 (trasferta)    ║");
        System.out.println("║  3. Crea una schedina multipla (quote moltiplicate)        ║");
        System.out.println("║  4. Inserisci la puntata e simula le partite               ║");
        System.out.println("║  5. Vinci solo se indovini tutti i pronostici              ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "TIPI DI SCOMMESSA:" + RESET + "                                        ║");
        System.out.println("║  • 1 (Casa)      → Vince la squadra di casa                ║");
        System.out.println("║  • 2 (Trasferta) → Vince la squadra ospite                 ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "SCHEDINA MULTIPLA:" + RESET + "                                        ║");
        System.out.println("║  • Quota totale = Quota1 × Quota2 × Quota3 × ...           ║");
        System.out.println("║  • Vincita = Puntata × Quota totale                        ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "⚠️  GIOCO RESPONSABILE:" + RESET + "                                   ║");
        System.out.println("║  • Stabilisci un budget e rispetta i tuoi limiti           ║");
        System.out.println("║  • Non rincorrere le perdite                               ║");
        System.out.println("║  • Gioca per divertimento                                  ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}