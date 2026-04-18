package stats;

import core.*;
import java.util.*;
import java.util.stream.Collectors;

public class Leaderboard {
    
    public static class LeaderboardEntry {
        public String username;
        public String nome;
        public double balance;
        public int wins;
        public double totalGain;
        public int gamesPlayed;
        public int level;
        
        public LeaderboardEntry(User user) {
            this.username = user.getUsername();
            this.nome = user.getNome();
            this.balance = user.getSaldo();
            this.wins = user.getGiociVinti();
            this.totalGain = user.getGuadagnoTotale();
            this.gamesPlayed = user.getGiociGiocati();
            this.level = user.getCurrentLevel();
        }
    }
    
    public static void displayLeaderboard() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         🏆 CLASSIFICA TOP 10           ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        List<LeaderboardEntry> entries = Database.getAllUsers().stream()
            .map(LeaderboardEntry::new)
            .sorted(Comparator.comparingDouble(e -> -e.balance))
            .limit(10)
            .collect(Collectors.toList());
        
        String[] medals = {"🥇", "🥈", "🥉", "⭐", "⭐", "⭐", "⭐", "⭐", "⭐", "⭐"};
        
        for (int i = 0; i < entries.size(); i++) {
            LeaderboardEntry e = entries.get(i);
            System.out.printf("║ %s #%d %-15s €%.2f (Lv.%d)%n",
                medals[i],
                i + 1,
                e.username,
                e.balance,
                e.level);
        }
        
        System.out.println("╚════════════════════════════════════════╝\n");
    }
    
    public static void displayUserRank(User user) {
        List<LeaderboardEntry> entries = Database.getAllUsers().stream()
            .map(LeaderboardEntry::new)
            .sorted(Comparator.comparingDouble(e -> -e.balance))
            .collect(Collectors.toList());
        
        int rank = 0;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).username.equals(user.getUsername())) {
                rank = i + 1;
                break;
            }
        }
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         📊 LA TUA POSIZIONE            ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Posizione: #" + rank + " su " + entries.size());
        System.out.println("║ Saldo: €" + String.format("%.2f", user.getSaldo()));
        System.out.println("║ Vittorie: " + user.getGiociVinti());
        System.out.println("║ Partite: " + user.getGiociGiocati());
        System.out.println("║ Livello: " + user.getCurrentLevel() + " - " + user.getLevelName());
        System.out.println("║ XP: " + user.getXp());
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}
