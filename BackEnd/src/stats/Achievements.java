package stats;

import core.*;
import java.util.*;

public class Achievements {
    
    public static class Achievement {
        public String id;
        public String name;
        public String icon;
        public String description;
        
        public Achievement(String id, String name, String icon, String description) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.description = description;
        }
    }
    
    private static final List<Achievement> ALL_ACHIEVEMENTS = Arrays.asList(
        new Achievement("first_win", "Prima Vittoria", "🎉", "Vinci la tua prima scommessa"),
        new Achievement("high_roller", "High Roller", "💸", "Scommetti €100 in una mano"),
        new Achievement("streak_3", "Tre di Fila", "🔥", "Vinci 3 scommesse consecutive"),
        new Achievement("blackjack", "Blackjack!", "🃏", "Fai un blackjack naturale"),
        new Achievement("jackpot", "Jackpottaro", "🎰", "Vinci più di €500 alle slot"),
        new Achievement("poker_royal", "Royal Flush", "♠", "Ottieni un Royal Flush al poker"),
        new Achievement("multi_bet", "Combinatore", "🎲", "Vinci una multi-scommessa"),
        new Achievement("total_100", "Centurione", "💯", "Gioca 100 scommesse totali"),
        new Achievement("balance_5000", "Ricco Sfondato", "🤑", "Raggiungi €5.000 di saldo"),
        new Achievement("zero_hero", "Zero Hero", "🎰", "Indovina lo zero alla roulette"),
        new Achievement("race_winner", "Appassionato di Gare", "🏁", "Vinci una corsa di cavalli"),
        new Achievement("baccarat_tie", "Puntatore Audace", "💎", "Vinci puntando sul pareggio al baccarat")
    );
    
    public static void checkAndUnlock(User user) {
        List<GameRecord> history = Database.getGameHistory(user.getId());
        
        // First win
        if (history.stream().anyMatch(GameRecord::isWin)) {
            user.addAchievement("first_win");
        }
        
        // High roller
        if (history.stream().anyMatch(h -> h.getBet() >= 100)) {
            user.addAchievement("high_roller");
        }
        
        // Streak 3
        int streak = 0;
        for (GameRecord h : history) {
            if (h.isWin()) {
                streak++;
                if (streak >= 3) {
                    user.addAchievement("streak_3");
                    break;
                }
            } else {
                streak = 0;
            }
        }
        
        // Jackpot
        if (history.stream().anyMatch(h -> h.getGame().equals("Slot Machine") && h.getGain() > 500)) {
            user.addAchievement("jackpot");
        }
        
        // Total 100
        if (history.size() >= 100) {
            user.addAchievement("total_100");
        }
        
        // Balance 5000
        if (user.getSaldo() >= 5000) {
            user.addAchievement("balance_5000");
        }
        
        Database.saveUsers();
    }
    
    public static void displayAchievements(User user) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         🏅 TRAGUARDI SBLOCCATI         ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        List<String> unlockedIds = user.getAchievements();
        int count = 0;
        
        for (Achievement ach : ALL_ACHIEVEMENTS) {
            if (unlockedIds.contains(ach.id)) {
                System.out.println("║ ✅ " + ach.icon + " " + ach.name);
                System.out.println("║    " + ach.description);
                count++;
            }
        }
        
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Totale: " + count + "/" + ALL_ACHIEVEMENTS.size());
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}
