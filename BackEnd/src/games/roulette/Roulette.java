package games.roulette;

import java.util.*;
import core.State;

public class Roulette {

    private ruotaRoulette wheel = new ruotaRoulette();
    private List<Bet> bets = new ArrayList<>();

    public void addBet(Bet bet) {
        bets.add(bet);
    }

    public void spin() {

        if (bets.isEmpty()) {
            System.out.println("Nessuna puntata!");
            return;
        }

        double total = bets.stream().mapToDouble(b -> b.amount).sum();

        if (!State.deductBalance(total)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        int result = wheel.spin();
        String color = wheel.getColor(result);

        System.out.println("Numero: " + result + " (" + color + ")");

        double win = 0;

        for (Bet b : bets) {

            boolean vinto = false;

            switch (b.type) {
                case "number": vinto = b.number == result; break;
                case "red": vinto = color.equals("red"); break;
                case "black": vinto = color.equals("black"); break;
                case "even": vinto = result > 0 && result % 2 == 0; break;
                case "odd": vinto = result > 0 && result % 2 != 0; break;
            }

            if (vinto) win += b.amount * b.odds;
        }

        if (win > 0) {
            State.addBalance(win);
            System.out.println("VINTO: " + win);
        } else {
            System.out.println("PERSO");
        }

        bets.clear();
    }
}