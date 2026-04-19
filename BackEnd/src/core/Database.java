package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Database {
    // Percorso assoluto alla cartella data — funziona sia da terminale che da IntelliJ
    private static final String DATA_DIR;
    private static final String USERS_FILE;
    private static final String GAMES_FILE;

    static {
        // Cerca la cartella "data" risalendo dalla directory di lavoro o dalla posizione del .class
        String dir = findDataDir();
        DATA_DIR   = dir;
        USERS_FILE = dir + File.separator + "users.dat";
        GAMES_FILE = dir + File.separator + "games.dat";
        new File(dir).mkdirs();
        loadUsers();
        loadGameHistory();
    }

    private static String findDataDir() {
        // Usa sempre BackEnd/data come percorso canonico del progetto
        // Risale dalla directory di lavoro finché trova la cartella BackEnd
        File cur = new File("").getAbsoluteFile();
        for (int i = 0; i < 8; i++) {
            // Caso 1: siamo dentro BackEnd (server web)
            File candidate1 = new File(cur, "data");
            if (new File(cur, "src").exists() && candidate1.exists()) {
                return candidate1.getAbsolutePath();
            }
            // Caso 2: siamo nella root del progetto o sopra (terminale IntelliJ)
            File candidate2 = new File(cur, "BackEnd" + File.separator + "data");
            if (candidate2.exists()) {
                return candidate2.getAbsolutePath();
            }
            // Caso 3: BackEnd esiste come cartella figlia
            File backendDir = new File(cur, "BackEnd");
            if (backendDir.exists() && backendDir.isDirectory()) {
                File dataDir = new File(backendDir, "data");
                dataDir.mkdirs();
                return dataDir.getAbsolutePath();
            }
            if (cur.getParentFile() == null) break;
            cur = cur.getParentFile();
        }
        // Fallback assoluto: cerca BetCenterNL nel percorso
        File abs = new File("").getAbsoluteFile();
        while (abs != null) {
            if (abs.getName().equalsIgnoreCase("BetCenterNL")) {
                File dataDir = new File(abs, "BackEnd" + File.separator + "data");
                dataDir.mkdirs();
                return dataDir.getAbsolutePath();
            }
            abs = abs.getParentFile();
        }
        // Ultimo fallback
        new File("data").mkdirs();
        return new File("data").getAbsolutePath();
    }

    private static Map<String, User> users = new HashMap<>();
    private static Map<String, List<GameRecord>> gameHistory = new HashMap<>();
    
    // ── USERS ──
    public static void saveUsers() {
        File tmpFile = new File(USERS_FILE + ".tmp");
        File target  = new File(USERS_FILE);
        File backup  = new File(USERS_FILE + ".bak");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio degli utenti: " + e.getMessage());
            tmpFile.delete();
            return;
        }
        try {
            // Backup del file corrente prima di sovrascrivere
            if (target.exists()) {
                Files.copy(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            // Sostituzione atomica
            Files.move(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // ATOMIC_MOVE non supportato su tutti i filesystem — fallback
            try {
                Files.move(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                System.err.println("Errore critico nel salvataggio: " + e2.getMessage());
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            // Prova il backup
            File backup = new File(USERS_FILE + ".bak");
            if (backup.exists()) {
                System.err.println("[DB] users.dat non trovato, ripristino dal backup...");
                try { Files.copy(backup.toPath(), file.toPath()); } catch (IOException ignored) {}
            } else return;
        }
        
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

    /** Ricarica il database dal disco — utile quando terminale e web condividono i file */
    public static synchronized void reload() {
        // Ricarica solo se il file è più recente dell'ultima lettura
        File f = new File(USERS_FILE);
        if (!f.exists()) return;
        loadUsers();
        loadGameHistory();
    }
    
    // ── GAME HISTORY ──
    public static void saveGameHistory() {
        File tmpFile = new File(GAMES_FILE + ".tmp");
        File target  = new File(GAMES_FILE);
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tmpFile))) {
            oos.writeObject(gameHistory);
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio della cronologia: " + e.getMessage());
            tmpFile.delete();
            return;
        }
        try {
            Files.move(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            try {
                Files.move(tmpFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e2) {
                System.err.println("Errore critico nel salvataggio cronologia: " + e2.getMessage());
            }
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
