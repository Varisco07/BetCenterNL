package core;

import java.time.LocalDate;
import java.time.Period;
import java.util.Scanner;

public class Auth {
    private static User currentUser = null;
    
    public static void mostraAuthMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean autenticato = false;
        
        while (!autenticato) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║      BetCenterNL — Autenticazione      ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ 1. 📝 Registrati                       ║");
            System.out.println("║ 2. 🔑 Accedi                           ║");
            System.out.println("║ 3. 🎮 Demo (senza registrazione)       ║");
            System.out.println("║ 4. 🚪 Esci                             ║");
            System.out.println("╚════════════════════════════════════════╝");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine().trim();
            
            switch (scelta) {
                case "1":
                    registrati(scanner);
                    break;
                case "2":
                    if (accedi(scanner)) autenticato = true;
                    break;
                case "3":
                    demoLogin();
                    autenticato = true;
                    break;
                case "4":
                    System.out.println("\n👋 Arrivederci!");
                    System.exit(0);
                default:
                    System.out.println("❌ Opzione non valida!");
            }
        }
    }
    
    private static void registrati(Scanner scanner) {
        boolean registrazioneCompletata = false;
        
        while (!registrazioneCompletata) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║           📝 REGISTRAZIONE             ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            // Nome
            String nome = "";
            while (nome.length() < 3) {
                System.out.print("Nome (min 3 caratteri): ");
                nome = scanner.nextLine().trim();
                if (nome.length() < 3) {
                    System.out.println("❌ ERRORE: Il nome deve contenere almeno 3 caratteri!");
                    System.out.println("💡 Suggerimento: Inserisci il tuo nome completo.\n");
                }
            }
            
            // Cognome
            String cognome = "";
            while (cognome.length() < 3) {
                System.out.print("Cognome (min 3 caratteri): ");
                cognome = scanner.nextLine().trim();
                if (cognome.length() < 3) {
                    System.out.println("❌ ERRORE: Il cognome deve contenere almeno 3 caratteri!");
                    System.out.println("💡 Suggerimento: Inserisci il tuo cognome completo.\n");
                }
            }
            
            // Username
            String username = "";
            while (username.isEmpty()) {
                System.out.print("Username: ");
                username = scanner.nextLine().trim();
                if (username.isEmpty()) {
                    System.out.println("❌ ERRORE: L'username non può essere vuoto!");
                    System.out.println("💡 Suggerimento: Scegli un nome utente univoco.\n");
                }
            }
            
            // Email
            String email = "";
            boolean emailValida = false;
            while (!emailValida) {
                System.out.print("Email: ");
                email = scanner.nextLine().trim();
                
                if (email.isEmpty()) {
                    System.out.println("❌ ERRORE: L'email non può essere vuota!\n");
                } else if (!email.contains("@")) {
                    System.out.println("❌ ERRORE: Email non valida - manca il simbolo '@'!");
                    System.out.println("💡 Suggerimento: L'email deve contenere '@' (es. utente@esempio.com).\n");
                } else if (!email.contains(".")) {
                    System.out.println("❌ ERRORE: Email non valida - manca il dominio!");
                    System.out.println("💡 Suggerimento: L'email deve avere un dominio con '.' (es. utente@esempio.com).\n");
                } else if (email.indexOf("@") > email.lastIndexOf(".")) {
                    System.out.println("❌ ERRORE: Email non valida - formato errato!");
                    System.out.println("💡 Suggerimento: Il dominio deve venire dopo '@' (es. utente@esempio.com).\n");
                } else if (Database.userExists(email)) {
                    System.out.println("❌ ERRORE: Questa email è già registrata nel sistema!");
                    System.out.println("💡 Suggerimento: Usa un'altra email o prova ad accedere con questa.\n");
                } else {
                    emailValida = true;
                }
            }
            
            // Password
            String password = "";
            while (password.length() < 8) {
                System.out.print("Password (min 8 caratteri): ");
                password = scanner.nextLine().trim();
                if (password.isEmpty()) {
                    System.out.println("❌ ERRORE: La password non può essere vuota!\n");
                } else if (password.length() < 8) {
                    int mancanti = 8 - password.length();
                    System.out.println("❌ ERRORE: Password troppo corta! Ti mancano " + mancanti + " caratteri.");
                    System.out.println("💡 Suggerimento: La password deve essere di almeno 8 caratteri (attualmente: " + password.length() + ").\n");
                }
            }
            
            // Data di nascita
            String dob = "";
            boolean dataValida = false;
            while (!dataValida) {
                System.out.print("Data di nascita (YYYY-MM-DD): ");
                dob = scanner.nextLine().trim();
                
                try {
                    LocalDate birthDate = LocalDate.parse(dob);
                    int age = Period.between(birthDate, LocalDate.now()).getYears();
                    if (age < 18) {
                        System.out.println("❌ ERRORE: Devi avere almeno 18 anni per registrarti!");
                        System.out.println("💡 Suggerimento: Il gioco d'azzardo è riservato ai maggiorenni.\n");
                    } else {
                        dataValida = true;
                    }
                } catch (Exception e) {
                    System.out.println("❌ ERRORE: Data non valida!");
                    System.out.println("💡 Suggerimento: Usa il formato YYYY-MM-DD (es. 1990-01-15).\n");
                }
            }
            
            // Registrazione completata
            User newUser = new User(nome, cognome, username, email, password, dob);
            Database.registerUser(newUser);
            currentUser = newUser;
            
            System.out.println("\n✅ Registrazione completata!");
            System.out.println("🎉 Benvenuto " + nome + "! Hai ricevuto €1.000 di bonus di benvenuto!");
            registrazioneCompletata = true;
        }
    }
    
    private static boolean accedi(Scanner scanner) {
        boolean loginRiuscito = false;
        int tentativiRimasti = 3;
        
        while (!loginRiuscito && tentativiRimasti > 0) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║            🔑 ACCEDI                   ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            if (tentativiRimasti < 3) {
                System.out.println("⚠️  Tentativi rimasti: " + tentativiRimasti);
            }
            
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Password: ");
            String password = scanner.nextLine().trim();
            
            // Ricarica il database per vedere utenti creati dal sito web
            Database.reload();
            
            User user = Database.getUserByEmail(email);
            
            // Verifica se l'email esiste
            if (user == null) {
                System.out.println("\n❌ ERRORE: Email non trovata!");
                System.out.println("💡 Suggerimento: Verifica di aver inserito l'email corretta o registrati se non hai un account.");
                tentativiRimasti--;
                
                if (tentativiRimasti > 0) {
                    System.out.print("\nVuoi riprovare? (s/n): ");
                    String risposta = scanner.nextLine().trim().toLowerCase();
                    if (!risposta.equals("s") && !risposta.equals("si")) {
                        return false;
                    }
                }
            } 
            // Verifica se la password è corretta
            else if (!user.getPassword().equals(password)) {
                System.out.println("\n❌ ERRORE: Password errata!");
                System.out.println("💡 Suggerimento: Controlla che il CAPS LOCK non sia attivo e riprova.");
                tentativiRimasti--;
                
                if (tentativiRimasti > 0) {
                    System.out.print("\nVuoi riprovare? (s/n): ");
                    String risposta = scanner.nextLine().trim().toLowerCase();
                    if (!risposta.equals("s") && !risposta.equals("si")) {
                        return false;
                    }
                }
            } 
            // Login riuscito
            else {
                currentUser = user;
                System.out.println("\n✅ Accesso effettuato!");
                System.out.println("👋 Bentornato, " + user.getNome() + "!");
                loginRiuscito = true;
            }
        }
        
        if (!loginRiuscito) {
            System.out.println("\n🚫 Troppi tentativi falliti. Torna al menu principale.");
        }
        
        return loginRiuscito;
    }
    
    private static void demoLogin() {
        // FIX: in precedenza veniva registrato un nuovo utente demo ad ogni sessione,
        // creando utenti duplicati nel database. Ora si riusa l'utente demo esistente.
        User demoUser = Database.getUserByEmail("demo@betcenter.nl");
        if (demoUser == null) {
            demoUser = new User("Demo", "Player", "demo", "demo@betcenter.nl", "demo123", "1990-01-01");
            Database.registerUser(demoUser);
        }
        currentUser = demoUser;
        System.out.println("\n🎮 Modalità demo attivata!");
        System.out.println("Saldo: €1.000");
    }
    
    public static User getCurrentUser() {
        return currentUser;
    }
    
    public static void logout() {
        if (currentUser != null) {
            Database.saveUsers();
            System.out.println("\n👋 Logout effettuato!");
            currentUser = null;
        }
    }
}
 