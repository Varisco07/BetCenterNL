package games.roulette;

import core.State;
import java.util.*;

public class RouletteMain {
    public static void main(String[] args) {
        new RouletteMain().avviaGioco();
    }

    private Scanner tastiera = new Scanner(System.in);
    private Roulette game = new Roulette();

    public void avviaGioco() {

        stampaBenvenuto();

        boolean continua = true;

        while (continua && State.getBalance() > 0) {

            System.out.println("\nSaldo attuale: " + State.getBalance());

            double puntata = chiediPuntata();

            System.out.println("\nScegli tipo scommessa:");
            System.out.println("1 - Numero");
            System.out.println("2 - Rosso");
            System.out.println("3 - Nero");
            System.out.println("4 - Pari");
            System.out.println("5 - Dispari");

            int scelta = -1;
            try {
                scelta = Integer.parseInt(tastiera.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            switch (scelta) {

                case 1:
                    System.out.print("Numero (0-36): ");
                    int num = -1;
                    try {
                        num = Integer.parseInt(tastiera.nextLine());
                        if (num < 0 || num > 36) {
                            System.out.println("❌ Numero non valido! Scegli tra 0 e 36.");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("❌ Inserisci un numero valido!");
                        continue;
                    }
                    game.addBet(new Bet("number", num, puntata, 36));
                    break;

                case 2:
                    game.addBet(new Bet("red", 0, puntata, 1.9));
                    break;

                case 3:
                    game.addBet(new Bet("black", 0, puntata, 1.9));
                    break;

                case 4:
                    game.addBet(new Bet("even", 0, puntata, 1.9));
                    break;

                case 5:
                    game.addBet(new Bet("odd", 0, puntata, 1.9));
                    break;

                default:
                    System.out.println("❌ Scelta non valida! Scegli tra 1 e 5.");
                    continue;
            }

            giocaSpin();

            continua = chiediContinua();
        }

        System.out.println("\nGioco terminato. Saldo finale: " + State.getBalance());
    }

    private void giocaSpin() {

        System.out.println("\n🎯 Lancio della roulette...");
        aspetta(2000);

        game.spin();

        aspetta(1000);
    }

    private double chiediPuntata() {

        while (true) {

            System.out.print("Quanto vuoi puntare? ");

            try {
                double p = Double.parseDouble(tastiera.nextLine());

                if (p <= 0) {
                    System.out.println("❌ La puntata deve essere positiva!");
                } else {
                    return p;
                }

            } catch (NumberFormatException e) {
                System.out.println("❌ Numero non valido!");
            }
        }
    }

    private boolean chiediContinua() {
        System.out.println("\nVuoi continuare?");
        System.out.println("1 - Si");
        System.out.println("2 - No");

        String s = tastiera.nextLine();
        return s.equals("1");
    }

    private void stampaBenvenuto() {
        System.out.println("=================================");
        System.out.println("        🎰 ROULETTE GAME");
        System.out.println("=================================");
    }

    private void aspetta(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}