package games.poker;

import java.util.*;
import core.State;
import core.Auth;
import core.User;
import core.Database;
import core.GameRecord;

public class PokerMain {
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        User utente = Auth.getCurrentUser();
        
        if (utente == null) {
            System.out.println("❌ Devi essere autenticato!");
            return;
        }
        
        boolean continua = true;
        
        // Prima conferma prima di iniziare
        if (!chiediConfermaGioco(scanner)) {
            utente.setSaldo(State.getBalance());
            Database.saveUsers();
            System.out.println("\n👋 Grazie per aver giocato a Video Poker!");
            return;
        }
        
        while (continua) {
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║        ♠ VIDEO POKER (Jacks+)          ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.printf( "║ Saldo: €%-31s║%n", String.format("%.2f", State.getBalance()));
            System.out.println("╠════════════════════════════════════════╣");
            System.out.println("║ TABELLA PAGAMENTI:                     ║");
            System.out.println("║ Royal Flush ............ 800x          ║");
            System.out.println("║ Straight Flush ........ 50x            ║");
            System.out.println("║ Poker (4 uguali) ...... 25x            ║");
            System.out.println("║ Full House ............ 9x             ║");
            System.out.println("║ Colore (Flush) ........ 6x             ║");
            System.out.println("║ Scala (Straight) ...... 4x             ║");
            System.out.println("║ Tris ................. 3x              ║");
            System.out.println("║ Doppia Coppia ......... 2x             ║");
            System.out.println("║ Coppia J+ ............ 1x              ║");
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
            
            if (!State.deductBalance(bet)) {
                System.out.println("❌ Saldo insufficiente!");
                continue;
            }
            
            // Deal initial hand
            List<VideoPoker.Card> hand = VideoPoker.dealHand();
            
            System.out.println("\n╔════════════════════════════════════════╗");
            System.out.println("║           🃏 LA TUA MANO               ║");
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
            System.out.println("║           🎴 RISULTATO                 ║");
            System.out.println("╠════════════════════════════════════════╣");
            System.out.printf( "║ Mano: %-33s║%n", formatHand(hand));
            System.out.printf( "║ Combinazione: %-25s║%n", result.handName);
            System.out.printf( "║ Moltiplicatore: %-23s║%n", result.multiplier + "x");
            System.out.println("╠════════════════════════════════════════╣");
            
            double guadagno = -bet;
            boolean haVinto = false;
            
            if (result.multiplier > 0) {
                double importoVincita = bet * result.multiplier;
                State.addBalance(importoVincita);
                guadagno = importoVincita - bet;
                haVinto = true;
                System.out.printf("║ ✅ HAI VINTO! +%-24s║%n", String.format("%.2f", guadagno) + "€");
            } else {
                System.out.println("║ ❌ NESSUNA VINCITA!                    ║");
            }
            
            System.out.printf( "║ Nuovo saldo: €%-25s║%n", String.format("%.2f", State.getBalance()));
            System.out.println("╚════════════════════════════════════════╝");
            
            // Registra il risultato
            GameRecord registrazione = new GameRecord("Video Poker", bet, guadagno, haVinto);
            registrazione.setDetails(result.handName);
            Database.recordGameResult(utente.getId(), registrazione);
            
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
        
        utente.setSaldo(State.getBalance());
        Database.saveUsers();
        System.out.println("\n👋 Grazie per aver giocato a Video Poker!");
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
    
    private static String formatHand(List<VideoPoker.Card> hand) {
        StringBuilder sb = new StringBuilder();
        for (VideoPoker.Card card : hand) {
            sb.append(card.toString()).append(" ");
        }
        return sb.toString().trim();
    }
}
