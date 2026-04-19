package games.baccarat;

import java.util.Scanner;
import core.*;

public class BaccaratMain {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        User user = Auth.getCurrentUser();
        
        if (user == null) {
            System.out.println("❌ Devi essere autenticato!");
            return;
        }
        
        boolean continua = true;
        
        // Prima conferma prima di iniziare
        if (!chiediConfermaGioco(scanner)) {
            user.setSaldo(State.getBalance());
            Database.saveUsers();
            System.out.println("\n👋 Grazie per aver giocato a Baccarat!");
            return;
        }
        
        while (continua) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║          💎 BACCARAT                   ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.printf( "║ Saldo: €%-31s║%n", String.format("%.2f", State.getBalance()));
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ Regole:                                ║");
            System.out.println("║ - Giocatore vs Banco                   ║");
            System.out.println("║ - Valore più vicino a 9 vince          ║");
            System.out.println("║ - Giocatore paga 1:1                   ║");
            System.out.println("║ - Banco paga 0.95:1 (commissione 5%)   ║");
            System.out.println("║ - Pareggio paga 8:1                    ║");
            System.out.println("╚════════════════════════════════════════╝\n");
            
            System.out.print("Inserisci la puntata (€): ");
            double bet = 0;
            try {
                bet = Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Importo non valido!");
                continue;
            }
            
            if (bet < 1 || bet > 10000) {
                System.out.println("❌ Puntata non valida (€1 - €10.000)!");
                continue;
            }
            
            if (!State.canBet(bet)) {
                System.out.println("❌ Saldo insufficiente!");
                continue;
            }
            
            System.out.println("\nSu chi scommetti?");
            System.out.println("1. 👤 GIOCATORE (paga 1:1)");
            System.out.println("2. 🏦 BANCO (paga 0.95:1)");
            System.out.println("3. 🤝 PAREGGIO (paga 8:1)");
            System.out.print("Scelta: ");
            
            String scelta = scanner.nextLine().trim();
            String betType = "";
            
            switch (scelta) {
                case "1": betType = "player"; break;
                case "2": betType = "banker"; break;
                case "3": betType = "tie"; break;
                default:
                    System.out.println("❌ Scelta non valida!");
                    continue;
            }
            
            if (!State.deductBalance(bet)) {
                System.out.println("❌ Saldo insufficiente!");
                continue;
            }
            Baccarat.BaccaratResult result = Baccarat.play(betType);
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║           🎴 RISULTATO                 ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.printf( "║ GIOCATORE: %-28s║%n", formatHand(result.playerHand) + " = " + result.playerValue);
            System.out.printf( "║ BANCO:     %-28s║%n", formatHand(result.bankerHand) + " = " + result.bankerValue);
            System.out.println("╠════════════════════════════════════════╣");
            
            double gain = -bet;
            boolean win = false;
            
            if (betType.equals(result.winner)) {
                gain = bet * result.payout - bet;
                win = true;
                State.addBalance(bet * result.payout);
                System.out.printf("║ ✅ HAI VINTO! +%-24s║%n", String.format("%.2f", gain) + "€");
            } else if (result.winner.equals("tie") && betType.equals("tie")) {
                gain = bet * result.payout - bet;
                win = true;
                State.addBalance(bet * result.payout);
                System.out.printf("║ ✅ PAREGGIO! +%-25s║%n", String.format("%.2f", gain) + "€");
            } else {
                System.out.printf("║ ❌ HAI PERSO! -%-24s║%n", String.format("%.2f", bet) + "€");
            }
            
            System.out.printf( "║ Nuovo saldo: €%-25s║%n", String.format("%.2f", State.getBalance()));
            System.out.println("╚════════════════════════════════════════╝");
            
            // Registra il risultato
            GameRecord record = new GameRecord("Baccarat", bet, gain, win);
            Database.recordGameResult(user.getId(), record);
            
            while (true) {
                System.out.println("\nVuoi giocare ancora?");
                System.out.println("1 - Si");
                System.out.println("2 - No");
                String risposta = scanner.nextLine().trim();
                if (risposta.equals("1")) { continua = true; break; }
                if (risposta.equals("2")) { continua = false; break; }
                System.out.println("❌ Scelta non valida! Inserisci 1 o 2.");
            }
        }
        
        user.setSaldo(State.getBalance());
        Database.saveUsers();
        System.out.println("\n👋 Grazie per aver giocato a Baccarat!");
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
    
    private static String formatHand(java.util.List<Baccarat.Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (Baccarat.Card card : hand) {
            sb.append(card.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}
