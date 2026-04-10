package BlackJack.giocatori;

import BlackJack.modello.Carta;
import java.util.ArrayList;

public class Giocatore {
    protected String nome;
    protected ArrayList<Carta> manoCarte;

    public Giocatore(String nome) {
        this.nome = nome;
        this.manoCarte = new ArrayList<Carta>();
    }

    public String getNome() {
        return nome;
    }

    public void aggiungiCarta(Carta carta) {
        manoCarte.add(carta);
    }

    public void svuotaMano() {
        manoCarte.clear();
    }

    public int valoreMano() {
        int totale = 0;
        int numeroAssi = 0;
        for (Carta cartaCorrente : manoCarte) {
            totale = totale + cartaCorrente.getValore();
            if (cartaCorrente.isAsso()) {
                numeroAssi = numeroAssi + 1;
            }
        }
        // Se abbiamo sballato (totale > 21) e abbiamo degli Assi,
        // riduciamo il valore di ogni Asso da 11 a 1 (sottraiamo 10)
        while (totale > 21 && numeroAssi > 0) {
            totale = totale - 10;
            numeroAssi = numeroAssi - 1;
        }

        return totale;
    }

    public boolean haSballato() {
        return valoreMano() > 21;
    }

    public boolean haBlackjack() {
        return manoCarte.size() == 2 && valoreMano() == 21;
    }

    public int numeroCarte() {
        return manoCarte.size();
    }

    public Carta getCarta(int indice) {
        return manoCarte.get(indice);
    }

    public void mostraMano() {
        System.out.println("Carte di " + nome + ":");
        for (Carta carta : manoCarte) {
            System.out.println("  " + carta.toString());
        }
        System.out.println("  Punteggio: " + valoreMano());
        System.out.println();
    }
}