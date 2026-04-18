package games.virtual;

import core.State;
import java.util.*;

public class VirtualMain {

    public static void main(String[] args) {
        new VirtualMain().avviaGioco();
    }

    private Scanner tastiera = new Scanner(System.in);
    private VirtualCalcio game = new VirtualCalcio();

    public void avviaGioco() {

        stampaBenvenuto();

        game.generateEvents();

        while (State.getBalance() > 0) {

            mostraEventi();

            scegliScommesse();   // ora crea la SCHEDINA

            double puntata = chiediPuntata();

            giocaRound(puntata);

            if (!chiediContinua()) break;

            game.generateEvents(); // nuovi eventi ogni giro
        }

        System.out.println("\n💰 Saldo finale: " + State.getBalance());
    }

    private void mostraEventi() {

        System.out.println("\n================== EVENTI ==================");

        int i = 1;
        for (Eventi e : game.getEvents()) {
            System.out.println(i + ") " + e.league + " - " + e.home + " vs " + e.away);
            System.out.println("   1:" + e.homeOdd + "  X:" + e.drawOdd + "  2:" + e.awayOdd);
            i++;
        }
    }


    private void scegliScommesse() {

        System.out.println("\n🎫 CREA SCHEDINA");

        game.clearBets(); // IMPORTANTISSIMO

        boolean continua = true;

        while (continua) {

            System.out.print("\nScegli evento (numero): ");
            int scelta = -1;
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

            Eventi e = game.getEvents().get(scelta);

            System.out.println("1 - " + e.home);
            System.out.println("2 - Pareggio");
            System.out.println("3 - " + e.away);

            int tipo = -1;
            try {
                tipo = Integer.parseInt(tastiera.nextLine());
            } catch (NumberFormatException ex) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            switch (tipo) {
                case 1:
                    game.addBet(e.homeOdd);
                    System.out.println("✔ aggiunto: " + e.home);
                    break;

                case 2:
                    game.addBet(e.drawOdd);
                    System.out.println("✔ aggiunto: X");
                    break;

                case 3:
                    game.addBet(e.awayOdd);
                    System.out.println("✔ aggiunto: " + e.away);
                    break;

                default:
                    System.out.println("❌ Scelta non valida! Scegli tra 1 e 3.");
                    continue;
            }

            System.out.print("Aggiungere altra partita? (1 sì / 2 no): ");
            String risposta = tastiera.nextLine();
            continua = risposta.equals("1");
        }
    }

    private void giocaRound(double puntata) {

        System.out.println("\n⚽ Simulazione partite...");
        aspetta(1500);

        game.play(puntata);

        aspetta(1000);
    }

    private double chiediPuntata() {

        while (true) {
            System.out.print("\n💰 Puntata schedina: ");

            try {
                double p = Double.parseDouble(tastiera.nextLine());

                if (p <= 0) {
                    System.out.println("❌ La puntata deve essere positiva!");
                } else {
                    return p;
                }

            } catch (Exception e) {
                System.out.println("❌ Numero non valido!");
            }
        }
    }

    private boolean chiediContinua() {

        System.out.println("\nContinuare?");
        System.out.println("1 - Si");
        System.out.println("2 - No");

        return tastiera.nextLine().equals("1");
    }

    private void stampaBenvenuto() {
        System.out.println("=================================");
        System.out.println("      ⚽ VIRTUAL SPORTS");
        System.out.println("=================================");
    }

    private void aspetta(int ms) {
        try { Thread.sleep(ms); } catch (Exception e) {}
    }
}