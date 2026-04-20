package games.virtualCani;

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

public class VirtualCani {

    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static final int RACE_LENGTH = 100;

    private final List<EventoCani> events = new ArrayList<>();
    private EventoCani selectedEvent;
    private int selectedDogIndex = -1;
    private double selectedOdd = 1.0;
    private final Random casuale = new Random();

    public void generateEvents() {
        events.clear();
        List<String> all = ForzaCani.dogs();
        List<String> shuffled = new ArrayList<>(all);
        Collections.shuffle(shuffled, casuale);

        List<String> dogs = new ArrayList<>(shuffled.subList(0, 6));
        List<Double> odds = generateOdds(dogs);
        events.add(new EventoCani("Corsa Unica", dogs, odds));
    }

    public List<EventoCani> getEvents() {
        return events;
    }

    public void setBet(EventoCani event, int dogIndex, double odd) {
        selectedEvent = event;
        selectedDogIndex = dogIndex;
        selectedOdd = odd;
    }

    public void clearBets() {
        selectedEvent = null;
        selectedDogIndex = -1;
        selectedOdd = 1.0;
    }

    public void play(double amount) {
        if (selectedEvent == null || selectedDogIndex < 0) {
            System.out.println("❌ Nessuna selezione!");
            return;
        }
        if (!State.deductBalance(amount)) {
            System.out.println("❌ Saldo insufficiente!");
            return;
        }

        System.out.println("\n━━━━━━━━━━ 🐕 " + CYAN + BOLD + "SIMULAZIONE GARA" + RESET + " ━━━━━━━━━━");
        int indiceVincitore = runRaceLive(selectedEvent);
        String winner = selectedEvent.dogs.get(indiceVincitore);
        String picked = selectedEvent.dogs.get(selectedDogIndex);
        boolean haVinto = (indiceVincitore == selectedDogIndex);

        System.out.println("🎯 Tua scelta: " + picked + " | Vincitore: " + winner +
                (haVinto ? " " + GREEN + "✔" + RESET : " " + RED + "❌" + RESET));
        System.out.println("Quota giocata: " + selectedOdd);

        if (haVinto) {
            double pagamento = amount * selectedOdd;
            State.addBalance(pagamento);
            System.out.println(GREEN + BOLD + "🎉 ESITO: VINCENTE" + RESET);
            System.out.println("Quota: " + YELLOW + round(selectedOdd) + RESET);
            System.out.println("Vincita: " + GREEN + round(pagamento) + RESET);
        } else {
            System.out.println(RED + BOLD + "❌ ESITO: PERSA" + RESET);
        }

        System.out.println(CYAN + "💳 Saldo: " + round(State.getBalance()) + RESET);

        User utente = Auth.getCurrentUser();
        if (utente != null) {
            double guadagno = haVinto ? (amount * selectedOdd - amount) : -amount;
            GameRecord registrazione = new GameRecord("🐕 Virtual", amount, guadagno, haVinto);
            Database.recordGameResult(utente.getId(), registrazione);
        }

        clearBets();
        generateEvents();
    }

    private List<Double> generateOdds(List<String> dogs) {
        List<Double> strengths = new ArrayList<>();
        double total = 0;
        for (String d : dogs) {
            double s = ForzaCani.get(d);
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

    private int runRaceLive(EventoCani event) {
        System.out.println("\n🏁 " + BOLD + event.raceName + RESET + " - VIA!");
        List<String> dogs = event.dogs;
        Map<String, Integer> distance = new HashMap<>();
        for (String d : dogs) distance.put(d, 0);

        while (true) {
            String leader = null;
            int leadDistance = -1;

            for (String d : dogs) {
                int strength = ForzaCani.get(d);
                int step = 5 + casuale.nextInt(6) + (strength - 84) / 6;
                int next = distance.get(d) + Math.max(1, step);
                distance.put(d, next);
                if (next > leadDistance) {
                    leadDistance = next;
                    leader = d;
                }
            }

            printRaceStatus(dogs, distance, leader);

            for (int i = 0; i < dogs.size(); i++) {
                if (distance.get(dogs.get(i)) >= RACE_LENGTH) {
                    return i;
                }
            }

            sleep(320);
        }
    }

    private void printRaceStatus(List<String> dogs, Map<String, Integer> distance, String leader) {
        List<String> sorted = new ArrayList<>(dogs);
        sorted.sort(Comparator.comparingInt(distance::get).reversed());

        System.out.println("\nPosizioni attuali:");
        for (int i = 0; i < sorted.size(); i++) {
            String d = sorted.get(i);
            int dist = Math.min(RACE_LENGTH, distance.get(d));
            String marker = d.equals(leader) ? " 👑" : "";
            System.out.printf("%d) %-20s %3dm/%dm%s%n", i + 1, d, dist, RACE_LENGTH, marker);
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
        System.out.println("║              🐕 VIRTUAL CORSE CANI - REGOLE                ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║  1. C'è una sola corsa per round                           ║");
        System.out.println("║  2. Scegli un solo cane vincente                           ║");
        System.out.println("║  3. Nessuna schedina multipla                              ║");
        System.out.println("║  4. Vinci se il tuo cane arriva primo                      ║");
        System.out.println("║  5. La gara viene simulata live nel terminale              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
