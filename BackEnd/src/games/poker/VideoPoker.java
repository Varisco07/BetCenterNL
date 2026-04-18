package games.poker;

import java.util.*;

public class VideoPoker {
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
    private static final Map<String, Integer> RANK_VALUES = new HashMap<>();
    
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
        
        @Override
        public String toString() {
            return rank + suit;
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
}
