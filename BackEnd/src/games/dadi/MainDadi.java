package games.dadi;

import core.State;
import games.dadi.dadi;

import java.util.Scanner;

public class MainDadi {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        dadi game = new dadi();

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           🎲 CRAPS / DADI              ║");
        System.out.println("╚════════════════════════════════════════╝");

        while (State.getBalance() > 0) {

            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.printf( "║ 💰 Saldo: €%-28s║%n", String.format("%.2f", State.getBalance()));
            System.out.println("╚════════════════════════════════════════╝");

            System.out.println("\nVuoi giocare?");
            System.out.println("1 - Si");
            System.out.println("2 - No");
            System.out.print("Scelta: ");

            String input = sc.nextLine().trim();

            if (input.equals("2")) break;
            if (!input.equals("1")) {
                System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
                continue;
            }

            game.start();
        }

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           🏁 GIOCO TERMINATO           ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf( "║ 💰 Saldo finale: €%-21s║%n", String.format("%.2f", State.getBalance()));
        System.out.println("╚════════════════════════════════════════╝");
    }
}