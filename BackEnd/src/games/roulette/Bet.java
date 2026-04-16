package games.roulette;

public class Bet {
    public String type;
    public int number;
    public double amount;
    public double odds;

    public Bet(String type, int number, double amount, double odds) {
        this.type = type;
        this.number = number;
        this.amount = amount;
        this.odds = odds;
    }
}