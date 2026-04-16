package core;

import java.util.Random;

public class random {

    private static final Random rand = new Random();

    public static int randomInt(int min, int max) {
        return rand.nextInt(max - min + 1) + min;
    }

    public static double randomDouble(double min, double max) {
        return min + (max - min) * rand.nextDouble();
    }
}