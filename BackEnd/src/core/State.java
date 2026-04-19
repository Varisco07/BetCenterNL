package core;

import java.io.*;
import java.nio.file.*;

public class State {
    private static double balance = 0;
    private static int xp = 0;

    // ── Jackpot progressivo ───────────────────────────────────────────────────
    private static final double JACKPOT_START = 12450.0;
    private static double jackpot = JACKPOT_START;
    private static final String JACKPOT_FILE = "data/jackpot.dat";

    static {
        loadJackpot();
    }

    private static void loadJackpot() {
        try {
            File f = new File(JACKPOT_FILE);
            if (f.exists()) {
                String val = new String(Files.readAllBytes(f.toPath())).trim();
                jackpot = Double.parseDouble(val);
            }
        } catch (Exception e) {
            jackpot = JACKPOT_START;
        }
    }

    private static void saveJackpot() {
        try {
            Files.write(Paths.get(JACKPOT_FILE), String.valueOf(jackpot).getBytes());
        } catch (Exception ignored) {}
    }

    public static double getJackpot() {
        return jackpot;
    }

    /** Aggiunge una percentuale della puntata al jackpot (chiamato ad ogni spin slot) */
    public static synchronized void addToJackpot(double betAmount) {
        jackpot += betAmount * 0.03; // 3% di ogni puntata
        jackpot = Math.round(jackpot * 100.0) / 100.0;
        saveJackpot();
    }

    /** Resetta il jackpot dopo una vincita */
    public static synchronized void resetJackpot() {
        jackpot = JACKPOT_START;
        saveJackpot();
    }

    // ── Saldo ─────────────────────────────────────────────────────────────────
    public static double getBalance() {
        return balance;
    }

    public static void setBalance(double amount) {
        balance = Math.max(0, amount);
    }

    public static void addBalance(double amount) {
        balance += amount;
    }

    public static boolean deductBalance(double amount) {
        if (amount < 0) return false;
        if (amount == 0) return true;
        if (!canBet(amount)) return false;
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
