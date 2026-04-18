package core;

import java.io.*;

public class State {

    private static double balance = loadBalance();

    private static final String FILE = "balance.dat";

    public static double getBalance() {
        return balance;
    }

    public static void setBalance(double amount) {
        balance = amount;
        saveBalance();
    }

    public static boolean deductBalance(double amount) {
        if (balance >= amount) {
            balance -= amount;
            saveBalance();
            return true;
        }
        return false;
    }

    public static void addBalance(double amount) {
        balance += amount;
        saveBalance();
    }

    private static void saveBalance() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            pw.println(balance);
        } catch (Exception e) {
            System.out.println("Errore salvataggio saldo");
        }
    }

    private static double loadBalance() {
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            return Double.parseDouble(br.readLine());
        } catch (Exception e) {
            return 1000; // default
        }
    }
}