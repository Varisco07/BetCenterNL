package games.dice;

import core.State;
import games.dadi.dadi;

import java.util.Scanner;

public class MainDadi {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        dadi game = new dadi();

        System.out.println("=================================");
        System.out.println("        🎲 CRAPS / DADI");
        System.out.println("=================================");

        while (State.getBalance() > 0) {

            System.out.println("\n-----------------------------");
            System.out.println("💰 Saldo: " + State.getBalance());
            System.out.println("-----------------------------");

            System.out.println("\nVuoi giocare?");
            System.out.println("1 - Si");
            System.out.println("0 - Esci");
            System.out.print("Scelta: ");

            String input = sc.nextLine();

            if (input.equals("0")) break;

            game.start();
        }

        System.out.println("\n🏁 GIOCO TERMINATO");
        System.out.println("💰 Saldo finale: " + State.getBalance());
    }
}