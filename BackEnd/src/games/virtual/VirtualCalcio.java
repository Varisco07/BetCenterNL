package games.virtual;

import java.util.*;
import core.State;

public class VirtualCalcio {

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

        System.out.println("\n━━━━━━━━━━ ⚽ RISULTATI PARTITE ━━━━━━━━━━");

        // 🔥 risultati più realistici (pochi gol, stile calcio vero)
        for (Eventi e : events) {

            e.homeGoals = generateGoals();
            e.awayGoals = generateGoals();

            System.out.printf(
                    "%s %d - %d %s",
                    e.home, e.homeGoals, e.awayGoals, e.away
            );

            if (e.homeGoals > e.awayGoals) {
                System.out.println("  👉 1");
            } else if (e.homeGoals < e.awayGoals) {
                System.out.println("  👉 2");
            } else {
                System.out.println("  👉 X");
            }
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        System.out.println("\n🎫 SCHEDINA");

        for (double o : bets) {
            System.out.println("Quota: " + o);
        }

        System.out.println("Totale quote: " + round(totalOdds));

        System.out.println("\n💰 Puntata: " + amount);

        if (win) {
            double payout = amount * totalOdds;
            State.addBalance(payout);

            System.out.println("🎉 ESITO: VINTO");
            System.out.println("💵 Vincita: " + round(payout));

        } else {
            System.out.println("❌ ESITO: PERSO");
        }

        System.out.println("💳 Saldo: " + round(State.getBalance()));

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        bets.clear();
        generateEvents();
    }
}
