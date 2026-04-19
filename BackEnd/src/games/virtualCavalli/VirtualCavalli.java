package games.virtualCavalli;

import core.Auth;
import core.Database;
import core.GameRecord;
import core.State;
import core.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VirtualCavalli {

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static final int RACE_LENGTH = 120;

    private final List<EventoCorsa> events = new ArrayList<>();
    private EventoCorsa selectedEvent;
    private int selectedHorseIndex = -1;
    private double selectedOdd = 1.0;
    private final Random rand = new Random();

    public void generateEvents() {
        events.clear();
        List<String> all = ForzaCavalli.horses();

        List<String> shuffled = new ArrayList<>(all);
        Collections.shuffle(shuffled, rand);
        List<String> horses = new ArrayList<>(shuffled.subList(0, 6));
        List<Double> odds = generateOdds(horses);
        events.add(new EventoCorsa("Corsa Unica", horses, odds));
    }

    public List<EventoCorsa> getEvents() {
        return events;
    }

    public void setBet(EventoCorsa event, int horseIndex, double odd) {
        selectedEvent = event;
        selectedHorseIndex = horseIndex;
        selectedOdd = odd;
    }

    public void clearBets() {
        selectedEvent = null;
        selectedHorseIndex = -1;
        selectedOdd = 1.0;
    }

    public void play(double amount) {
        if (selectedEvent == null || selectedHorseIndex < 0) {
            System.out.println("❌ Nessuna selezione!");
            return;
        }
        if (!State.deductBalance(amount)) {
            System.out.println("❌ Saldo insufficiente!");
            return;
        }

        System.out.println("\n━━━━━━━━━━ 🐎 " + CYAN + BOLD + "SIMULAZIONE GARA" + RESET + " ━━━━━━━━━━");
        int winnerIdx = runRaceLive(selectedEvent);
        String winner = selectedEvent.horses.get(winnerIdx);
        String picked = selectedEvent.horses.get(selectedHorseIndex);
        boolean won = (winnerIdx == selectedHorseIndex);

        System.out.println("🎯 Tua scelta: " + picked + " | Vincitore: " + winner +
                (won ? " " + GREEN + "✔" + RESET : " " + RED + "❌" + RESET));
        System.out.println("Quota giocata: " + selectedOdd);

        if (won) {
            double payout = amount * selectedOdd;
            State.addBalance(payout);
            System.out.println(GREEN + BOLD + "🎉 ESITO: VINCENTE" + RESET);
            System.out.println("Quota: " + YELLOW + round(selectedOdd) + RESET);
            System.out.println("Vincita: " + GREEN + round(payout) + RESET);
        } else {
            System.out.println(RED + BOLD + "❌ ESITO: PERSA" + RESET);
        }

        System.out.println(CYAN + "💳 Saldo: " + round(State.getBalance()) + RESET);

        User user = Auth.getCurrentUser();
        if (user != null) {
            double gain = won ? (amount * selectedOdd - amount) : -amount;
            GameRecord record = new GameRecord("🐎 Virtual", amount, gain, won);
            Database.recordGameResult(user.getId(), record);
        }

        clearBets();
        generateEvents();
    }

    private List<Double> generateOdds(List<String> horses) {
        List<Double> strengths = new ArrayList<>();
        double total = 0;
        for (String h : horses) {
            double s = ForzaCavalli.get(h);
            strengths.add(s);
            total += s;
        }

        List<Double> odds = new ArrayList<>();
        for (double s : strengths) {
            double prob = (s / total) * 0.92;
            double odd = round(1.0 / prob);
            odds.add(Math.max(1.20, odd));
        }
        return odds;
    }

    private int runRaceLive(EventoCorsa event) {
        System.out.println("\n🏁 " + BOLD + event.raceName + RESET + " - VIA!");
        List<String> horses = event.horses;
        Map<String, Integer> distance = new HashMap<>();
        for (String h : horses) distance.put(h, 0);

        while (true) {
            String leader = null;
            int leadDistance = -1;

            for (String h : horses) {
                int strength = ForzaCavalli.get(h);
                int step = 4 + rand.nextInt(5) + (strength - 86) / 6; // base + boost forza
                int next = distance.get(h) + Math.max(1, step);
                distance.put(h, next);
                if (next > leadDistance) {
                    leadDistance = next;
                    leader = h;
                }
            }

            printRaceStatus(horses, distance, leader);

            for (int i = 0; i < horses.size(); i++) {
                if (distance.get(horses.get(i)) >= RACE_LENGTH) {
                    return i;
                }
            }

            sleep(350);
        }
    }

    private void printRaceStatus(List<String> horses, Map<String, Integer> distance, String leader) {
        List<String> sorted = new ArrayList<>(horses);
        sorted.sort(Comparator.comparingInt(distance::get).reversed());

        System.out.println("\nPosizioni attuali:");
        for (int i = 0; i < sorted.size(); i++) {
            String h = sorted.get(i);
            int d = Math.min(RACE_LENGTH, distance.get(h));
            String marker = h.equals(leader) ? " 👑" : "";
            System.out.printf("%d) %-18s %3dm/%dm%s%n", i + 1, h, d, RACE_LENGTH, marker);
        }
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }

    private double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    public static void printGameRules() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            🐎 VIRTUAL CORSE CAVALLI - REGOLE               ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. C'è una sola corsa per round                           ║");
        System.out.println("║  2. Scegli un solo cavallo vincente                        ║");
        System.out.println("║  3. Nessuna schedina multipla                              ║");
        System.out.println("║  4. Vinci se il tuo cavallo arriva primo                   ║");
        System.out.println("║  5. La gara viene simulata live nel terminale              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
