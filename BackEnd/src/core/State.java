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
    
    public static void deductBalance(double amount) {
        balance = Math.max(0, balance - amount);
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
