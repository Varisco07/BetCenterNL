package games.virtualBasket;

import java.util.*;

public class forzaNBA {

    private static final Map<String, Integer> STRENGTH = new HashMap<>();

    static {

        // TOP TEAMS
        STRENGTH.put("Boston Celtics", 96);
        STRENGTH.put("Denver Nuggets", 95);
        STRENGTH.put("Milwaukee Bucks", 94);
        STRENGTH.put("Golden State Warriors", 92);
        STRENGTH.put("Phoenix Suns", 91);
        STRENGTH.put("Los Angeles Lakers", 90);
        STRENGTH.put("Los Angeles Clippers", 89);
        STRENGTH.put("Philadelphia 76ers", 90);
        STRENGTH.put("Miami Heat", 89);
        STRENGTH.put("Dallas Mavericks", 90);
        STRENGTH.put("Minnesota Timberwolves", 88);
        STRENGTH.put("Oklahoma City Thunder", 87);
        STRENGTH.put("New York Knicks", 86);
        STRENGTH.put("Cleveland Cavaliers", 85);
        STRENGTH.put("Sacramento Kings", 85);
        STRENGTH.put("Atlanta Hawks", 82);
        STRENGTH.put("Toronto Raptors", 81);
        STRENGTH.put("Chicago Bulls", 80);
        STRENGTH.put("Brooklyn Nets", 80);
        STRENGTH.put("New Orleans Pelicans", 83);
        STRENGTH.put("Charlotte Hornets", 75);
        STRENGTH.put("Washington Wizards", 74);
        STRENGTH.put("Detroit Pistons", 72);
        STRENGTH.put("San Antonio Spurs", 78);
        STRENGTH.put("Houston Rockets", 77);
        STRENGTH.put("Utah Jazz", 78);
        STRENGTH.put("Orlando Magic", 79);
        STRENGTH.put("Portland Trail Blazers", 76);
        STRENGTH.put("Indiana Pacers", 80);
        STRENGTH.put("Memphis Grizzlies", 82);
    }

    public static int get(String team) {
        return STRENGTH.getOrDefault(team, 80);
    }
}