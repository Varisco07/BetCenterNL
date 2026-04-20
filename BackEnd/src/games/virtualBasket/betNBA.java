package games.virtualBasket;

public class betNBA {

    public static double[] generate(String home, String away) {

        int h = forzaNBA.get(home);
        int a = forzaNBA.get(away);

        double diff = h - a;

        double probCasa = 0.52 + (diff * 0.005);
        double probTrasferta = 1 - probCasa;

        probCasa = clamp(probCasa, 0.15, 0.85);
        probTrasferta = 1 - probCasa;

        double quotaCasa = round(1 / probCasa);
        double quotaTrasferta = round(1 / probTrasferta);

        return new double[]{quotaCasa, quotaTrasferta};
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
