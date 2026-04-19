package games.virtualCani;

import java.util.List;

public class EventoCani {
    public final String raceName;
    public final List<String> dogs;
    public final List<Double> odds;

    public EventoCani(String raceName, List<String> dogs, List<Double> odds) {
        this.raceName = raceName;
        this.dogs = dogs;
        this.odds = odds;
    }
}
