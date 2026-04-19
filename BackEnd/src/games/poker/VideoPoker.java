package games.poker;

import java.util.*;

public class VideoPoker {
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static final Map<String, Integer> RANK_VALUES = new HashMap<>();
    
    // ANSI Color codes per le carte
    private static final String RED = "\u001B[31m";
    private static final String BLACK = "\u001B[30m";
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN = "\u001B[32m";
    
    static {
        for (int i = 0; i < RANKS.length; i++) {
            RANK_VALUES.put(RANKS[i], i + 2);
        }
    }
    
    public static class Card {
        public String rank;
        public String suit;
        
        public Card(String rank, String suit) {
            this.rank = rank;
            this.suit = suit;
        }
        
        public int getValue() {
            return RANK_VALUES.getOrDefault(rank, 0);
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
    
    public static class PokerResult {
        public List<Card> hand;
        public String handName;
        public int multiplier;
        
        public PokerResult(List<Card> hand, String handName, int multiplier) {
            this.hand = hand;
            this.handName = handName;
            this.multiplier = multiplier;
        }
    }
    
    public static PokerResult evaluateHand(List<Card> hand) {
        List<Integer> values = new ArrayList<>();
        Set<String> suits = new HashSet<>();
        Map<Integer, Integer> rankCounts = new HashMap<>();
        
        for (Card card : hand) {
            int val = card.getValue();
            values.add(val);
            suits.add(card.suit);
            rankCounts.put(val, rankCounts.getOrDefault(val, 0) + 1);
        }
        
        Collections.sort(values);
        
        boolean flush = suits.size() == 1;
        boolean straight = isStraight(values);
        List<Integer> counts = new ArrayList<>(rankCounts.values());
        Collections.sort(counts, Collections.reverseOrder());
        
        boolean royal = straight && flush && values.get(0) >= 10;
        
        if (royal) return new PokerResult(hand, "Royal Flush", 800);
        if (straight && flush) return new PokerResult(hand, "Straight Flush", 50);
        if (counts.get(0) == 4) return new PokerResult(hand, "Poker (4 uguali)", 25);
        if (counts.get(0) == 3 && counts.get(1) == 2) return new PokerResult(hand, "Full House", 9);
        if (flush) return new PokerResult(hand, "Colore (Flush)", 6);
        if (straight) return new PokerResult(hand, "Scala (Straight)", 4);
        if (counts.get(0) == 3) return new PokerResult(hand, "Tris", 3);
        if (counts.get(0) == 2 && counts.get(1) == 2) return new PokerResult(hand, "Doppia Coppia", 2);
        if (counts.get(0) == 2) {
            for (Integer val : rankCounts.keySet()) {
                if (rankCounts.get(val) == 2 && val >= 11) {
                    return new PokerResult(hand, "Coppia J+", 1);
                }
            }
        }
        
        return new PokerResult(hand, "Nessuna Vincita", 0);
    }
    
    private static boolean isStraight(List<Integer> values) {
        if (values.size() != 5) return false;
        for (int i = 1; i < 5; i++) {
            if (values.get(i) != values.get(i-1) + 1) return false;
        }
        return true;
    }
    
    public static List<Card> dealHand() {
        List<Card> deck = createDeck();
        List<Card> hand = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            hand.add(deck.remove(0));
        }
        return hand;
    }
    
    public static List<Card> drawCards(List<Card> currentHand, boolean[] held) {
        List<Card> deck = createDeck();
        List<Card> newHand = new ArrayList<>(currentHand);
        
        for (int i = 0; i < 5; i++) {
            if (!held[i]) {
                newHand.set(i, deck.remove(0));
            }
        }
        
        return newHand;
    }
    
    private static List<Card> createDeck() {
        List<Card> deck = new ArrayList<>();
        for (String suit : SUITS) {
            for (String rank : RANKS) {
                deck.add(new Card(rank, suit));
            }
        }
        Collections.shuffle(deck);
        return deck;
    }
    
    public static void printGameRules() {
        System.out.println("\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║           " + CYAN + BOLD + "🎰 VIDEO POKER - REGOLE DEL GIOCO" + RESET + "           ║");
        System.out.println("╠════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "COME SI GIOCA:" + RESET + "                                            ║");
        System.out.println("║  1. Piazza la tua puntata                                  ║");
        System.out.println("║  2. Ricevi 5 carte iniziali                                ║");
        System.out.println("║  3. Scegli quali carte tenere (hold)                       ║");
        System.out.println("║  4. Le carte non tenute vengono sostituite                 ║");
        System.out.println("║  5. Vinci in base alla combinazione finale                 ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + GREEN + "COMBINAZIONI VINCENTI (Moltiplicatori):" + RESET + "                  ║");
        System.out.println("║  • Royal Flush (A-K-Q-J-10 stesso seme)    → 800x         ║");
        System.out.println("║  • Straight Flush (scala colore)           → 50x          ║");
        System.out.println("║  • Poker (4 carte uguali)                  → 25x          ║");
        System.out.println("║  • Full House (tris + coppia)              → 9x           ║");
        System.out.println("║  • Flush (5 carte stesso seme)             → 6x           ║");
        System.out.println("║  • Straight (scala)                        → 4x           ║");
        System.out.println("║  • Tris (3 carte uguali)                   → 3x           ║");
        System.out.println("║  • Doppia Coppia                           → 2x           ║");
        System.out.println("║  • Coppia J+ (J, Q, K, A)                  → 1x           ║");
        System.out.println("║                                                            ║");
        System.out.println("║  " + YELLOW + "⚠️  GIOCO RESPONSABILE:" + RESET + "                                   ║");
        System.out.println("║  • Stabilisci un budget prima di giocare                   ║");
        System.out.println("║  • Non inseguire le perdite                                ║");
        System.out.println("║  • Il gioco deve rimanere un divertimento                  ║");
        System.out.println("║  • Se hai problemi, cerca aiuto professionale              ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }
}
