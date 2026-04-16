package games.BlackJack.modello;


public class Carta {
    private String seme;
    private String nome;
    private int valore;

    // Codici colore ANSI (funzionano tipicamente su terminali che li supportano)
    private static final String ROSSO = "\u001B[31m";
    private static final String VERDE = "\u001B[32m";
    private static final String BLU = "\u001B[34m";
    private static final String BIANCO = "\u001B[37m";
    private static final String RESET = "\u001B[0m";

    public Carta(String seme, String nome, int valore) {
        this.seme = seme;
        this.nome = nome;
        this.valore = valore;
    }
    public String getSeme() {
        return seme;
    }

    public String getNome() {
        return nome;
    }


    public int getValore() {
        return valore;
    }

    public boolean isAsso() {
        return nome.equals("Asso");
    }

    public String toString() {
        String simboloSeme;
        if (seme.equals("Cuori")) {
            simboloSeme = "♥";
        } else if (seme.equals("Quadri")) {
            simboloSeme = "♦";
        } else if (seme.equals("Fiori")) {
            simboloSeme = "♣";
        } else if (seme.equals("Picche")) {
            simboloSeme = "♠";
        } else {
            simboloSeme = "?";
        }

        String coloreSeme;
        if (seme.equals("Cuori") || seme.equals("Quadri")) {
            coloreSeme = ROSSO;
        } else if (seme.equals("Fiori") || seme.equals("Picche")) {
            coloreSeme = BLU;
        } else {
            coloreSeme = BIANCO;
        }

        return coloreSeme + nome + simboloSeme + RESET;
    }
}