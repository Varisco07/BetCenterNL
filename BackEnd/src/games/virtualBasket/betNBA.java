package games.virtualBasket;

public class betNBA {

    public static double[] generate(String home, String away) {

        int h = forzaNBA.get(home);
        int a = forzaNBA.get(away);

        double diff = h - a;

        double homeProb = 0.52 + (diff * 0.005);
        double awayProb = 1 - homeProb;

        homeProb = clamp(homeProb, 0.15, 0.85);
        awayProb = 1 - homeProb;

        double homeOdd = round(1 / homeProb);
        double awayOdd = round(1 / awayProb);

        return new double[]{homeOdd, awayOdd};
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
