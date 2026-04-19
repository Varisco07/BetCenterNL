package games.virtualCavalli;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForzaCavalli {

    private static final Map<String, Integer> STRENGTH = Map.ofEntries(
            Map.entry("Equinox", 99),
            Map.entry("Flightline", 98),
            Map.entry("August Rodin", 95),
            Map.entry("White Abarrio", 94),
            Map.entry("Arcangelo", 93),
            Map.entry("Fierceness", 92),
            Map.entry("City of Troy", 91),
            Map.entry("Derma Sotogake", 89),
            Map.entry("Continuar", 88),
            Map.entry("Luxembourg", 88),
            Map.entry("Elite Power", 87),
            Map.entry("Art Collector", 86)
    );

    public static int get(String horse) {
        return STRENGTH.getOrDefault(horse, 85);
    }

    public static List<String> horses() {
        return new ArrayList<>(STRENGTH.keySet());
    }
}
