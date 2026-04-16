package games.roulette;

import java.util.*;
import core.random;

public class ruotaRoulette {

    private static final List<Integer> NUMBERS = Arrays.asList(
            0,26,3,35,12,28,7,29,18,22,9,31,
            14,20,1,33,16,24,5,34,17,6,27,13,
            36,11,30,8,23,10,32,15,19,4,21,2,25
    );

    public int spin() {
        return NUMBERS.get(random.randomInt(0, NUMBERS.size() - 1));
    }

    public String getColor(int n) {
        if (n == 0) return "green";

        int[] red = {
                1,3,5,7,9,12,14,16,18,19,
                21,23,25,27,30,32,34,36
        };

        for (int r : red) if (r == n) return "red";

        return "black";
    }
}