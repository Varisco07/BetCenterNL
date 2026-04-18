package core;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Scanner;

public class Auth {
    private static HashMap<String, User> users = new HashMap<>();
    private static User currentUser = null;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static boolean login(String email, String password) {
        for (User user : users.values()) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    public static boolean register(String nome, String cognome, String username, String email, String password, String dob) {
        // Controlla se email esiste già
        for (User user : users.values()) {
            if (user.getEmail().equals(email)) {
                return false;
            }
        }

        // Controlla età
        if (!isAtLeast18(dob)) {
            return false;
        }

        User newUser = new User(nome, cognome, username, email, password, dob);
        users.put(email, newUser);
        currentUser = newUser;
        return true;
    }

    private static boolean isAtLeast18(String dob) {
        try {
            LocalDate birthDate = LocalDate.parse(dob);
            LocalDate today = LocalDate.now();
            int age = Period.between(birthDate, today).getYears();
            return age >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    public static void mostraAuthMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean autenticato = false;

        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║     BENVENUTO A BETCENTER NL           ║");
        System.out.println("╚════════════════════════════════════════╝\n");

        while (!autenticato) {
            System.out.println("┌────────────────────────────────────────┐");
            System.out.println("│ 1. 🔐 LOGIN                            │");
            System.out.println("│ 2. 📝 REGISTRAZIONE                    │");
            System.out.println("│ 3. 🎮 DEMO                             │");
            System.out.println("│ 4. 🚪 ESCI                             │");
            System.out.println("└────────────────────────────────────────┘");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1":
                    autenticato = handleLogin(scanner);
                    break;
                case "2":
                    handleRegister(scanner);
                    break;
                case "3":
                    handleDemo();
                    autenticato = true;
                    break;
                case "4":
                    System.out.println("\n👋 Arrivederci!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("❌ Opzione non valida!\n");
            }
        }
    }

    private static boolean handleLogin(Scanner scanner) {
        System.out.print("\nEmail: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        if (login(email, password)) {
            System.out.println("\n✅ Login effettuato con successo!");
            System.out.println("Benvenuto, " + currentUser.getNome() + "!\n");
            return true;
        } else {
            System.out.println("❌ Email o password errati!\n");
            return false;
        }
    }

    private static void handleRegister(Scanner scanner) {
        System.out.print("\nNome: ");
        String nome = scanner.nextLine().trim();
        System.out.print("Cognome: ");
        String cognome = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Data di nascita (YYYY-MM-DD): ");
        String dob = scanner.nextLine().trim();

        if (register(nome, cognome, username, email, password, dob)) {
            System.out.println("\n✅ Registrazione effettuata con successo!");
            System.out.println("Benvenuto, " + nome + "!\n");
        } else {
            System.out.println("❌ Errore nella registrazione (email già esistente o minore di 18 anni)\n");
        }
    }

    private static void handleDemo() {
        User demoUser = new User("Demo", "Player", "demo", "demo@betcenter.nl", "demo123", "2000-01-01");
        currentUser = demoUser;
        System.out.println("\n🎮 Modalità demo attivata!");
        System.out.println("Benvenuto, Demo Player!\n");
    }
}
