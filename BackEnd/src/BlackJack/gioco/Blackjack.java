package BlackJack.gioco;

import BlackJack.modello.Carta;
import BlackJack.modello.MazzoCarte;
import BlackJack.giocatori.GiocatoreUmano;
import BlackJack.giocatori.Banco;
import java.util.Scanner;

public class Blackjack {

    // Codici colore ANSI per il terminale
    private static final String BIANCO   = "\u001B[37m";
    private static final String ROSSO    = "\u001B[31m";
    private static final String GIALLO   = "\u001B[33m";
    private static final String VERDE    = "\u001B[32m";
    private static final String BLU      = "\u001B[34m";
    private static final String ARANCIONE = "\u001B[38;5;208m";
    private static final String ROSA     = "\u001B[38;5;205m";
    private static final String RESET    = "\u001B[0m";

    private static final int CREDITI_INIZIALI = 1000;

    private static final int SOGLIA_CARTE_MAZZO = 15;

    private MazzoCarte mazzo;
    private GiocatoreUmano giocatore;
    private Banco banco;
    private Scanner tastiera;

    public Blackjack() {
        mazzo = new MazzoCarte();
        giocatore = new GiocatoreUmano("Giocatore", CREDITI_INIZIALI);
        banco = new Banco();
        tastiera = new Scanner(System.in);
    }


