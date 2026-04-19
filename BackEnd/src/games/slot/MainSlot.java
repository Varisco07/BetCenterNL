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
            if (!chiediConfermaGioco(sc)) {
                break;
            }
            System.out.print("Puntata (0 per uscire): ");
            double bet = 0;
            try {
                bet = Double.parseDouble(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            if (bet == 0) break;

            if (bet < 0) {
                System.out.println("❌ La puntata deve essere positiva!");
                continue;
            }

            slot.spin(bet);
            System.out.println();
        }

        System.out.println("Fine gioco!");
    }
    
    private static boolean chiediConfermaGioco(Scanner scanner) {
        while (true) {
            System.out.println("\nVuoi giocare?");
            System.out.println("1 - Si");
            System.out.println("2 - No");
            String scelta = scanner.nextLine().trim();
            if (scelta.equals("1")) return true;
            if (scelta.equals("2")) return false;
            System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
        }
    }
}
