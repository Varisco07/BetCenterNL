package games.virtualCani;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForzaCani {

    private static final Map<String, Integer> STRENGTH = Map.ofEntries(
            Map.entry("Romeo Magico", 96),
            Map.entry("Signet Ace", 92),
            Map.entry("Droopys Addition", 90),
            Map.entry("Newinn Taylor", 91),
            Map.entry("Coolavanny Hoffa", 94),
            Map.entry("King Sheeran", 88),
            Map.entry("Aayamza Royale", 93),
            Map.entry("Ballymac Finn", 89),
            Map.entry("Newinn Session", 90),
            Map.entry("Glengar Bale", 87),
            Map.entry("Darbys Delight", 86),
            Map.entry("Knocknaboul Syd", 85),
            Map.entry("Broadstrand Bono", 91),
            Map.entry("Crafty Kokoro", 88),
            Map.entry("Gurteen Feather", 84)
    );

    public static int get(String dog) {
        return STRENGTH.getOrDefault(dog, 85);
    }

    public static List<String> dogs() {
        return new ArrayList<>(STRENGTH.keySet());
    }
}
