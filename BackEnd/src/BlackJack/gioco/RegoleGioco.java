package BlackJack.gioco;

public class RegoleGioco {
    public static void stampaRegole() {
        System.out.println("\u001B[34m==============================================");
        System.out.println("         REGOLE DEL BLACKJACK");
        System.out.println("==============================================\u001B[0m");
        System.out.println();
        System.out.println("- L'obiettivo e' arrivare il piu' vicino possibile a 21");
        System.out.println("  senza superarlo.");
        System.out.println();
        System.out.println("- Le carte numeriche (2-10) valgono il loro valore.");
        System.out.println("- Le figure (Jack, Queen, King) valgono 10 punti.");
        System.out.println("- L'Asso vale 11 punti, oppure 1 punto se si supera 21.");
        System.out.println();
        System.out.println("- All'inizio vengono distribuite 2 carte al giocatore");
        System.out.println("  e 2 carte al banco (di cui una coperta).");
        System.out.println();
        System.out.println("- Il giocatore puo' scegliere di:");
        System.out.println("  * PESCARE (Hit): prendere un'altra carta");
        System.out.println("  * STARE (Stand): fermarsi con le carte attuali");
        System.out.println();
        System.out.println("- Se il giocatore supera 21, SBALLA e perde la mano.");
        System.out.println();
        System.out.println("- Dopo il giocatore, il banco gioca automaticamente:");
        System.out.println("  il banco DEVE pescare finche' non raggiunge almeno 17.");
        System.out.println();
        System.out.println("- Vince chi ha il punteggio piu' alto senza superare 21.");
        System.out.println("- In caso di pareggio, la mano e' un pareggio (push)");
        System.out.println("  e la puntata viene restituita.");
        System.out.println();
        System.out.println("- Il Blackjack naturale (Asso + carta da 10 nelle prime");
        System.out.println("  2 carte) batte qualsiasi altro 21.");
        System.out.println();
        System.out.println("==============================================");
        System.out.println();
    }

    public static void stampaDisclaimer() {
        System.out.println();
        System.out.println("\u001B[31m**********************************************");
        System.out.println("* ATTENZIONE: Questo e' solo un gioco        *");
        System.out.println("* didattico. Il gioco d'azzardo puo' creare  *");
        System.out.println("* dipendenza. Non giocare mai con soldi      *");
        System.out.println("* reali.                                     *");
        System.out.println("**********************************************\u001B[0m");
        System.out.println();
    }

    public static void stampaMessaggioFinale() {
        System.out.println();
        System.out.println("\u001B[34m==============================================");
        System.out.println("      Hai terminato la partita.\u001B[0m");
        System.out.println();
        System.out.println(" Questo esempio mostra come nel gioco");
        System.out.println(" d'azzardo alla lunga si tende a perdere");
        System.out.println(" denaro.");
        System.out.println();
        System.out.println(" Per questo motivo e' importante non giocare");
        System.out.println(" con soldi reali.");
        System.out.println();
        System.out.println(" Il blackjack e gli altri giochi d'azzardo");
        System.out.println(" possono creare dipendenza e portare a");
        System.out.println(" perdere grandi quantita' di denaro.");
        System.out.println("==============================================");
        System.out.println();
    }
}