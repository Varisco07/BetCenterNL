package games.chicken;

import core.State;
import java.util.Random;
import java.util.Scanner;

public class ChickenGame {
    private static final int GRID_SIZE = 5;
    private static final Random random = new Random();
    
    private int chickenPosition;
    private boolean[] cars;
    private double bet;
    private double currentMultiplier;
    private int level;
    private boolean gameOver;
    private boolean cashOut;
    
    public ChickenGame(double bet) {
        this.bet = bet;
        this.chickenPosition = 0;
        this.cars = new boolean[GRID_SIZE];
        this.currentMultiplier = 1.0;
        this.level = 0;
        this.gameOver = false;
        this.cashOut = false;
        generateCars();
    }
    
    private void generateCars() {
        // Genera auto casuali, aumenta difficoltà con il livello
        int numCars = Math.min(1 + level / 2, GRID_SIZE - 1);
        cars = new boolean[GRID_SIZE];
        
        for (int i = 0; i < numCars; i++) {
            int pos;
            do {
                pos = random.nextInt(GRID_SIZE);
            } while (cars[pos]);
            cars[pos] = true;
        }
    }
    
    public boolean move(int position) {
        if (gameOver || cashOut) return false;
        
        if (position < 0 || position >= GRID_SIZE) {
            return false;
        }
        
        chickenPosition = position;
        
        // Controlla se ha colpito un'auto
        if (cars[position]) {
            gameOver = true;
            return false;
        }
        
        // Successo! Aumenta moltiplicatore
        level++;
        currentMultiplier *= 1.5;
        
        // Genera nuove auto per il prossimo livello
        generateCars();
        
        return true;
    }
    
    public double cashOut() {
        if (gameOver) return 0;
        
        cashOut = true;
        double winAmount = bet * currentMultiplier;
        return winAmount;
    }
    
    public void displayGrid() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║     🐔 CHICKEN CROSS THE ROAD         ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf("║ Livello: %d  │  Moltiplicatore: %.2fx  ║%n", level, currentMultiplier);
        System.out.printf("║ Puntata: €%.2f  │  Vincita: €%.2f      ║%n", bet, bet * currentMultiplier);
        System.out.println("╠════════════════════════════════════════╣");
        
        // Mostra la griglia
        System.out.println("║                                        ║");
        for (int i = 0; i < GRID_SIZE; i++) {
            System.out.print("║     ");
            for (int j = 0; j < GRID_SIZE; j++) {
                if (j == i) {
                    System.out.print("[" + (j + 1) + "] ");
                } else {
                    System.out.print("    ");
                }
            }
            System.out.println("    ║");
        }
        System.out.println("║                                        ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println("Scegli una posizione (1-5) o 0 per incassare");
    }
    
    public boolean isGameOver() {
        return gameOver;
    }
    
    public boolean isCashOut() {
        return cashOut;
    }
    
    public double getCurrentWin() {
        return bet * currentMultiplier;
    }
    
    public double getMultiplier() {
        return currentMultiplier;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean[] getCars() {
        return cars;
    }
}
