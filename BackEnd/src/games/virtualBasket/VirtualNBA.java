package games.virtualBasket;

import core.State;

import java.util.*;

public class VirtualNBA {

    private List<EventiNBA> events = new ArrayList<>();

    private List<EventiNBA> schedinaEvents = new ArrayList<>();
    private List<Double> schedinaOdds = new ArrayList<>();
    private List<Integer> schedinaChoices = new ArrayList<>();

    private Random rand = new Random();

    public void generateEvents() {

        events.clear();

        List<String> teams = List.of(
                "Lakers", "Bulls", "Celtics", "Heat", "Warriors",
                "Nuggets", "Spurs", "Mavericks", "Raptors", "Knicks",
                "Suns", "Clippers", "76ers", "Bucks", "Hawks"
        );

        for (int i = 0; i < 5; i++) {

            String home = teams.get(rand.nextInt(teams.size()));
            String away;

            do {
                away = teams.get(rand.nextInt(teams.size()));
            } while (away.equals(home));

            double homeOdd = Math.round((1.70 + rand.nextDouble()) * 100.0) / 100.0;
            double awayOdd = Math.round((1.70 + rand.nextDouble()) * 100.0) / 100.0;

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

        System.out.println("\n🏀 SIMULAZIONE PARTITE...\n");

        double totalOdds = 1;
        boolean win = true;

        for (int i = 0; i < schedinaEvents.size(); i++) {

            EventiNBA e = schedinaEvents.get(i);

            int homeScore = rand.nextInt(130);
            int awayScore = rand.nextInt(130);

            int choice = schedinaChoices.get(i);

            boolean result;

            // SOLO 1 o 2
            if (homeScore > awayScore) {
                result = (choice == 1);
            } else {
                result = (choice == 2);
            }

            String outcome = homeScore > awayScore ? "1" : "2";

            System.out.println(
                    e.home + " " + homeScore + " - " + awayScore + " " + e.away +
                            "  👉 " + outcome +
                            (result ? " ✔" : " ❌")
            );

            totalOdds *= schedinaOdds.get(i);

            if (!result) win = false;
        }

        System.out.println("\n━━━━━━━━━━ RISULTATO SCHEDINA ━━━━━━━━━━");

        if (win) {
            double payout = amount * totalOdds;
            State.addBalance(payout);

            System.out.println("🎫 VINCENTE!");
            System.out.println("Quota totale: " + totalOdds);
            System.out.println("Vincita: " + payout);

        } else {
            System.out.println("💀 PERSA");
        }

        System.out.println("💳 Saldo: " + State.getBalance());

        clearSchedina();
        generateEvents();
    }
}