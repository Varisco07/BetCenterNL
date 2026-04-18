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

    // Costruttore per puntate senza numero specifico
    public Bet(String type, double amount, double odds) {
        this(type, -1, amount, odds);
    }

    // Factory methods per creare puntate comuni
    public static Bet onNumber(int number, double amount) {
        return new Bet("number", number, amount, 35.0);
    }

    public static Bet onRed(double amount) {
        return new Bet("red", amount, 1.0);
    }

    public static Bet onBlack(double amount) {
        return new Bet("black", amount, 1.0);
    }

    public static Bet onEven(double amount) {
        return new Bet("even", amount, 1.0);
    }

    public static Bet onOdd(double amount) {
        return new Bet("odd", amount, 1.0);
    }

    public static Bet onLow(double amount) {
        return new Bet("low", amount, 1.0);
    }

    public static Bet onHigh(double amount) {
        return new Bet("high", amount, 1.0);
    }

    public static Bet onDozen1(double amount) {
        return new Bet("dozen1", amount, 2.0);
    }

    public static Bet onDozen2(double amount) {
        return new Bet("dozen2", amount, 2.0);
    }

    public static Bet onDozen3(double amount) {
        return new Bet("dozen3", amount, 2.0);
    }

    public static Bet onColumn1(double amount) {
        return new Bet("column1", amount, 2.0);
    }

    public static Bet onColumn2(double amount) {
        return new Bet("column2", amount, 2.0);
    }

    public static Bet onColumn3(double amount) {
        return new Bet("column3", amount, 2.0);
    }

    @Override
    public String toString() {
        if (type.equals("number")) {
            return String.format("Numero %d: €%.2f (35:1)", number, amount);
        } else {
            return String.format("%s: €%.2f (%.0f:1)", 
                type.substring(0, 1).toUpperCase() + type.substring(1), 
                amount, odds);
        }
    }
}