package games.dadi;

import core.State;
import core.random;

import java.util.Scanner;

public class dadi {

    private final Scanner sc = new Scanner(System.in);

    public void start() {

        System.out.println("🎲 CRAPS / DADI");
        System.out.println("💰 Saldo: " + State.getBalance());

        while (true) {

            System.out.println("\nScegli scommessa:");
            System.out.println("1 - Pass Line (1x)");
            System.out.println("2 - Don't Pass (1x)");
            System.out.println("3 - Field Bet (1.5x)");
            System.out.println("4 - Any Seven (4x)");
            System.out.println("5 - Hardway 8 (9x)");
            System.out.println("6 - Hardway 6 (9x)");
            System.out.println("0 - Esci");

            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            if (choice == 0) break;

            if (choice < 1 || choice > 6) {
                System.out.println("❌ Scelta non valida! Scegli tra 1 e 6.");
                continue;
            }

            System.out.print("Puntata: ");
            double bet = 0;
            try {
                bet = Double.parseDouble(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            if (bet <= 0) {
                System.out.println("❌ La puntata deve essere positiva!");
                continue;
            }

            play(choice, bet);
        }
    }

    private void play(int choice, double bet) {

        if (!State.deductBalance(bet)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        int d1 = roll();
        int d2 = roll();
        int sum = d1 + d2;

        animate(d1, d2);

        System.out.println("🎲 Risultato: " + d1 + " + " + d2 + " = " + sum);

        boolean win = false;
        double mult = 1;

        switch (choice) {

            case 1: // Pass Line
                if (sum == 7 || sum == 11) win = true;
                if (sum == 2 || sum == 3 || sum == 12) win = false;
                mult = 1;
                break;

            case 2: // Don't Pass
                if (sum == 2 || sum == 3) win = true;
                if (sum == 7 || sum == 11) win = false;
                mult = 1;
                break;

            case 3: // Field Bet
                if (sum == 2 || sum == 3 || sum == 4 || sum == 9 || sum == 10 || sum == 11 || sum == 12)
                    win = true;
                mult = 1.5;
                break;

            case 4: // Any 7
                win = (sum == 7);
                mult = 4;
                break;

            case 5: // Hard 8
                win = (d1 == 4 && d2 == 4);
                mult = 9;
                break;

            case 6: // Hard 6
                win = (d1 == 3 && d2 == 3);
                mult = 9;
                break;
        }

        if (win) {
            double payout = bet * mult;
            State.addBalance(payout);
            System.out.println("🎉 VINTO: " + payout);
        } else {
            System.out.println("❌ PERSO");
        }

        System.out.println("💰 Saldo: " + State.getBalance());
    }

    private int roll() {
        return random.randomInt(1, 6);
    }

    private void animate(int d1, int d2) {

        System.out.println("\n🎲 LANCIO DADI...");

        for (int i = 0; i < 10; i++) {
            int a = roll();
            int b = roll();
            System.out.print("\r🎲 " + a + " | " + b);
            sleep(80 + i * 10);
        }

        System.out.print("\r🎲 " + d1 + " | " + d2 + "   \n");
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}