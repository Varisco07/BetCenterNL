package games.virtual;

import java.util.*;

public class ForzaSquadra {

    private static final Map<String, Integer> STRENGTH = new HashMap<>();

    static {

        // ================= SERIE A =================
        STRENGTH.put("Atalanta", 87);
        STRENGTH.put("Bologna", 80);
        STRENGTH.put("Cagliari", 76);
        STRENGTH.put("Como", 73);
        STRENGTH.put("Cremonese", 72);
        STRENGTH.put("Fiorentina", 84);
        STRENGTH.put("Genoa", 78);
        STRENGTH.put("Inter", 91);
        STRENGTH.put("Juventus", 90);
        STRENGTH.put("Lazio", 85);
        STRENGTH.put("Lecce", 75);
        STRENGTH.put("Milan", 89);
        STRENGTH.put("Napoli", 89);
        STRENGTH.put("Parma", 77);
        STRENGTH.put("Pisa", 74);
        STRENGTH.put("Roma", 86);
        STRENGTH.put("Sassuolo", 78);
        STRENGTH.put("Torino", 80);
        STRENGTH.put("Udinese", 79);
        STRENGTH.put("Verona", 77);

        // ================= PREMIER LEAGUE =================
        STRENGTH.put("Arsenal", 90);
        STRENGTH.put("Aston Villa", 83);
        STRENGTH.put("Bournemouth", 78);
        STRENGTH.put("Brentford", 81);
        STRENGTH.put("Brighton", 83);
        STRENGTH.put("Burnley", 76);
        STRENGTH.put("Chelsea", 87);
        STRENGTH.put("Crystal Palace", 80);
        STRENGTH.put("Everton", 79);
        STRENGTH.put("Fulham", 80);
        STRENGTH.put("Leeds", 79);
        STRENGTH.put("Leicester", 81);
        STRENGTH.put("Liverpool", 91);
        STRENGTH.put("Manchester City", 95);
        STRENGTH.put("Manchester United", 86);
        STRENGTH.put("Newcastle", 85);
        STRENGTH.put("Nottingham Forest", 77);
        STRENGTH.put("Tottenham", 86);
        STRENGTH.put("West Ham", 82);
        STRENGTH.put("Wolves", 81);

        // ================= LA LIGA =================
        STRENGTH.put("Alavés", 76);
        STRENGTH.put("Athletic Bilbao", 84);
        STRENGTH.put("Atlético Madrid", 88);
        STRENGTH.put("Barcellona", 92);
        STRENGTH.put("Celta Vigo", 79);
        STRENGTH.put("Elche", 74);
        STRENGTH.put("Espanyol", 78);
        STRENGTH.put("Getafe", 80);
        STRENGTH.put("Girona", 83);
        STRENGTH.put("Levante", 77);
        STRENGTH.put("Maiorca", 78);
        STRENGTH.put("Osasuna", 81);
        STRENGTH.put("Rayo Vallecano", 80);
        STRENGTH.put("Real Betis", 83);
        STRENGTH.put("Real Madrid", 94);
        STRENGTH.put("Real Oviedo", 73);
        STRENGTH.put("Real Sociedad", 84);
        STRENGTH.put("Siviglia", 85);
        STRENGTH.put("Valencia", 82);
        STRENGTH.put("Villarreal", 85);

        // ================= CHAMPIONS LEAGUE =================
        STRENGTH.put("Ajax", 82);
        STRENGTH.put("Arsenal", 90);
        STRENGTH.put("Atalanta", 87);
        STRENGTH.put("Athletic Bilbao", 84);
        STRENGTH.put("Atlético Madrid", 88);
        STRENGTH.put("Barcellona", 92);
        STRENGTH.put("Bayer Leverkusen", 86);
        STRENGTH.put("Bayern Monaco", 93);
        STRENGTH.put("Benfica", 83);
        STRENGTH.put("Borussia Dortmund", 86);
        STRENGTH.put("Chelsea", 87);
        STRENGTH.put("Eintracht Francoforte", 82);
        STRENGTH.put("Galatasaray", 81);
        STRENGTH.put("Inter", 91);
        STRENGTH.put("Juventus", 90);
        STRENGTH.put("Liverpool", 91);
        STRENGTH.put("Manchester City", 95);
        STRENGTH.put("Marsiglia", 83);
        STRENGTH.put("Monaco", 84);
        STRENGTH.put("Napoli", 89);
        STRENGTH.put("Newcastle", 85);
        STRENGTH.put("Olympiacos", 78);
        STRENGTH.put("Paris Saint-Germain", 92);
        STRENGTH.put("PSV Eindhoven", 82);
        STRENGTH.put("Real Madrid", 94);
        STRENGTH.put("Slavia Praga", 77);
        STRENGTH.put("Sporting CP", 82);
        STRENGTH.put("Tottenham", 86);
        STRENGTH.put("Union Saint-Gilloise", 76);
        STRENGTH.put("Villarreal", 85);
    }

    public static int get(String team) {
        return STRENGTH.getOrDefault(team, 80);
    }
}