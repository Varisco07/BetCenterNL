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

            int choice = -1;
            try {
                choice = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Inserisci un numero valido!");
                continue;
            }

            if (choice == 0) break;

            if (choice < 1 || choice > 6) {
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

            play(choice, bet);
        }
    }

    private void play(int choice, double bet) {

        if (!State.deductBalance(bet)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        int d1 = roll();
        int d2 = roll();
        int sum = d1 + d2;

        animate(d1, d2);

        System.out.println("🎲 Risultato: " + d1 + " + " + d2 + " = " + sum);

        boolean win = false;
        double mult = 1;

        switch (choice) {

            case 1: // Pass Line
                if (sum == 7 || sum == 11) win = true;
                if (sum == 2 || sum == 3 || sum == 12) win = false;
                mult = 1;
                break;

            case 2: // Don't Pass
                if (sum == 2 || sum == 3) win = true;
                if (sum == 7 || sum == 11) win = false;
                mult = 1;
                break;

            case 3: // Field Bet
                if (sum == 2 || sum == 3 || sum == 4 || sum == 9 || sum == 10 || sum == 11 || sum == 12)
                    win = true;
                mult = 1.5;
                break;

            case 4: // Any 7
                win = (sum == 7);
                mult = 4;
                break;

            case 5: // Hard 8
                win = (d1 == 4 && d2 == 4);
                mult = 9;
                break;

            case 6: // Hard 6
                win = (d1 == 3 && d2 == 3);
                mult = 9;
                break;
        }

        if (win) {
            double payout = bet * mult;
            State.addBalance(payout);
            System.out.println("🎉 VINTO: " + payout);
        } else {
            System.out.println("❌ PERSO");
        }

        System.out.println("💰 Saldo: " + State.getBalance());
        
        // Registra il risultato nel database
        core.User user = core.Auth.getCurrentUser();
        if (user != null) {
            double gain = win ? (bet * mult - bet) : -bet;
            core.GameRecord record = new core.GameRecord("Dadi", bet, gain, win);
            core.Database.recordGameResult(user.getId(), record);
        }
    }

    private int roll() {
        return random.randomInt(1, 6);
    }

    private void animate(int d1, int d2) {

        System.out.println("\n🎲 " + CYAN + "LANCIO DADI..." + RESET);

        for (int i = 0; i < 10; i++) {
            int a = roll();
            int b = roll();
            System.out.print("\r🎲 " + YELLOW + a + RESET + " | " + YELLOW + b + RESET);
            sleep(80 + i * 10);
        }

        System.out.print("\r🎲 " + BOLD + GREEN + d1 + RESET + " | " + BOLD + GREEN + d2 + RESET + "   \n");
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