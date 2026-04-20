package core;

import java.io.Serializable;
import java.time.LocalDateTime;

public class GameRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String game;
    private double bet;
    private double gain;
    private boolean win;
    private LocalDateTime timestamp;
    private String details;
    
    public GameRecord(String game, double bet, double gain, boolean win) {
        this.id = java.util.UUID.randomUUID().toString();
        this.game = game;
        this.bet = bet;
        this.gain = gain;
        this.win = win;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getId() { return id; }
    public String getGame() { return game; }
    public double getBet() { return bet; }
    public double getGain() { return gain; }
    public boolean isWin() { return win; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getDetails() { return details; }
    
    public void setDetails(String details) { this.details = details; }
    
    public int getXpGained() {
        if (win) return (int) (bet * 2 + gain * 0.5);
        return (int) (bet * 0.5);
    }
    
    @Override
    public String toString() {
        return String.format("[%s] %s - Puntata: €%.2f | Guadagno: €%.2f | %s",
                timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")),
                game,
                bet,
                gain,
                win ? "✅ VINTO" : "❌ PERSO");
    }
}
 