    public void avviaGioco() {
        stampaBenvenuto();
        RegoleGioco.stampaRegole();

        System.out.print("Inserisci il tuo nome: ");
        String nomeGiocatore = tastiera.nextLine();

        giocatore = new GiocatoreUmano(nomeGiocatore, CREDITI_INIZIALI);

        System.out.println();
        System.out.println(VERDE + "Benvenuto " + nomeGiocatore + "!" + RESET);
        System.out.println(GIALLO + "Inizi con " + CREDITI_INIZIALI + " crediti." + RESET);
        System.out.println();

        boolean vuoleContinuare = true;

        while (vuoleContinuare && !giocatore.haFinitoCrediti()) {
            RegoleGioco.stampaDisclaimer();
            giocaUnaMano();
            giocatore.stampaStatistiche();
            if (giocatore.haFinitoCrediti()) {
                System.out.println();
                System.out.println("Hai esaurito tutti i tuoi crediti!");
                System.out.println("La partita e' terminata.");
                vuoleContinuare = false;
            } else {
                vuoleContinuare = chiediSeVuoleContinuare();
            }
        }
        System.out.println();
        System.out.println(BIANCO + "=== STATISTICHE FINALI ===" + RESET);
        giocatore.stampaStatistiche();
        RegoleGioco.stampaMessaggioFinale();

    }
    private void giocaUnaMano() {
        if (mazzo.carteRimaste() < SOGLIA_CARTE_MAZZO) {
            System.out.println("Il mazzo sta per finire. Creo un nuovo mazzo...");
            mazzo.ricreaMazzo();
        }
        giocatore.svuotaMano();
        banco.svuotaMano();


        int puntata = chiediPuntata();

        System.out.println();
        System.out.println(BLU + "----------------------------------------------");
        System.out.println("  NUOVA MANO - Puntata: " + puntata + " crediti");
        System.out.println("----------------------------------------------" + RESET);
        System.out.println();
        aspetta(2000);
        System.out.println(BLU + "Distribuzione delle carte..." + RESET);
        System.out.println();

        // Prima carta al giocatore
        Carta carta1Giocatore = mazzo.pescaCarta();
        giocatore.aggiungiCarta(carta1Giocatore);
        System.out.println("Prima carta al giocatore: " + carta1Giocatore);
        aspetta(2000);

        // Prima carta al banco
        Carta carta1Banco = mazzo.pescaCarta();
        banco.aggiungiCarta(carta1Banco);
        System.out.println("Prima carta al banco: " + carta1Banco);
        aspetta(2000);

        // Seconda carta al giocatore
        Carta carta2Giocatore = mazzo.pescaCarta();
        giocatore.aggiungiCarta(carta2Giocatore);
        System.out.println("Seconda carta al giocatore: " + carta2Giocatore);
        aspetta(2000);

        // Seconda carta al banco
        Carta carta2Banco = mazzo.pescaCarta();
        banco.aggiungiCarta(carta2Banco);
        System.out.println("Seconda carta al banco (coperta al giocatore)");
        aspetta(2000);

        // Mostriamo le carte: quelle del giocatore tutte scoperte,
        // quelle del banco con una carta coperta
        giocatore.mostraMano();
        banco.mostraManoConCartaCoperta();
        aspetta(2000);

        // --- Fase 3: Controlliamo Blackjack naturale ---
        boolean giocatoreHaBlackjack = giocatore.haBlackjack();
        boolean bancoHaBlackjack = banco.haBlackjack();

        if (giocatoreHaBlackjack && bancoHaBlackjack) {
            System.out.println("*** Entrambi hanno BLACKJACK! Pareggio! ***");
            banco.mostraMano();
            giocatore.registraPareggio();
            return;
        }

        if (giocatoreHaBlackjack) {
            System.out.println("*** BLACKJACK! Hai vinto! ***");
            banco.mostraMano();
            int vincitaBlackjack = puntata + (puntata / 2); // Pagamento 3:2
            giocatore.aggiungiCrediti(vincitaBlackjack);
            giocatore.registraVittoria();
            System.out.println("Hai vinto " + vincitaBlackjack + " crediti!");
            return;
        }

        if (bancoHaBlackjack) {
            System.out.println("*** Il Banco ha BLACKJACK! Hai perso! ***");
            banco.mostraMano();
            giocatore.rimuoviCrediti(puntata);
            giocatore.registraSconfitta();
            System.out.println("Hai perso " + puntata + " crediti.");
            return;
        }

        // --- Fase 4: Turno del giocatore ---
        boolean turnoGiocatoreFinito = false;

        while (!turnoGiocatoreFinito) {
            System.out.println("Cosa vuoi fare?");
            System.out.println("  " + VERDE + "1 - Pescare (Hit)" + RESET);
            System.out.println("  " + GIALLO + "2 - Stare (Stand)" + RESET);
            System.out.println("  " + ROSA + "3 - Raddoppiare (Double)" + RESET);
            System.out.print("Scelta: ");

            String scelta = tastiera.nextLine();

            if (scelta.equals("1")) {
                Carta nuovaCarta = mazzo.pescaCarta();
                giocatore.aggiungiCarta(nuovaCarta);
                System.out.println();
                System.out.println("Hai pescato: " + nuovaCarta.toString());
                System.out.println();
                giocatore.mostraMano();
                aspetta(2000);

                // Controlliamo se ha sballato
                if (giocatore.haSballato()) {
                    System.out.println("*** HAI SBALLATO! Hai superato 21! ***");
                    turnoGiocatoreFinito = true;
                }

                // Controlliamo se ha raggiunto esattamente 21
                if (giocatore.valoreMano() == 21) {
                    System.out.println(VERDE + "Hai raggiunto 21! Ti fermi automaticamente." + RESET);
                    turnoGiocatoreFinito = true;
                }

            } else if (scelta.equals("2")) {
                System.out.println();
                System.out.println("Ti sei fermato con " + GIALLO + giocatore.valoreMano() + RESET + " punti.");
                aspetta(2000);
                turnoGiocatoreFinito = true;

            } else if (scelta.equals("3")) {
                if (giocatore.numeroCarte() != 2) {
                    System.out.println(ROSSO + "Puoi raddoppiare solo con le prime due carte." + RESET);
                    System.out.println();
                } else if (!giocatore.puoPuntare(puntata * 2)) {
                    System.out.println(ROSSO + "Non hai abbastanza crediti per raddoppiare la puntata." + RESET);
                    System.out.println();
                } else {
                    puntata = puntata * 2;
                    Carta nuovaCarta = mazzo.pescaCarta();
                    giocatore.aggiungiCarta(nuovaCarta);
                    System.out.println();
                    System.out.println(ROSA + "Hai scelto DOUBLE. Nuova puntata: " + puntata + " crediti." + RESET);
                    System.out.println("Hai pescato: " + nuovaCarta.toString());
                    System.out.println();
                    giocatore.mostraMano();
                    aspetta(2000);
                    turnoGiocatoreFinito = true;
                }

            } else {
                System.out.println("Scelta non valida! Inserisci 1, 2 o 3.");
            }
        }

        // --- Fase 5: Se il giocatore ha sballato, perde subito ---
        if (giocatore.haSballato()) {
            banco.mostraMano();
            giocatore.rimuoviCrediti(puntata);
            giocatore.registraSconfitta();
            System.out.println("Hai perso " + puntata + " crediti.");
            return;
        }

        // --- Fase 6: Turno del banco ---
        System.out.println();
        System.out.println(BLU + "--- Turno del Banco ---" + RESET);
        System.out.println();
        banco.mostraMano();
        aspetta(2000);

        while (banco.devePescare()) {
            Carta cartaBanco = mazzo.pescaCarta();
            banco.aggiungiCarta(cartaBanco);
            System.out.println("Il Banco pesca: " + cartaBanco.toString());
            System.out.println();
            banco.mostraMano();
            aspetta(1000);
        }

        // --- Fase 7: Determiniamo il vincitore ---
        System.out.println(BLU + "----------------------------------------------");
        System.out.println("          RISULTATO DELLA MANO");
        System.out.println("----------------------------------------------" + RESET);
        System.out.println();
        giocatore.mostraMano();
        banco.mostraMano();
        aspetta(2000);

        int punteggioGiocatore = giocatore.valoreMano();
        int punteggioBanco = banco.valoreMano();

        if (banco.haSballato()) {
            System.out.println(VERDE + "*** Il Banco ha sballato! HAI VINTO! ***" + RESET);
            giocatore.aggiungiCrediti(puntata);
            giocatore.registraVittoria();
            System.out.println("Hai vinto " + puntata + " crediti!");

        } else if (punteggioGiocatore > punteggioBanco) {
            System.out.println(VERDE + "*** HAI VINTO! " + punteggioGiocatore
                    + " contro " + punteggioBanco + " ***" + RESET);
            giocatore.aggiungiCrediti(puntata);
            giocatore.registraVittoria();
            System.out.println("Hai vinto " + puntata + " crediti!");

        } else if (punteggioGiocatore < punteggioBanco) {
            System.out.println(ROSSO + "*** HAI PERSO! " + punteggioGiocatore
                    + " contro " + punteggioBanco + " ***" + RESET);
            giocatore.rimuoviCrediti(puntata);
            giocatore.registraSconfitta();
            System.out.println("Hai perso " + puntata + " crediti.");

        } else {
            System.out.println(GIALLO + "*** PAREGGIO! Entrambi con " + punteggioGiocatore
                    + " punti ***" + RESET);
            System.out.println("La puntata ti viene restituita.");
            giocatore.registraPareggio();
        }
    }
    private int chiediPuntata() {
        int puntata = 0;
        boolean puntataValida = false;

        while (!puntataValida) {
            System.out.println("Crediti disponibili: " + giocatore.getCreditiAttuali());
            System.out.print("Quanto vuoi puntare? ");

            String input = tastiera.nextLine();

            // Proviamo a convertire l'input in un numero
            boolean inputValido = true;
            int numero = 0;

            // Controlliamo che l'input contenga solo cifre
            if (input.isEmpty()) {
                inputValido = false;
            } else {
                for (int i = 0; i < input.length(); i++) {
                    char carattere = input.charAt(i);
                    if (carattere < '0' || carattere > '9') {
                        inputValido = false;
                    }
                }
            }

            if (inputValido) {
                try {
                    numero = Integer.parseInt(input);
                } catch (NumberFormatException errore) {
                    inputValido = false;
                }
            }

            if (!inputValido) {
                System.out.println("Inserisci un numero valido!");
                System.out.println();
            } else if (numero <= 0) {
                System.out.println("La puntata deve essere maggiore di zero!");
                System.out.println();
            } else if (!giocatore.puoPuntare(numero)) {
                System.out.println("Non hai abbastanza crediti per questa puntata!");
                System.out.println("Crediti disponibili: " + giocatore.getCreditiAttuali());
                System.out.println();
            } else {
                puntata = numero;
                puntataValida = true;
            }
        }

        return puntata;
    }
    private boolean chiediSeVuoleContinuare() {
        while (true) {
            System.out.println();
            System.out.println("Vuoi giocare un'altra mano?");
            System.out.println("  1 - Si, continua");
            System.out.println("  2 - No, esci");
            System.out.print("Scelta: ");

            String scelta = tastiera.nextLine();

            if (scelta.equals("1")) {
                return true;
            } else if (scelta.equals("2")) {
                return false;
            } else {
                System.out.println("Scelta non valida! Inserisci 1 o 2.");
            }
        }
    }
    private void stampaBenvenuto() {
        System.out.println();
        System.out.println("==============================================");
        System.out.println("     ____  _            _    _            _   ");
        System.out.println("    | __ )| | __ _  ___| | _(_) __ _  ___| | __");
        System.out.println("    |  _ \\| |/ _` |/ __| |/ / |/ _` |/ __| |/ /");
        System.out.println("    | |_) | | (_| | (__|   <| | (_| | (__|   < ");
        System.out.println("    |____/|_|\\__,_|\\___|_|\\_\\ |\\__,_|\\___|_|\\_\\");
        System.out.println("                            _/ |               ");
        System.out.println("                           |__/                ");
        System.out.println("         GIOCO DIDATTICO DI BLACKJACK");
        System.out.println("==============================================");
        System.out.println();
    }
    private void aspetta(int millisecondi) {
        try {
            Thread.sleep(millisecondi);
        } catch (InterruptedException errore) {
        }
    }
}