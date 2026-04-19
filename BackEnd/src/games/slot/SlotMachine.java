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

        String[] result = new String[3];

        for (int i = 0; i < 3; i++) {

            for (int j = 0; j < 10; j++) {
                result[i] = randomSymbol();
                printRow(result);
                sleep(80 + j * 10);
            }

            sleep(300);
        }

        double win = calculateWin(result[0], result[1], result[2], bet);

        System.out.println("\n" + YELLOW + "--------------------" + RESET);
        System.out.println(BOLD + "🎰 RISULTATO FINALE:" + RESET);
        printRow(result);
        System.out.println("\n" + YELLOW + "--------------------" + RESET);

        if (win > 0) {
            State.addBalance(win);
            System.out.println(GREEN + BOLD + "🎉 VINTO: " + win + RESET);
        } else {
            System.out.println(RED + "❌ PERSO" + RESET);
        }

        System.out.println(CYAN + "💰 Saldo: " + State.getBalance() + RESET);
        
        // Registra il risultato nel database
        User user = Auth.getCurrentUser();
        if (user != null) {
            double gain = win > 0 ? (win - bet) : -bet;
            boolean winFlag = win > 0;
            GameRecord record = new GameRecord("Slot Machine", bet, gain, winFlag);
            Database.recordGameResult(user.getId(), record);
        }
    }

    private void printRow(String[] r) {
        System.out.print("\r");
        System.out.print(BOLD + "🎰 " + YELLOW + r[0] + RESET + " | " + YELLOW + r[1] + RESET + " | " + YELLOW + r[2] + RESET);
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