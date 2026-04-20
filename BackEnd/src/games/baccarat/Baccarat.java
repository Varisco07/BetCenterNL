package games.baccarat;

import java.util.*;

public class Baccarat {
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    
    // ANSI Color codes
    private static final String RED = "\u001B[31m";
    private static final String BLACK = "\u001B[30m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    
    public static class Card {
        public String rank;
        public String suit;
        
        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }
        
        public int getValue() {
            if (rank.equals("10") || rank.equals("J") || rank.equals("Q") || rank.equals("K")) return 0;
            if (rank.equals("A")) return 1;
            return Integer.parseInt(rank);
        }
        
        public boolean isRed() {
            return suit.equals("♥") || suit.equals("♦");
        }
        
        @Override
        public String toString() {
            return rank + suit;
        }
        
        public String toColoredString() {
            String color = isRed() ? RED : BLACK;
            return color + BOLD + rank + suit + RESET;
        }
    }
    
    public static class BaccaratResult {
        public List<Card> playerHand;
        public List<Card> bankerHand;
        public int playerValue;
        public int bankerValue;
        public String winner; // "player", "banker", "tie"
        public double payout;
        
        public BaccaratResult(List<Card> playerHand, List<Card> bankerHand, String winner) {
            this.playerHand = playerHand;
            this.bankerHand = bankerHand;
            this.playerValue = calculateValue(playerHand);
            this.bankerValue = calculateValue(bankerHand);
            this.winner = winner;
            
            if (winner.equals("player")) this.payout = 2.0;
            else if (winner.equals("banker")) this.payout = 1.95;
            else this.payout = 9.0;
        }
    }
    
    public static BaccaratResult play(String betType) {
        List<Card> mazzo = createDeck();
        List<Card> manoGiocatore = new ArrayList<>();
        List<Card> manoBanco = new ArrayList<>();
        
        // Deal initial cards
        manoGiocatore.add(mazzo.remove(0));
        manoBanco.add(mazzo.remove(0));
        manoGiocatore.add(mazzo.remove(0));
        manoBanco.add(mazzo.remove(0));
        
        int punteggioGiocatore = calculateValue(manoGiocatore);
        int puntataBanco = calculateValue(manoBanco);
        
        // Natural check
        if (punteggioGiocatore < 8 && puntataBanco < 8) {
            // Player third card rule
            if (punteggioGiocatore <= 5) {
                manoGiocatore.add(mazzo.remove(0));
                punteggioGiocatore = calculateValue(manoGiocatore);
            }
            
            // Banker third card rule (simplified)
            if (puntataBanco <= 5) {
                manoBanco.add(mazzo.remove(0));
                puntataBanco = calculateValue(manoBanco);
            }
        }
        
        punteggioGiocatore = calculateValue(manoGiocatore);
        puntataBanco = calculateValue(manoBanco);
        
        String vincitore = "tie";
        if (punteggioGiocatore > puntataBanco) vincitore = "player";
        else if (puntataBanco > punteggioGiocatore) vincitore = "banker";
        
        return new BaccaratResult(manoGiocatore, manoBanco, vincitore);
    }
    
    private static int calculateValue(List<Card> mano) {
        int valore = 0;
        for (Card card : mano) {
            valore += card.getValue();
        }
        return valore % 10;
    }
    
    private static List<Card> createDeck() {
        List<Card> mazzo = new ArrayList<>();
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                mazzo.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(mazzo);
        return mazzo;
    }
    
    public static void printGameRules() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║            " + CYAN + BOLD + "🎴 BACCARAT - REGOLE DEL GIOCO" + RESET + "            ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Scommetti su GIOCATORE, BANCO o PAREGGIO              ║");
        System.out.println("║  2. Vengono distribuite 2 carte a Giocatore e Banco       ║");
        System.out.println("║  3. Il valore delle carte viene calcolato (modulo 10)     ║");
        System.out.println("║  4. Può essere pescata una terza carta (regole fisse)     ║");
        System.out.println("║  5. Vince chi si avvicina di più a 9                       ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "VALORI DELLE CARTE:" + RESET + "                                       ║");
        System.out.println("║  • Asso = 1 punto                                          ║");
        System.out.println("║  • 2-9 = valore nominale                                   ║");
        System.out.println("║  • 10, J, Q, K = 0 punti                                   ║");
        System.out.println("║  • Totale > 9: si conta solo l'ultima cifra                ║");
        System.out.println("║    (es: 7+8=15 → vale 5)                                   ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "PAGAMENTI:" + RESET + "                                                ║");
        System.out.println("║  • Giocatore vince  → 2.00x (1:1)                          ║");
        System.out.println("║  • Banco vince      → 1.95x (1:1 meno 5% commissione)     ║");
        System.out.println("║  • Pareggio         → 9.00x (8:1)                          ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "⚠️  GIOCO RESPONSABILE:" + RESET + "                                   ║");
        System.out.println("║  • Stabilisci un budget e rispettalo                       ║");
        System.out.println("║  • Non cercare di recuperare le perdite                    ║");
        System.out.println("║  • Gioca per divertimento, non per necessità               ║");
        System.out.println("║  • Chiedi aiuto se il gioco diventa un problema            ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
