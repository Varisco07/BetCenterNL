package games.virtualCavalli;

import java.util.List;

public class EventoCorsa {
    public final String raceName;
    public final List<String> horses;
    public final List<Double> odds;

    public EventoCorsa(String raceName, List<String> horses, List<Double> odds) {
        this.raceName = raceName;
        this.horses = horses;
        this.odds = odds;
    }
}
