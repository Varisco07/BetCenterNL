package games.virtualCavalli;

import core.State;

import java.util.Scanner;

public class CavalliMain {

    private final Scanner tastiera = new Scanner(System.in);
    private final VirtualCavalli game = new VirtualCavalli();

    public static void main(String[] args) {
        new CavalliMain().start();
    }

    public void start() {
        VirtualCavalli.printGameRules();
        System.out.println("\n=================================");
        System.out.println("     🐎 VIRTUAL CORSE CAVALLI");
        System.out.println("=================================");

        game.generateEvents();
        boolean continua = true;

        while (continua && State.getBalance() > 0) {
            if (!chiediConfermaGioco()) break;

            
            System.out.println("\n🎫 SCEGLI IL VINCENTE");
            game.clearBets();
            EventoCorsa evento = game.getEvents().get(0);
            int horseIdx;
            while (true) {
                System.out.println("\n" + evento.raceName + " - scegli il vincente:");
                for (int i = 0; i < evento.horses.size(); i++) {
                    System.out.println((i + 1) + " - " + evento.horses.get(i) + "  (" + evento.odds.get(i) + ")");
                }
                try {
                    horseIdx = Integer.parseInt(tastiera.nextLine()) - 1;
                    if (horseIdx < 0 || horseIdx >= evento.horses.size()) {
                        System.out.println("❌ Cavallo non valido!");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("❌ Inserisci un numero valido!");
                }
            }
            game.setBet(evento, horseIdx, evento.odds.get(horseIdx));
            System.out.println("✔ selezionato: " + evento.horses.get(horseIdx));

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
