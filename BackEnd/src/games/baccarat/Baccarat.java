package games.baccarat;

import java.util.*;

public class Baccarat {
    private static final String[] SUITS = {"♠", "♥", "♦", "♣"};
    private static final String[] RANKS = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
    
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
        
        @Override
        public String toString() {
            return rank + suit;
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
        List<Card> deck = createDeck();
        List<Card> playerHand = new ArrayList<>();
        List<Card> bankerHand = new ArrayList<>();
        
        // Deal initial cards
        playerHand.add(deck.remove(0));
        bankerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        bankerHand.add(deck.remove(0));
        
        int playerValue = calculateValue(playerHand);
        int bankerValue = calculateValue(bankerHand);
        
        // Natural check
        if (playerValue < 8 && bankerValue < 8) {
            // Player third card rule
            if (playerValue <= 5) {
                playerHand.add(deck.remove(0));
                playerValue = calculateValue(playerHand);
            }
            
            // Banker third card rule (simplified)
            if (bankerValue <= 5) {
                bankerHand.add(deck.remove(0));
                bankerValue = calculateValue(bankerHand);
            }
        }
        
        playerValue = calculateValue(playerHand);
        bankerValue = calculateValue(bankerHand);
        
        String winner = "tie";
        if (playerValue > bankerValue) winner = "player";
        else if (bankerValue > playerValue) winner = "banker";
        
        return new BaccaratResult(playerHand, bankerHand, winner);
    }
    
    private static int calculateValue(List<Card> hand) {
        int value = 0;
        for (Card card : hand) {
            value += card.getValue();
        }
        return value % 10;
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
