package games.virtualCani;

import core.State;

import java.util.Scanner;

public class CaniMain {

    private final Scanner tastiera = new Scanner(System.in);
    private final VirtualCani game = new VirtualCani();

    public static void main(String[] args) {
        new CaniMain().start();
    }

    public void start() {
        VirtualCani.printGameRules();
        System.out.println("\n=================================");
        System.out.println("      🐕 VIRTUAL CORSE CANI");
        System.out.println("=================================");

        game.generateEvents();
        boolean continua = true;

        while (continua && State.getBalance() > 0) {
            if (!chiediConfermaGioco()) break;

            mostraEventi();
            System.out.println("\n🎫 SCEGLI IL VINCENTE");
            game.clearBets();

            EventoCani evento = game.getEvents().get(0);
            int dogIdx;
            while (true) {
                System.out.println("\n" + evento.raceName + " - scegli il vincente:");
                for (int i = 0; i < evento.dogs.size(); i++) {
                    System.out.println((i + 1) + " - " + evento.dogs.get(i) + "  (" + evento.odds.get(i) + ")");
                }
                try {
                    dogIdx = Integer.parseInt(tastiera.nextLine()) - 1;
                    if (dogIdx < 0 || dogIdx >= evento.dogs.size()) {
                        System.out.println("❌ Cane non valido!");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Inserisci un numero valido!");
                }
            }
            game.setBet(evento, dogIdx, evento.odds.get(dogIdx));
            System.out.println("✔ selezionato: " + evento.dogs.get(dogIdx));

            double puntata;
            while (true) {
                System.out.print("\n💰 Puntata: ");
                try {
                    puntata = Double.parseDouble(tastiera.nextLine());
                    if (puntata <= 0) {
                        System.out.println("❌ La puntata deve essere positiva!");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Inserisci un numero valido!");
                }
            }

            game.play(puntata);

            while (true) {
                System.out.println("\nContinuare? (1 sì / 2 no)");
                String risposta = tastiera.nextLine().trim();
                if (risposta.equals("1")) { continua = true; break; }
                if (risposta.equals("2")) { continua = false; break; }
                System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
            }
        }

        System.out.println("\n💳 SALDO FINALE: " + State.getBalance());
    }

    private void mostraEventi() {
        System.out.println("\n🐕 CORSA DISPONIBILE\n");
        EventoCani e = game.getEvents().get(0);
        System.out.println("1) " + e.raceName);
        for (int j = 0; j < e.dogs.size(); j++) {
            System.out.println("   " + (j + 1) + ". " + e.dogs.get(j) + "  | quota " + e.odds.get(j));
        }
        System.out.println();
    }

    private boolean chiediConfermaGioco() {
        while (true) {
            System.out.println("\nVuoi giocare?");
            System.out.println("1 - Si");
            System.out.println("2 - No");
            String scelta = tastiera.nextLine().trim();
            if (scelta.equals("1")) return true;
            if (scelta.equals("2")) return false;
            System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
        }
    }
}
