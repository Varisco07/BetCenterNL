package games.BlackJack;

import games.BlackJack.gioco.Blackjack;
public class Main {

    public static void main(String[] args) {
        Blackjack partita = new Blackjack();
        partita.avviaGioco();
    }
}