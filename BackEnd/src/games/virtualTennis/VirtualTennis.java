package games.virtualTennis;

import core.Auth;
import core.Database;
import core.GameRecord;
import core.State;
import core.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VirtualTennis {

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private final List<EventiTennis> events = new ArrayList<>();
    private final List<EventiTennis> schedinaEvents = new ArrayList<>();
    private final List<Double> schedinaOdds = new ArrayList<>();
    private final List<Integer> schedinaChoices = new ArrayList<>();
    private final Random casuale = new Random();

    public void generateEvents() {
        events.clear();
        List<String> players = ForzaTennis.players();

        for (int i = 0; i < 5; i++) {
            String home = players.get(casuale.nextInt(players.size()));
            String away;
            do {
                away = players.get(casuale.nextInt(players.size()));
            } while (away.equals(home));

            double[] odds = QuoteTennis.generate(home, away);
            events.add(new EventiTennis("ATP Virtual", home, away, odds[0], odds[1]));
        }
    }

    public List<EventiTennis> getEvents() {
        return events;
    }

    public void addToSchedina(EventiTennis e, double odd, int choice) {
        schedinaEvents.add(e);
        schedinaOdds.add(odd);
        schedinaChoices.add(choice);
    }

    public boolean hasEventInSchedina(EventiTennis e) {
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

        System.out.println("\n━━━━━━━━━━ 🎾 " + CYAN + BOLD + "SIMULAZIONE MATCH" + RESET + " ━━━━━━━━━━\n");

        double totalOdds = 1;
        boolean haVinto = true;

        for (int i = 0; i < schedinaEvents.size(); i++) {
            EventiTennis e = schedinaEvents.get(i);

            int homeSets = 0;
            int awaySets = 0;
            double homeSetWinProb = computeHomeSetWinProbability(e.home, e.away);
            while (homeSets < 2 && awaySets < 2) {
                if (casuale.nextDouble() < homeSetWinProb) {
                    homeSets++;
                } else {
                    awaySets++;
                }
            }

            int scelta = schedinaChoices.get(i);
            boolean esito = (homeSets > awaySets) ? (scelta == 1) : (scelta == 2);
            String outcome = (homeSets > awaySets) ? "1" : "2";
            String coloredOutcome = outcome.equals("1") ? GREEN + "1" + RESET : RED + "2" + RESET;

            System.out.println(
                    BOLD + e.home + RESET + " " + homeSets + "-" + awaySets + " " + BOLD + e.away + RESET +
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

        User utente = Auth.getCurrentUser();
        if (utente != null) {
            double guadagno = haVinto ? (amount * totalOdds - amount) : -amount;
            GameRecord registrazione = new GameRecord("🎾 Virtual", amount, guadagno, haVinto);
            Database.recordGameResult(utente.getId(), registrazione);
        }

        clearSchedina();
        generateEvents();
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
    
    private double computeHomeSetWinProbability(String home, String away) {
        int homeStrength = ForzaTennis.get(home);
        int awayStrength = ForzaTennis.get(away);
        double diff = homeStrength - awayStrength;
        double prob = 0.50 + (diff * 0.005);
        return clamp(prob, 0.35, 0.65);
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static void printGameRules() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            " + CYAN + BOLD + "🎾 VIRTUAL TENNIS - REGOLE" + RESET + "                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Scegli uno o più match ATP virtuali                    ║");
        System.out.println("║  2. Per ogni match scegli 1 (giocatore a sinistra)         ║");
        System.out.println("║     oppure 2 (giocatore a destra)                          ║");
        System.out.println("║  3. Crea una schedina multipla (quote moltiplicate)        ║");
        System.out.println("║  4. Inserisci la puntata e simula i match (best of 3 set)  ║");
        System.out.println("║  5. Vinci solo se indovini tutti i pronostici              ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "SCHEDINA MULTIPLA:" + RESET + "                                        ║");
        System.out.println("║  • Quota totale = Quota1 × Quota2 × Quota3 × ...           ║");
        System.out.println("║  • Vincita = Puntata × Quota totale                        ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "⚠️  GIOCO RESPONSABILE:" + RESET + "                                   ║");
        System.out.println("║  • Gioca con moderazione e rispetta i tuoi limiti          ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
