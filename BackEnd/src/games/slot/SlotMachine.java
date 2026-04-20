package games.slot;

import core.*;
import core.State;
import core.Auth;
import core.User;
import core.Database;
import core.GameRecord;
import core.random;
import java.util.*;

public class SlotMachine {
    
    // ANSI Color codes
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";

    private static final List<String> SYMBOLS = List.of(
            "🍒", "🍋", "🍊", "🍇", "⭐", "💎", "7️⃣", "🔔"
    );

    public void spin(double bet) {

        if (!State.deductBalance(bet)) {
            System.out.println(RED + "Saldo insufficiente!" + RESET);
            return;
        }

        System.out.println("\n" + CYAN + BOLD + "🎰 SPINNING..." + RESET + "\n");

        String[] risultato = new String[3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 10; j++) {
                risultato[i] = randomSymbol();
                printRow(risultato);
                sleep(80 + j * 10);
            }
            sleep(300);
        }

        boolean isJackpot = risultato[0].equals("🔔") && risultato[1].equals("🔔") && risultato[2].equals("🔔");
        double vincita;

        if (isJackpot) {
            // Vinci il jackpot progressivo!
            vincita = State.getJackpot();
            State.addBalance(vincita);
            State.resetJackpot();
        } else {
            vincita = calculateWin(risultato[0], risultato[1], risultato[2], bet);
            if (vincita > 0) {
                State.addBalance(vincita);
            }
            // Ogni spin contribuisce al jackpot
            State.addToJackpot(bet);
        }

        System.out.println("\n" + YELLOW + "--------------------" + RESET);
        System.out.println(BOLD + "🎰 RISULTATO FINALE:" + RESET);
        printRow(risultato);
        System.out.println("\n" + YELLOW + "--------------------" + RESET);

        if (isJackpot) {
            System.out.println(YELLOW + BOLD + "🏆🔔🔔🔔 JACKPOT! HAI VINTO €" + String.format("%.2f", vincita) + "! 🔔🔔🔔🏆" + RESET);
        } else if (vincita > 0) {
            System.out.println(GREEN + BOLD + "🎉 VINTO: €" + String.format("%.2f", vincita) + RESET);
        } else {
            System.out.println(RED + "❌ PERSO" + RESET);
        }

        System.out.printf(CYAN + "💰 Saldo: €%.2f" + RESET + "%n", State.getBalance());
        System.out.printf(YELLOW + "🏆 Jackpot attuale: €%.2f" + RESET + "%n", State.getJackpot());

        // Registra il risultato nel database
        User utente = Auth.getCurrentUser();
        if (utente != null) {
            double guadagno = vincita > 0 ? (vincita - bet) : -bet;
            boolean haVinto = vincita > 0;
            GameRecord registrazione = new GameRecord("Slot Machine", bet, guadagno, haVinto);
            Database.recordGameResult(utente.getId(), registrazione);
        }
    }

    private void printRow(String[] risultato) {
        System.out.print("\r");
        System.out.print(BOLD + "🎰 " + YELLOW + risultato[0] + RESET + " | " + YELLOW + risultato[1] + RESET + " | " + YELLOW + risultato[2] + RESET);
        System.out.flush();
    }

    private String randomSymbol() {
        return SYMBOLS.get(random.randomInt(0, SYMBOLS.size() - 1));
    }

    private double calculateWin(String a, String b, String c, double bet) {

        if (a.equals(b) && b.equals(c)) {

            switch (a) {
                case "🍒": return bet * 5;
                case "🍋": return bet * 8;
                case "🍊": return bet * 10;
                case "🍇": return bet * 15;
                case "⭐": return bet * 25;
                case "💎": return bet * 50;
                case "7️⃣": return bet * 100;
                case "🔔": return bet * 200;
            }
        }

        if ((a.equals("🍒") && b.equals("🍒")) ||
                (a.equals("🍒") && c.equals("🍒")) ||
                (b.equals("🍒") && c.equals("🍒"))) {
            return bet * 1.5;
        }

        return 0;
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (Exception ignored) {}
    }
}