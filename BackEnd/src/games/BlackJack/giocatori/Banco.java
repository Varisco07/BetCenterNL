package games.BlackJack.giocatori;

public class Banco extends Giocatore {
    private static final int SOGLIA_BANCO = 17;

    public Banco() {
        super("Banco");
    }

    public boolean devePescare() {
        return valoreMano() < SOGLIA_BANCO;
    }

    public void mostraManoConCartaCoperta() {
        System.out.println("Carte del Banco:");
        if (!manoCarte.isEmpty()) {
            System.out.println("  " + manoCarte.getFirst().toString());
        }
        for (int i = 1; i < manoCarte.size(); i++) {
            System.out.println("  [Carta coperta]");
        }
        System.out.println();
    }
}