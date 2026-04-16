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

        System.out.println("=================================");
        System.out.println("      🏀 VIRTUAL BASKETBALL");
        System.out.println("=================================\n");

        game.generateEvents();

        boolean continua = true;

        while (continua && State.getBalance() > 0) {

            mostraEventi();

            System.out.println("\n🎫 CREA SCHEDINA");

            boolean aggiungi = true;

            while (aggiungi) {

                System.out.print("\nScegli evento: ");
                int scelta = Integer.parseInt(tastiera.nextLine()) - 1;

                EventiNBA e = game.getEvents().get(scelta);

                System.out.println("1 - " + e.home);
                System.out.println("2 - " + e.away);

                int pick = Integer.parseInt(tastiera.nextLine());

                double odd = (pick == 1) ? e.homeOdd : (e.awayOdd);

                game.addToSchedina(e, odd, pick);

                System.out.println("✔ aggiunto: " + e.home + " vs " + e.away);

                System.out.print("Aggiungere altra partita? (1 sì / 2 no): ");
                aggiungi = tastiera.nextLine().equals("1");
            }

            System.out.print("\n💰 Puntata schedina: ");
            double puntata = Double.parseDouble(tastiera.nextLine());

            game.playSchedina(puntata);

            System.out.println("\nContinuare? (1 sì / 2 no)");
            continua = tastiera.nextLine().equals("1");
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
}