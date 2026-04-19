package games.virtualBasket;

import core.State;

import java.util.Scanner;

public class BasketMain {

    private Scanner tastiera = new Scanner(System.in);
    private VirtualNBA game = new VirtualNBA();

    public static void main(String[] args) {
        new BasketMain().start();
    }

    public void start() {
        VirtualNBA.printGameRules();
        System.out.println("\n=================================");
        System.out.println("      🏀 VIRTUAL BASKETBALL");
        System.out.println("=================================");

        game.generateEvents();

        boolean continua = true;

        while (continua && State.getBalance() > 0) {
            if (!chiediConfermaGioco()) break;

            mostraEventi();

            System.out.println("\n🎫 CREA SCHEDINA");

            game.clearSchedina();
            boolean aggiungi = true;

            while (aggiungi) {

                System.out.print("\nScegli evento: ");
                int scelta;
                try {
                    scelta = Integer.parseInt(tastiera.nextLine()) - 1;
                    if (scelta < 0 || scelta >= game.getEvents().size()) {
                        System.out.println("❌ Evento non valido!");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Inserisci un numero valido!");
                    continue;
                }

                EventiNBA e = game.getEvents().get(scelta);
                if (game.hasEventInSchedina(e)) {
                    System.out.println("❌ Evento già presente in schedina!");
                    continue;
                }

                System.out.println("1 - " + e.home);
                System.out.println("2 - " + e.away);

                int pick;
                try {
                    pick = Integer.parseInt(tastiera.nextLine());
                    if (pick != 1 && pick != 2) {
                        System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
                        continue;
                    }
                } catch (NumberFormatException e1) {
                    System.out.println("❌ Inserisci un numero valido!");
                    continue;
                }

                double odd = (pick == 1) ? e.homeOdd : (e.awayOdd);

                game.addToSchedina(e, odd, pick);

                System.out.println("✔ aggiunto: " + e.home + " vs " + e.away);

                while (true) {
                    System.out.print("Aggiungere altra partita? (1 sì / 2 no): ");
                    String risposta = tastiera.nextLine().trim();
                    if (risposta.equals("1")) { aggiungi = true; break; }
                    if (risposta.equals("2")) { aggiungi = false; break; }
                    System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
                }
            }
            
            double puntata;
            while (true) {
                System.out.print("\n💰 Puntata schedina: ");
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

            game.playSchedina(puntata);

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

        System.out.println("\n🏀 NBA EVENTS\n");

        int i = 1;
        for (EventiNBA e : game.getEvents()) {
            System.out.println(i + ") " + e.home + " vs " + e.away);
            System.out.println("   1:" + e.homeOdd + "  2:" + e.awayOdd);
            i++;
        }
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