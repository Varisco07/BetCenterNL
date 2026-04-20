package games.virtual;

public class Quote {

    public static double[] generate(String home, String away) {

        int homeStr = ForzaSquadra.get(home);
        int awayStr = ForzaSquadra.get(away);

        double diff = homeStr - awayStr;

        // probabilità base
        double probCasa = 0.45 + (diff * 0.01);
        double probTrasferta = 0.45 - (diff * 0.01);
        double probPareggio = 0.25;

        // normalizza
        double somma = probCasa + probTrasferta + probPareggio;
        probCasa /= somma;
        probTrasferta /= somma;
        probPareggio /= somma;

        // converte in quote
        double quotaCasa = round(1 / probCasa);
        double quotaPareggio = round(1 / probPareggio);
        double quotaTrasferta = round(1 / probTrasferta);

        return new double[]{quotaCasa, quotaPareggio, quotaTrasferta};
    }

    private static double round(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}