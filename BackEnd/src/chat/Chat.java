package chat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Chat implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final List<ChatMessage> messages = new ArrayList<>();
    private static final int MAX_MESSAGES = 500;
    
    public static class ChatMessage implements Serializable {
        private static final long serialVersionUID = 1L;
        public String id;
        public String username;
        public String text;
        public LocalDateTime timestamp;
        
        public ChatMessage(String username, String text) {
            this.id = UUID.randomUUID().toString();
            this.username = username;
            this.text = text;
            this.timestamp = LocalDateTime.now();
        }
        
        @Override
        public String toString() {
            String time = timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            return String.format("[%s] %s: %s", time, username, text);
        }
    }
    
    public static void sendMessage(String username, String text) {
        if (text == null || text.trim().isEmpty() || text.length() > 500) {
            System.out.println("❌ Messaggio non valido!");
            return;
        }
        
        ChatMessage msg = new ChatMessage(username, text.trim());
        messages.add(0, msg);
        
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(messages.size() - 1);
        }
    }
    
    public static void displayChat(int limit) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           💬 CHAT GLOBALE             ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        List<ChatMessage> toDisplay = new ArrayList<>(messages);
        Collections.reverse(toDisplay);
        
        int count = 0;
        for (ChatMessage msg : toDisplay) {
            if (count >= limit) break;
            System.out.println("║ " + msg);
            count++;
        }
        
        System.out.println("╚════════════════════════════════════════╝\n");
    }
    
    public static List<ChatMessage> getMessages(int limit) {
        List<ChatMessage> result = new ArrayList<>(messages);
        Collections.reverse(result);
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }
        return result;
    }
}
