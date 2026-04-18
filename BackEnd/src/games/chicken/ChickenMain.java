package games.chicken;

import core.State;
import core.GameRecord;
import core.Database;
import core.Auth;
import java.util.Scanner;

public class ChickenMain {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🐔 CHICKEN CROSS THE ROAD         ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Il pollo deve attraversare la strada!  ║");
        System.out.println("║ Evita le auto 🚗 e aumenta il premio! ║");
        System.out.println("║ Ogni livello: moltiplicatore x1.5     ║");
        System.out.println("║ Incassa quando vuoi o rischia tutto!  ║");
        System.out.println("╚════════════════════════════════════════╝");
        
        System.out.printf("\nSaldo attuale: €%.2f%n", State.getBalance());
        System.out.print("Inserisci la puntata: €");
        
        double bet;
        try {
            bet = Double.parseDouble(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("❌ Puntata non valida!");
            return;
        }
        
        if (bet <= 0 || bet > State.getBalance()) {
            System.out.println("❌ Puntata non valida o saldo insufficiente!");
            return;
        }
        
        // Deduce la puntata
        State.deductBalance(bet);
        
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
                // Cash out
                double winAmount = game.cashOut();
                State.addBalance(winAmount);
                
                System.out.println("\n╔════════════════════════════════════════╗");
                System.out.println("║          💰 HAI INCASSATO!            ║");
                System.out.println("╠════════════════════════════════════════╣");
                System.out.printf("║ Livelli completati: %d                  ║%n", game.getLevel());
                System.out.printf("║ Moltiplicatore finale: %.2fx           ║%n", game.getMultiplier());
                System.out.printf("║ Vincita: €%.2f                         ║%n", winAmount);
                System.out.printf("║ Guadagno: €%.2f                        ║%n", winAmount - bet);
                System.out.printf("║ Nuovo saldo: €%.2f                     ║%n", State.getBalance());
                System.out.println("╚════════════════════════════════════════╝");
                
                // Registra vittoria
                if (Auth.getCurrentUser() != null) {
                    GameRecord record = new GameRecord(
                        Auth.getCurrentUser().getId(),
                        "Chicken Road",
                        bet,
                        winAmount,
                        winAmount - bet
                    );
                    Database.addGameRecord(record);
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
                System.out.println("║          🚗💥 GAME OVER!              ║");
                System.out.println("╠════════════════════════════════════════╣");
                System.out.println("║ Il pollo è stato investito!            ║");
                System.out.printf("║ Livelli completati: %d                  ║%n", game.getLevel());
                System.out.printf("║ Hai perso: €%.2f                       ║%n", bet);
                System.out.printf("║ Saldo rimanente: €%.2f                 ║%n", State.getBalance());
                System.out.println("╚════════════════════════════════════════╝");
                
                // Registra perdita
                if (Auth.getCurrentUser() != null) {
                    GameRecord record = new GameRecord(
                        Auth.getCurrentUser().getId(),
                        "Chicken Road",
                        bet,
                        0,
                        -bet
                    );
                    Database.addGameRecord(record);
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
}
