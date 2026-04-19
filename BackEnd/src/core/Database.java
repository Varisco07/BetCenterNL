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
        // FIX: scrittura atomica tramite file temporaneo per evitare corruzione in caso di crash
        File tmpFile = new File(USERS_FILE + ".tmp");
        File target = new File(USERS_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio degli utenti: " + e.getMessage());
            tmpFile.delete();
            return;
        }
        if (!tmpFile.renameTo(target)) {
            // fallback su sistemi che non supportano rename atomico
            target.delete();
            tmpFile.renameTo(target);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, User> loaded = (Map<String, User>) ois.readObject();
            // FIX: aggiorna la mappa solo se il caricamento ha avuto successo
            users = loaded;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nel caricamento degli utenti: " + e.getMessage());
            // FIX: file corrotto — mappa resta quella attuale (vuota all'avvio) invece di lasciare
            // dati parziali inconsistenti
            users = new HashMap<>();
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
        // FIX: salva sia gli utenti che la cronologia così la entry del nuovo utente
        // non va persa in caso di crash prima del prossimo saveGameHistory
        saveUsers();
        saveGameHistory();
    }
    
    public static boolean userExists(String email) {
        return users.containsKey(email);
    }
    
    public static Collection<User> getAllUsers() {
        return users.values();
    }
    
    // ── GAME HISTORY ──
    public static void saveGameHistory() {
        // FIX: scrittura atomica tramite file temporaneo per evitare corruzione in caso di crash
        File tmpFile = new File(GAMES_FILE + ".tmp");
        File target = new File(GAMES_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
            oos.writeObject(gameHistory);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della cronologia: " + e.getMessage());
            tmpFile.delete();
            return;
        }
        if (!tmpFile.renameTo(target)) {
            target.delete();
            tmpFile.renameTo(target);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadGameHistory() {
        File file = new File(GAMES_FILE);
        if (!file.exists()) return;
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            Map<String, List<GameRecord>> loaded = (Map<String, List<GameRecord>>) ois.readObject();
            // FIX: aggiorna la mappa solo se il caricamento ha avuto successo
            gameHistory = loaded;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Errore nel caricamento della cronologia: " + e.getMessage());
            // FIX: file corrotto — mappa resta vuota invece di dati parziali inconsistenti
            gameHistory = new HashMap<>();
        }
    }
    
    public static void recordGameResult(String userId, GameRecord record) {
        // FIX: aggiunge il record SOLO in gameHistory (singola sorgente di verità).
        // In precedenza veniva aggiunto anche via user.addGameRecord() causando
        // duplicazione: ogni partita era salvata sia in gameHistory (games.dat)
        // che in user.history dentro users.dat, con possibile divergenza tra i due.
        gameHistory.computeIfAbsent(userId, k -> new ArrayList<>()).add(0, record);
        User user = getUserById(userId);
        if (user != null) {
            user.updateStats(record);  // aggiorna solo contatori/xp, NON aggiunge a history
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
