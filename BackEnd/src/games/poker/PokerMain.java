package games.poker;

import java.util.*;
import core.*;

public class PokerMain {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        User user = Auth.getCurrentUser();
        
        if (user == null) {
            System.out.println("❌ Devi essere autenticato!");
            return;
        }
        
        boolean continua = true;
        
        while (continua) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        ♠ VIDEO POKER (Jacks+)         ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ Saldo: €" + String.format("%.2f", State.getBalance()));
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ TABELLA PAGAMENTI:                     ║");
            System.out.println("║ Royal Flush ............ 800x           ║");
            System.out.println("║ Straight Flush ........ 50x            ║");
            System.out.println("║ Poker (4 uguali) ...... 25x            ║");
            System.out.println("║ Full House ............ 9x             ║");
            System.out.println("║ Colore (Flush) ........ 6x             ║");
            System.out.println("║ Scala (Straight) ...... 4x             ║");
            System.out.println("║ Tris ................. 3x             ║");
            System.out.println("║ Doppia Coppia ......... 2x             ║");
            System.out.println("║ Coppia J+ ............ 1x             ║");
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
            
            State.deductBalance(bet);
            
            // Deal initial hand
            List<VideoPoker.Card> hand = VideoPoker.dealHand();
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║           🃏 LA TUA MANO              ║");
            System.out.println("╠════════════════════════════════════════╣");
            for (int i = 0; i < hand.size(); i++) {
                System.out.println("║ " + (i+1) + ". " + hand.get(i).toString());
            }
            System.out.println("╚════════════════════════════════════════╝\n");
            
            // Ask which cards to hold
            boolean[] held = new boolean[5];
            System.out.println("Quali carte vuoi tenere? (es: 1,3,5 oppure 'nessuna')");
            System.out.print("Scelta: ");
            String scelta = scanner.nextLine().trim().toLowerCase();
            
            if (!scelta.equals("nessuna")) {
                try {
                    String[] parts = scelta.split(",");
                    for (String part : parts) {
                        int idx = Integer.parseInt(part.trim()) - 1;
                        if (idx >= 0 && idx < 5) {
                            held[idx] = true;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("❌ Scelta non valida!");
                    State.addBalance(bet);
                    continue;
                }
            }
            
            // Draw new cards
            hand = VideoPoker.drawCards(hand, held);
            VideoPoker.PokerResult result = VideoPoker.evaluateHand(hand);
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║           🎴 RISULTATO                ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ Mano: " + formatHand(hand));
            System.out.println("║ Combinazione: " + result.handName);
            System.out.println("║ Moltiplicatore: " + result.multiplier + "x");
            System.out.println("╠════════════════════════════════════════╣");
            
            double gain = -bet;
            boolean win = false;
            
            if (result.multiplier > 0) {
                double winAmount = bet * result.multiplier;
                State.addBalance(winAmount);
                gain = winAmount - bet;
                win = true;
                System.out.println("║ ✅ HAI VINTO! +" + String.format("%.2f", gain) + "€");
            } else {
                System.out.println("║ ❌ NESSUNA VINCITA!");
            }
            
            System.out.println("║ Nuovo saldo: €" + String.format("%.2f", State.getBalance()));
            System.out.println("╚════════════════════════════════════════╝");
            
            // Registra il risultato
            GameRecord record = new GameRecord("Video Poker", bet, gain, win);
            record.setDetails(result.handName);
            Database.recordGameResult(user.getId(), record);
            
            System.out.print("\nVuoi giocare ancora? (s/n): ");
            String risposta = scanner.nextLine().trim().toLowerCase();
            if (!risposta.equals("s")) continua = false;
        }
        
        user.setSaldo(State.getBalance());
        Database.saveUsers();
        System.out.println("\n👋 Grazie per aver giocato a Video Poker!");
    }
    
    private static String formatHand(List<VideoPoker.Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (VideoPoker.Card card : hand) {
            sb.append(card.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}
