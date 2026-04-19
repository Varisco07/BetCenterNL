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
            System.out.println("║ 4. 🚪 Esci                            ║");
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
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║           📝 REGISTRAZIONE             ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.print("Nome: ");
        String nome = scanner.nextLine().trim();
        
        System.out.print("Cognome: ");
        String cognome = scanner.nextLine().trim();
        
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        if (Database.userExists(email)) {
            System.out.println("❌ Email già registrata!");
            return;
        }
        
        System.out.print("Password (min 8 caratteri): ");
        String password = scanner.nextLine().trim();
        
        if (password.length() < 8) {
            System.out.println("❌ Password troppo corta!");
            return;
        }
        
        System.out.print("Data di nascita (YYYY-MM-DD): ");
        String dob = scanner.nextLine().trim();
        
        // Verifica età
        try {
            LocalDate birthDate = LocalDate.parse(dob);
            int age = Period.between(birthDate, LocalDate.now()).getYears();
            if (age < 18) {
                System.out.println("❌ Devi avere almeno 18 anni!");
                return;
            }
        } catch (Exception e) {
            System.out.println("❌ Data non valida!");
            return;
        }
        
        User newUser = new User(nome, cognome, username, email, password, dob);
        Database.registerUser(newUser);
        currentUser = newUser;
        
        System.out.println("\n✅ Registrazione completata!");
        System.out.println("🎉 Benvenuto " + nome + "! Hai ricevuto €1.000 di bonus di benvenuto!");
    }
    
    private static boolean accedi(Scanner scanner) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║            🔑 ACCEDI                   ║");
        System.out.println("╚════════════════════════════════════════╝\n");
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        User user = Database.getUserByEmail(email);
        if (user == null || !user.getPassword().equals(password)) {
            System.out.println("❌ Email o password non corretti!");
            return false;
        }
        
        currentUser = user;
        System.out.println("\n✅ Accesso effettuato!");
        System.out.println("👋 Bentornato, " + user.getNome() + "!");
        return true;
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
