package core;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String nome;
    private String cognome;
    private String username;
    private String email;
    private String password;
    private String dob;
    private double saldo;
    private int xp;
    private LocalDateTime createdAt;
    private LocalDateTime lastBonusDate;
    private int bonusStreak;
    
    // Statistiche
    private int giociGiocati;
    private int giociVinti;
    private int giociPersi;
    private double guadagnoTotale;
    private List<String> achievements;
    private List<GameRecord> history;
    
    public User(String nome, String cognome, String username, String email, String password, String dob) {
        this.id = UUID.randomUUID().toString();
        this.nome = nome;
        this.cognome = cognome;
        this.username = username;
        this.email = email;
        this.password = password;
        this.dob = dob;
        this.saldo = 1000.0;
        this.xp = 0;
        this.createdAt = LocalDateTime.now();
        this.bonusStreak = 0;
        this.giociGiocati = 0;
        this.giociVinti = 0;
        this.giociPersi = 0;
        this.guadagnoTotale = 0.0;
        this.achievements = new ArrayList<>();
        this.history = new ArrayList<>();
    }
    
    // Getters e Setters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getDob() { return dob; }
    public double getSaldo() { return saldo; }
    public int getXp() { return xp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastBonusDate() { return lastBonusDate; }
    public int getBonusStreak() { return bonusStreak; }
    public int getGiociGiocati() { return giociGiocati; }
    public int getGiociVinti() { return giociVinti; }
    public int getGiociPersi() { return giociPersi; }
    public double getGuadagnoTotale() { return guadagnoTotale; }
    public List<String> getAchievements() { return achievements; }
    public List<GameRecord> getHistory() { return history; }
    
    public void setSaldo(double saldo) { this.saldo = Math.max(0, saldo); }
    public void setXp(int xp) { this.xp = xp; }
    public void setLastBonusDate(LocalDateTime date) { this.lastBonusDate = date; }
    public void setBonusStreak(int streak) { this.bonusStreak = streak; }
    
    // Metodi per statistiche
    // FIX: addGameRecord aggiungeva il record sia qui (user.history in users.dat) che in
    // Database.gameHistory (games.dat), causando duplicazione e possibile divergenza dei dati.
    // Ora updateStats aggiorna solo i contatori/xp; la history è gestita esclusivamente da Database.
    public void updateStats(GameRecord record) {
        giociGiocati++;
        if (record.isWin()) giociVinti++;
        else giociPersi++;
        guadagnoTotale += record.getGain();
        xp += record.getXpGained();
    }

    // Mantenuto per retrocompatibilità con codice che lo chiama direttamente fuori da Database,
    // ma non aggiunge più alla history per evitare duplicazione.
    /** @deprecated Usare {@link #updateStats(GameRecord)} — la history è in Database.getGameHistory() */
    @Deprecated
    public void addGameRecord(GameRecord record) {
        updateStats(record);
    }
    
    public double getWinRate() {
        if (giociGiocati == 0) return 0;
        return (double) giociVinti / giociGiocati * 100;
    }
    
    public void addAchievement(String achievementId) {
        if (!achievements.contains(achievementId)) {
            achievements.add(achievementId);
        }
    }
    
    public int getCurrentLevel() {
        if (xp < 500) return 1;
        if (xp < 1500) return 2;
        if (xp < 4000) return 3;
        if (xp < 10000) return 4;
        if (xp < 25000) return 5;
        if (xp < 75000) return 6;
        return 7;
    }
    
    public String getLevelName() {
        int level = getCurrentLevel();
        switch (level) {
            case 1: return "Novizio";
            case 2: return "Apprentista";
            case 3: return "Giocatore";
            case 4: return "Veterano";
            case 5: return "Esperto";
            case 6: return "Campione";
            case 7: return "Leggenda";
            default: return "Sconosciuto";
        }
    }
    
    @Override
    public String toString() {
        return "User{" +
                "nome='" + nome + '\'' +
                ", username='" + username + '\'' +
                ", saldo=" + saldo +
                ", xp=" + xp +
                ", level=" + getCurrentLevel() +
                '}';
    }
}
 