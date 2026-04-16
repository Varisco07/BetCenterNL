package games.virtual;

import java.util.*;
import core.State;
//
public class Schedina {

    private List<Double> odds = new ArrayList<>();

    public void addBet(double odd) {
        odds.add(odd);
    }

    public void clear() {
        odds.clear();
    }

    public void play(double amount) {

        if (odds.isEmpty()) {
            System.out.println("Schedina vuota!");
            return;
        }

        if (!State.deductBalance(amount)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        double totalOdds = 1.0;

        for (double o : odds) {
            totalOdds *= o;
        }

        Random rand = new Random();

        // piccolo margine bookmaker
        double winChance = 1.0 / totalOdds * 0.90;

        boolean win = rand.nextDouble() < winChance;

        if (win) {
            double payout = amount * totalOdds;
            State.addBalance(payout);
            System.out.println("🎉 SCHEDINA VINCENTE: " + payout);
        } else {
            System.out.println("❌ SCHEDINA PERSA");
        }

        clear();
    }
}