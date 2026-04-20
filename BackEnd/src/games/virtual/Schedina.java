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

        Random casuale = new Random();

        // piccolo margine bookmaker
        double probabilitaVincita = 1.0 / totalOdds * 0.90;

        boolean haVinto = casuale.nextDouble() < probabilitaVincita;

        if (haVinto) {
            double pagamento = amount * totalOdds;
            State.addBalance(pagamento);
            System.out.println("🎉 SCHEDINA VINCENTE: " + pagamento);
        } else {
            System.out.println("❌ SCHEDINA PERSA");
        }

        clear();
    }
}