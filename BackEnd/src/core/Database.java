package core;

import java.io.*;
import java.util.*;

public class Database {
    private static final String USERS_FILE = "data/users.dat";
    private static final String GAMES_FILE = "data/games.dat";
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, List<GameRecord>> gameHistory = new HashMap<>();
    
    static {
        new File("data").mkdirs();
        loadUsers();
        loadGameHistory();
    }
    
    // ── USERS ──
    public static void saveUsers() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio degli utenti: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            users = (Map<String, User>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nel caricamento degli utenti: " + e.getMessage());
        }
    }
    
    public static User getUserByEmail(String email) {
        return users.get(email);
    }
    
    public static User getUserById(String id) {
        for (User u : users.values()) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }
    
    public static void registerUser(User user) {
        users.put(user.getEmail(), user);
        gameHistory.put(user.getId(), new ArrayList<>());
        saveUsers();
    }
    
    public static boolean userExists(String email) {
        return users.containsKey(email);
    }
    
    public static Collection<User> getAllUsers() {
        return users.values();
    }
    
    // ── GAME HISTORY ──
    public static void saveGameHistory() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(GAMES_FILE))) {
            oos.writeObject(gameHistory);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della cronologia: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadGameHistory() {
        File file = new File(GAMES_FILE);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            gameHistory = (Map<String, List<GameRecord>>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nel caricamento della cronologia: " + e.getMessage());
        }
    }
    
    public static void recordGameResult(String userId, GameRecord record) {
        gameHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, record);
        User user = getUserById(userId);
        if (user != null) {
            user.addGameRecord(record);
            saveUsers();
        }
        saveGameHistory();
    }
    
    public static List<GameRecord> getGameHistory(String userId) {
        return gameHistory.getOrDefault(userId, new ArrayList<>());
    }
    
    public static void clearAll() {
        users.clear();
        gameHistory.clear();
        new File(USERS_FILE).delete();
        new File(GAMES_FILE).delete();
    }
}
