package core;

public class State {
    private static double balance = 0;
    private static int xp = 0;
    
    public static double getBalance() {
        return balance;
    }
    
    public static void setBalance(double amount) {
        balance = Math.max(0, amount);
    }
    
    public static void addBalance(double amount) {
        balance += amount;
    }
    
    /** Sottrae {@code amount} dal saldo. @return false se il saldo non basta o importo negativo */
    public static boolean deductBalance(double amount) {
        if (amount < 0) {
            return false;
        }
        if (amount == 0) {
            return true;
        }
        if (!canBet(amount)) {
            return false;
        }
        balance -= amount;
        return true;
    }
    
    public static boolean canBet(double amount) {
        return balance >= amount;
    }
    
    public static int getXp() {
        return xp;
    }
    
    public static void addXp(int amount) {
        xp += amount;
    }
    
    public static void reset() {
        balance = 0;
        xp = 0;
    }
}
