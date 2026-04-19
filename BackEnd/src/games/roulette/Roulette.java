package games.roulette;

import java.util.*;
import core.*;

public class Roulette {
    
    // ANSI Color codes
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String WHITE = "\u001B[37m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String BG_RED = "\u001B[41m";
    private static final String BG_BLACK = "\u001B[40m";
    private static final String BG_GREEN = "\u001B[42m";

    private ruotaRoulette wheel = new ruotaRoulette();
    private List<Bet> bets = new ArrayList<>();

    public void addBet(Bet bet) {
        bets.add(bet);
    }

    public void spin() {
        if (bets.isEmpty()) {
            System.out.println("❌ Nessuna puntata piazzata!");
            return;
        }

        double total = bets.stream().mapToDouble(b -> b.amount).sum();

        if (!State.deductBalance(total)) {
            System.out.println("❌ Saldo insufficiente per le puntate!");
            return;
        }

        // Animazione spin
        System.out.println("\n🎰 La ruota sta girando...");
        animateSpinning();

        int result = wheel.spin();
        String color = wheel.getColor(result);

        // Mostra risultato con stile
        displayResult(result, color);

        double totalWin = 0;
        double totalBet = 0;

        System.out.println("\n📊 RISULTATI PUNTATE:");
        System.out.println("┌─────────────────────────────────────────┐");

        for (Bet bet : bets) {
            boolean vinto = checkWin(bet, result, color);
            double winAmount = vinto ? bet.amount * bet.odds : 0;
            totalWin += winAmount;
            totalBet += bet.amount;

            String status = vinto ? "✅ VINTA" : "❌ PERSA";
            String betDesc = getBetDescription(bet);
            
            System.out.printf("│ %-20s €%-6.2f %s%n", betDesc, bet.amount, status);
            if (vinto) {
                System.out.printf("│   → Vincita: €%.2f (%.1fx)%n", winAmount, bet.odds);
            }
        }

        System.out.println("├─────────────────────────────────────────┤");
        System.out.printf("│ TOTALE PUNTATO: " + CYAN + "€%.2f" + RESET + "%n", totalBet);
        System.out.printf("│ TOTALE VINTO: " + (totalWin > 0 ? GREEN : RED) + "€%.2f" + RESET + "%n", totalWin);
        
        double netGain = totalWin - totalBet;
        if (netGain > 0) {
            State.addBalance(totalWin);
            System.out.printf("│ " + GREEN + BOLD + "🎉 GUADAGNO NETTO: +€%.2f" + RESET + "%n", netGain);
        } else {
            System.out.printf("│ " + RED + "💸 PERDITA NETTA: €%.2f" + RESET + "%n", Math.abs(netGain));
        }
        
        System.out.printf("│ " + CYAN + "💰 SALDO ATTUALE: €%.2f" + RESET + "%n", State.getBalance());
        System.out.println("└─────────────────────────────────────────┘");

        // Registra il risultato nel database
        core.User user = core.Auth.getCurrentUser();
        if (user != null) {
            netGain = totalWin - totalBet;
            boolean win = netGain > 0;
            core.GameRecord record = new core.GameRecord("Roulette", totalBet, netGain, win);
            core.Database.recordGameResult(user.getId(), record);
        }

        bets.clear();
    }

    private void animateSpinning() {
        String[] frames = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
        try {
            for (int i = 0; i < 20; i++) {
                System.out.print("\r" + CYAN + frames[i % frames.length] + " Girando..." + RESET);
                Thread.sleep(100);
            }
            System.out.print("\r");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void displayResult(int number, String color) {
        String colorDisplay = "";
        String bgColor = "";
        
        if (color.equals("red")) {
            colorDisplay = RED + "ROSSO" + RESET;
            bgColor = BG_RED + WHITE + BOLD;
        } else if (color.equals("black")) {
            colorDisplay = "NERO";
            bgColor = BG_BLACK + WHITE + BOLD;
        } else {
            colorDisplay = GREEN + "VERDE" + RESET;
            bgColor = BG_GREEN + WHITE + BOLD;
        }
        
        String border = "═".repeat(25);
        
        System.out.println("\n╔" + border + "╗");
        System.out.printf("║     🎯 RISULTATO: " + bgColor + " %2d " + RESET + "     ║%n", number);
        System.out.printf("║        (%s)        ║%n", colorDisplay);
        System.out.println("╚" + border + "╝");
        
        // Mostra info aggiuntive sul numero
        if (number == 0) {
            System.out.println(YELLOW + "🍀 ZERO! La casa vince su pari/dispari, rosso/nero" + RESET);
        } else {
            String parity = (number % 2 == 0) ? CYAN + "PARI" + RESET : MAGENTA + "DISPARI" + RESET;
            String range = (number <= 18) ? "BASSO (1-18)" : "ALTO (19-36)";
            String dozen = getDozens(number);
            String column = getColumn(number);
            
            System.out.println(CYAN + "📋 Proprietà del numero:" + RESET);
            System.out.println("   • " + parity + " • " + range);
            System.out.println("   • " + dozen + " • " + column);
        }
    }

    private String getColorEmoji(String color) {
        switch (color) {
            case "red": return "🔴";
            case "black": return "⚫";
            case "green": return "🟢";
            default: return "⚪";
        }
    }

    private String getDozens(int number) {
        if (number >= 1 && number <= 12) return "1ª DOZZINA (1-12)";
        if (number >= 13 && number <= 24) return "2ª DOZZINA (13-24)";
        if (number >= 25 && number <= 36) return "3ª DOZZINA (25-36)";
        return "";
    }

    private String getColumn(int number) {
        if (number % 3 == 1) return "1ª COLONNA";
        if (number % 3 == 2) return "2ª COLONNA";
        if (number % 3 == 0) return "3ª COLONNA";
        return "";
    }

    private boolean checkWin(Bet bet, int result, String color) {
        switch (bet.type.toLowerCase()) {
            case "number": 
                return bet.number == result;
            case "red": 
                return color.equals("red");
            case "black": 
                return color.equals("black");
            case "even": 
                return result > 0 && result % 2 == 0;
            case "odd": 
                return result > 0 && result % 2 != 0;
            case "low": 
                return result >= 1 && result <= 18;
            case "high": 
                return result >= 19 && result <= 36;
            case "dozen1": 
                return result >= 1 && result <= 12;
            case "dozen2": 
                return result >= 13 && result <= 24;
            case "dozen3": 
                return result >= 25 && result <= 36;
            case "column1": 
                return result > 0 && result % 3 == 1;
            case "column2": 
                return result > 0 && result % 3 == 2;
            case "column3": 
                return result > 0 && result % 3 == 0;
            default: 
                return false;
        }
    }

    private String getBetDescription(Bet bet) {
        switch (bet.type.toLowerCase()) {
            case "number": return "Numero " + bet.number;
            case "red": return "Rosso";
            case "black": return "Nero";
            case "even": return "Pari";
            case "odd": return "Dispari";
            case "low": return "Basso (1-18)";
            case "high": return "Alto (19-36)";
            case "dozen1": return "1ª Dozzina";
            case "dozen2": return "2ª Dozzina";
            case "dozen3": return "3ª Dozzina";
            case "column1": return "1ª Colonna";
            case "column2": return "2ª Colonna";
            case "column3": return "3ª Colonna";
            default: return bet.type;
        }
    }

    public void showBettingOptions() {
        System.out.println("\n🎯 OPZIONI DI PUNTATA ROULETTE:");
        System.out.println("┌─────────────────────────────────────────┐");
        System.out.println("│ PUNTATE SEMPLICI (Payout 1:1)          │");
        System.out.println("│ • Rosso/Nero • Pari/Dispari            │");
        System.out.println("│ • Basso (1-18) • Alto (19-36)          │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ DOZZINE E COLONNE (Payout 2:1)         │");
        System.out.println("│ • 1ª/2ª/3ª Dozzina • 1ª/2ª/3ª Colonna │");
        System.out.println("├─────────────────────────────────────────┤");
        System.out.println("│ NUMERO SINGOLO (Payout 35:1)           │");
        System.out.println("│ • Qualsiasi numero da 0 a 36           │");
        System.out.println("└─────────────────────────────────────────┘");
    }
}