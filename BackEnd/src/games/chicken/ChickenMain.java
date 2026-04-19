package games.chicken;

import core.State;
import core.Auth;
import core.User;
import core.Database;
import core.GameRecord;
import java.util.Scanner;

public class ChickenMain {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🐔 CHICKEN CROSS THE ROAD          ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Il pollo deve attraversare la strada!  ║");
        System.out.println("║ Evita le auto 🚗 e aumenta il premio!  ║");
        System.out.println("║ Ogni livello: moltiplicatore x1.5      ║");
        System.out.println("║ Incassa quando vuoi o rischia tutto!   ║");
        System.out.println("╚════════════════════════════════════════╝");
        if (!chiediConfermaGioco(scanner)) {
            return;
        }
        
        System.out.printf("\nSaldo attuale: €%.2f%n", State.getBalance());
        System.out.print("Inserisci la puntata: €");
        
        double bet;
        try {
            bet = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Puntata non valida!");
            return;
        }
        
        if (bet <= 0) {
            System.out.println("❌ Puntata non valida o saldo insufficiente!");
            return;
        }
        if (!State.deductBalance(bet)) {
            System.out.println("❌ Puntata non valida o saldo insufficiente!");
            return;
        }
        
        ChickenGame game = new ChickenGame(bet);
        
        while (!game.isGameOver() && !game.isCashOut()) {
            game.displayGrid();
            
            System.out.print("\nScegli posizione (1-5) o 0 per incassare: ");
            int choice;
            
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("❌ Input non valido!");
                continue;
            }
            
            if (choice == 0) {

                double winAmount = game.cashOut();
                State.addBalance(winAmount);

                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║          💰 HAI INCASSATO!             ║");
                System.out.println("╠════════════════════════════════════════╣");

                System.out.printf("║ %-38s ║%n", "Livelli completati: " + game.getLevel());
                System.out.printf("║ %-38s ║%n", String.format("Moltiplicatore finale: %.2fx", game.getMultiplier()));
                System.out.printf("║ %-38s ║%n", String.format("Vincita: €%.2f", winAmount));
                System.out.printf("║ %-38s ║%n", String.format("Guadagno: €%.2f", winAmount - bet));
                System.out.printf("║ %-38s ║%n", String.format("Nuovo saldo: €%.2f", State.getBalance()));

                System.out.println("╚════════════════════════════════════════╝");
                if (Auth.getCurrentUser() != null) {
                    double gain = winAmount - bet;
                    boolean win = gain > 0;
                    GameRecord record = new GameRecord("Chicken Road", bet, gain, win);
                    Database.recordGameResult(Auth.getCurrentUser().getId(), record);
                }
                
                break;
            }
            
            if (choice < 1 || choice > 5) {
                System.out.println("❌ Posizione non valida! Scegli 1-5");
                continue;
            }
            
            boolean success = game.move(choice - 1);
            
            if (!success) {
                // Ha colpito un'auto!
                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║          🚗💥 GAME OVER!               ║");
                System.out.println("╠════════════════════════════════════════╣");

                System.out.printf("║ %-38s ║%n", "Il pollo è stato investito!");
                System.out.printf("║ %-38s ║%n", "Livelli completati: " + game.getLevel());
                System.out.printf("║ %-38s ║%n", String.format("Hai perso: €%.2f", bet));
                System.out.printf("║ %-38s ║%n", String.format("Saldo rimanente: €%.2f", State.getBalance()));

                System.out.println("╚════════════════════════════════════════╝");
                
                if (Auth.getCurrentUser() != null) {
                    GameRecord record = new GameRecord("Chicken Road", bet, -bet, false);
                    Database.recordGameResult(Auth.getCurrentUser().getId(), record);
                }
                
                break;
            }
            
            // Successo!
            System.out.println("\n✅ Sicuro! Livello " + game.getLevel() + " completato!");
            System.out.printf("💰 Vincita attuale: €%.2f (%.2fx)%n", game.getCurrentWin(), game.getMultiplier());
            System.out.println("Premi Invio per continuare...");
            scanner.nextLine();
        }
        
        System.out.println("\nGrazie per aver giocato! 🐔");
    }
    
    private static boolean chiediConfermaGioco(Scanner scanner) {
        while (true) {
            System.out.println("\nVuoi giocare?");
            System.out.println("1 - Si");
            System.out.println("2 - No");
            String scelta = scanner.nextLine().trim();
            if (scelta.equals("1")) return true;
            if (scelta.equals("2")) return false;
            System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
        }
    }
}
