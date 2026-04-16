package games.slot;

import core.State;
import core.random;
import java.util.*;

public class SlotMachine {

    private static final List<String> SYMBOLS = List.of(
            "🍒", "🍋", "🍊", "🍇", "⭐", "💎", "7️⃣", "🔔"
    );

    public void spin(double bet) {

        if (!State.deductBalance(bet)) {
            System.out.println("Saldo insufficiente!");
            return;
        }

        System.out.println("\n🎰 SPINNING...\n");

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

        System.out.println("\n--------------------");
        System.out.println("🎰 RISULTATO FINALE:");
        printRow(result);
        System.out.println("--------------------");

        if (win > 0) {
            State.addBalance(win);
            System.out.println("🎉 VINTO: " + win);
        } else {
            System.out.println("❌ PERSO");
        }

        System.out.println("💰 Saldo: " + State.getBalance());
    }

    private void printRow(String[] r) {
        System.out.print("\r");
        System.out.print("🎰 " + r[0] + " | " + r[1] + " | " + r[2]);
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