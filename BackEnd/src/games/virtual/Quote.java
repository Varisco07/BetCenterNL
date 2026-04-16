package games.virtual;

public class Quote {

    public static double[] generate(String home, String away) {

        int homeStr = ForzaSquadra.get(home);
        int awayStr = ForzaSquadra.get(away);

        double diff = homeStr - awayStr;

        // probabilità base
        double homeProb = 0.45 + (diff * 0.01);
        double awayProb = 0.45 - (diff * 0.01);
        double drawProb = 0.25;

        // normalizza
        double sum = homeProb + awayProb + drawProb;
        homeProb /= sum;
        awayProb /= sum;
        drawProb /= sum;

        // converte in quote
        double homeOdd = round(1 / homeProb);
        double drawOdd = round(1 / drawProb);
        double awayOdd = round(1 / awayProb);

        return new double[]{homeOdd, drawOdd, awayOdd};
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}