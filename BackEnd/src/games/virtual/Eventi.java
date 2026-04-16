package games.virtual;

public class Eventi {

    public String league;
    public String home;
    public String away;

    public double homeOdd;
    public double drawOdd;
    public double awayOdd;

    // 🆕 RISULTATO PARTITA
    public int homeGoals;
    public int awayGoals;

    public Eventi(String league, String home, String away,
                  double homeOdd, double drawOdd, double awayOdd) {

        this.league = league;
        this.home = home;
        this.away = away;
        this.homeOdd = homeOdd;
        this.drawOdd = drawOdd;
        this.awayOdd = awayOdd;
    }
}