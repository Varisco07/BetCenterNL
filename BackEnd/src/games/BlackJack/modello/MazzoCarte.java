package games.BlackJack.modello;

import java.util.ArrayList;
import java.util.Random;


public class MazzoCarte {
    private ArrayList<Carta> carte;
    private Random generatoreCasuale;

    public MazzoCarte() {
        carte = new ArrayList<Carta>();
        generatoreCasuale = new Random();
        creaMazzo();
        mescola();
    }

    private void creaMazzo() {
        String[] semi = {"Cuori", "Quadri", "Fiori", "Picche"};

        String[] nomi = {
                "Asso", "2", "3", "4", "5", "6", "7",
                "8", "9", "10", "Jack", "Queen", "King"
        };


        int[] valori = {11, 2, 3, 4, 5, 6, 7, 8, 9, 10, 10, 10, 10};
        carte.clear();
        for (String s : semi) {
            for (int j = 0; j < nomi.length; j++) {
                Carta nuovaCarta = new Carta(s, nomi[j], valori[j]);
                carte.add(nuovaCarta);
            }
        }
    }

    public void mescola() {
        int dimensioneMazzo = carte.size();

        for (int i = dimensioneMazzo - 1; i > 0; i--) {
            int posizioneCasuale = generatoreCasuale.nextInt(i + 1);
            Carta cartaTemporanea = carte.get(i);
            carte.set(i, carte.get(posizioneCasuale));
            carte.set(posizioneCasuale, cartaTemporanea);
        }
    }

    public Carta pescaCarta() {
        if (!carte.isEmpty()) {
            return carte.removeFirst();
        }
        return null;
    }

    public int carteRimaste() {
        return carte.size();
    }

    public void ricreaMazzo() {
        creaMazzo();
        mescola();
    }
}