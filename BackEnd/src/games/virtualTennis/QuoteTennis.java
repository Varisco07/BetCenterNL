package games.virtualTennis;

public class QuoteTennis {

    public static double[] generate(String home, String away) {
        int forzaCasa = ForzaTennis.get(home);
        int forzaTrasferta = ForzaTennis.get(away);

        double diff = forzaCasa - forzaTrasferta;

        double probCasa = 0.50 + (diff * 0.006);
        probCasa = clamp(probCasa, 0.20, 0.80);
        double probTrasferta = 1.0 - probCasa;

        double quotaCasa = round(1.0 / probCasa);
        double quotaTrasferta = round(1.0 / probTrasferta);
        return new double[]{quotaCasa, quotaTrasferta};
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
