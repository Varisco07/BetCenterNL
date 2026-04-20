package games.dadi;

import core.*;

import java.util.Scanner;

public class dadi {
    
    // ANSI Color codes
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private final Scanner sc = new Scanner(System.in);

    public void start() {
        
        printGameRules();

        System.out.println("\n🎲 " + BOLD + "CRAPS / DADI" + RESET);
        System.out.println("💰 Saldo: " + GREEN + State.getBalance() + RESET);

        while (true) {

            System.out.println("\nScegli scommessa:");
            System.out.println("1 - Pass Line (1x)");
            System.out.println("2 - Don't Pass (1x)");
            System.out.println("3 - Field Bet (1.5x)");
            System.out.println("4 - Any Seven (4x)");
            System.out.println("5 - Hardway 8 (9x)");
            System.out.println("6 - Hardway 6 (9x)");
            System.out.println("0 - Esci");

            int scelta = -1;
            try {
                scelta = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            if (scelta == 0) break;

            if (scelta < 1 || scelta > 6) {
                System.out.println("❌ Scelta non valida! Scegli tra 1 e 6.");
                continue;
            }

            System.out.print("Puntata: ");
            double bet = 0;
            try {
                bet = Double.parseDouble(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            if (bet <= 0) {
                System.out.println("❌ La puntata deve essere positiva!");
                continue;
            }

            play(scelta, bet);
        }
    }

    private void play(int scelta, double bet) {

        if (!State.deductBalance(bet)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        int dado1 = roll();
        int dado2 = roll();
        int somma = dado1 + dado2;

        animate(dado1, dado2);

        System.out.println("🎲 Risultato: " + dado1 + " + " + dado2 + " = " + somma);

        boolean haVinto = false;
        double moltiplicatore = 1;

        switch (scelta) {

            case 1: // Pass Line
                if (somma == 7 || somma == 11) haVinto = true;
                if (somma == 2 || somma == 3 || somma == 12) haVinto = false;
                moltiplicatore = 1;
                break;

            case 2: // Don't Pass
                if (somma == 2 || somma == 3) haVinto = true;
                if (somma == 7 || somma == 11) haVinto = false;
                moltiplicatore = 1;
                break;

            case 3: // Field Bet
                if (somma == 2 || somma == 3 || somma == 4 || somma == 9 || somma == 10 || somma == 11 || somma == 12)
                    haVinto = true;
                moltiplicatore = 1.5;
                break;

            case 4: // Any 7
                haVinto = (somma == 7);
                moltiplicatore = 4;
                break;

            case 5: // Hard 8
                haVinto = (dado1 == 4 && dado2 == 4);
                moltiplicatore = 9;
                break;

            case 6: // Hard 6
                haVinto = (dado1 == 3 && dado2 == 3);
                moltiplicatore = 9;
                break;
        }

        if (haVinto) {
            double pagamento = bet * moltiplicatore;
            State.addBalance(pagamento);
            System.out.println("🎉 VINTO: " + pagamento);
        } else {
            System.out.println("❌ PERSO");
        }

        System.out.println("💰 Saldo: " + State.getBalance());
        
        // Registra il risultato nel database
        core.User utente = core.Auth.getCurrentUser();
        if (utente != null) {
            double guadagno = haVinto ? (bet * moltiplicatore - bet) : -bet;
            core.GameRecord registrazione = new core.GameRecord("Dadi", bet, guadagno, haVinto);
            core.Database.recordGameResult(utente.getId(), registrazione);
        }
    }

    private int roll() {
        return random.randomInt(1, 6);
    }

    private void animate(int dado1, int dado2) {

        System.out.println("\n🎲 " + CYAN + "LANCIO DADI..." + RESET);

        for (int i = 0; i < 10; i++) {
            int a = roll();
            int b = roll();
            System.out.print("\r🎲 " + YELLOW + a + RESET + " | " + YELLOW + b + RESET);
            sleep(80 + i * 10);
        }

        System.out.print("\r🎲 " + BOLD + GREEN + dado1 + RESET + " | " + BOLD + GREEN + dado2 + RESET + "   \n");
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
    
    public static void printGameRules() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║              " + CYAN + BOLD + "🎲 CRAPS - REGOLE DEL GIOCO" + RESET + "                   ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Scegli il tipo di scommessa                            ║");
        System.out.println("║  2. Piazza la tua puntata                                  ║");
        System.out.println("║  3. Lancia i dadi (2 dadi a 6 facce)                       ║");
        System.out.println("║  4. Vinci in base al risultato e alla scommessa            ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "TIPI DI SCOMMESSE:" + RESET + "                                        ║");
        System.out.println("║  • Pass Line (1x)      → Vinci con 7 o 11                  ║");
        System.out.println("║  • Don't Pass (1x)     → Vinci con 2 o 3                   ║");
        System.out.println("║  • Field Bet (1.5x)    → Vinci con 2,3,4,9,10,11,12        ║");
        System.out.println("║  • Any Seven (4x)      → Vinci se esce 7                   ║");
        System.out.println("║  • Hardway 8 (9x)      → Vinci con doppio 4 (4+4)          ║");
        System.out.println("║  • Hardway 6 (9x)      → Vinci con doppio 3 (3+3)          ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "PROBABILITÀ:" + RESET + "                                              ║");
        System.out.println("║  • 7 è il numero più probabile (16.67%)                    ║");
        System.out.println("║  • 2 e 12 sono i meno probabili (2.78%)                    ║");
        System.out.println("║  • Hardway: probabilità bassa, payout alto                 ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "⚠️  GIOCO RESPONSABILE:" + RESET + "                                   ║");
        System.out.println("║  • Imposta limiti di spesa prima di iniziare               ║");
        System.out.println("║  • Non giocare con soldi che non puoi perdere              ║");
        System.out.println("║  • Fai pause regolari durante il gioco                     ║");
        System.out.println("║  • Cerca supporto se il gioco diventa problematico         ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}