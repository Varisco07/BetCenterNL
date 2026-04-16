package games.slot;

import core.State;

import java.util.Scanner;

public class MainSlot {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        SlotMachine slot = new SlotMachine();

        System.out.println("🎰 SLOT MACHINE");
        System.out.println("💰 Saldo: " + State.getBalance());

        while (true) {
            System.out.print("Puntata (0 per uscire): ");
            double bet = Double.parseDouble(sc.nextLine());

            if (bet == 0) break;

            slot.spin(bet);
            System.out.println();
        }

        System.out.println("Fine gioco!");
    }
}
