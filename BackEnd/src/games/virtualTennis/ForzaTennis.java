package games.virtualTennis;

import java.util.Map;

public class ForzaTennis {

    private static final Map<String, Integer> STRENGTH = Map.ofEntries(
            Map.entry("Carlos Alcaraz", 99),
            Map.entry("Jannik Sinner", 98),
            Map.entry("Novak Djokovic", 94),
            Map.entry("Daniil Medvedev", 92),
            Map.entry("Alexander Zverev", 91),
            Map.entry("Stefanos Tsitsipas", 89),
            Map.entry("Andrey Rublev", 88),
            Map.entry("Holger Rune", 88),
            Map.entry("Casper Ruud", 87),
            Map.entry("Taylor Fritz", 86),
            Map.entry("Alex de Minaur", 86),
            Map.entry("Hubert Hurkacz", 85),
            Map.entry("Tommy Paul", 85),
            Map.entry("Grigor Dimitrov", 84),
            Map.entry("Matteo Berrettini", 87),
            Map.entry("Lorenzo Musetti", 86),
            Map.entry("Lorenzo Sonego", 83),
            Map.entry("Flavio Cobolli", 81)
    );

    public static int get(String player) {
        return STRENGTH.getOrDefault(player, 85);
    }

    public static java.util.List<String> players() {
        return new java.util.ArrayList<>(STRENGTH.keySet());
    }
}
