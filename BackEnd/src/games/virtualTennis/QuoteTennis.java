package games.virtualTennis;

public class QuoteTennis {

    public static double[] generate(String home, String away) {
        int homeStrength = ForzaTennis.get(home);
        int awayStrength = ForzaTennis.get(away);

        double diff = homeStrength - awayStrength;

        double homeProb = 0.50 + (diff * 0.006);
        homeProb = clamp(homeProb, 0.20, 0.80);
        double awayProb = 1.0 - homeProb;

        double homeOdd = round(1.0 / homeProb);
        double awayOdd = round(1.0 / awayProb);
        return new double[]{homeOdd, awayOdd};
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
