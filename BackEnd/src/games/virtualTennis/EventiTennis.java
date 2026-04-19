package games.virtualTennis;

public class EventiTennis {
    public String league;
    public String home;
    public String away;
    public double homeOdd;
    public double awayOdd;

    public EventiTennis(String league, String home, String away, double homeOdd, double awayOdd) {
        this.league = league;
        this.home = home;
        this.away = away;
        this.homeOdd = homeOdd;
        this.awayOdd = awayOdd;
    }
}
