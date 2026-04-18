package chat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LiveFeed implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final List<FeedActivity> activities = new ArrayList<>();
    private static final int MAX_ACTIVITIES = 50;
    
    public static class FeedActivity implements Serializable {
        private static final long serialVersionUID = 1L;
        public String id;
        public String message;
        public String type; // "win", "bet", "info", "level"
        public LocalDateTime timestamp;
        
        public FeedActivity(String message, String type) {
            this.id = UUID.randomUUID().toString();
            this.message = message;
            this.type = type;
            this.timestamp = LocalDateTime.now();
        }
        
        @Override
        public String toString() {
            String time = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"));
            String icon = "";
            switch (type) {
                case "win": icon = "🏆"; break;
                case "bet": icon = "🎯"; break;
                case "level": icon = "⭐"; break;
                default: icon = "ℹ️";
            }
            return String.format("[%s] %s %s", time, icon, message);
        }
    }
    
    public static void addActivity(String message, String type) {
        FeedActivity activity = new FeedActivity(message, type);
        activities.add(0, activity);
        
        if (activities.size() > MAX_ACTIVITIES) {
            activities.remove(activities.size() - 1);
        }
    }
    
    public static void addWin(String username, String game, double amount) {
        addActivity(username + " ha vinto €" + String.format("%.2f", amount) + " a " + game + "!", "win");
    }
    
    public static void addBet(String username, String game, double amount) {
        addActivity(username + " ha scommesso €" + String.format("%.2f", amount) + " su " + game, "bet");
    }
    
    public static void addLevelUp(String username, int level) {
        addActivity(username + " ha raggiunto il Livello " + level + "!", "level");
    }
    
    public static void displayFeed(int limit) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         📡 LIVE FEED                   ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        int count = 0;
        for (FeedActivity activity : activities) {
            if (count >= limit) break;
            System.out.println("║ " + activity);
            count++;
        }
        
        System.out.println("╚════════════════════════════════════════╝\n");
    }
    
    public static List<FeedActivity> getActivities(int limit) {
        List<FeedActivity> result = new ArrayList<>(activities);
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        return result;
    }
}
