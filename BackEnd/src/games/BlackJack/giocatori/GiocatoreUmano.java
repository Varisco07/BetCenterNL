package games.BlackJack.giocatori;
public class GiocatoreUmano extends Giocatore {
    // Codici colore ANSI per il terminale
    private static final String ROSSO = "\u001B[31m";
    private static final String VERDE = "\u001B[32m";
    private static final String GIALLO = "\u001B[33m";
    private static final String RESET = "\u001B[0m";

    private int creditiAttuali;
    private int maniVinte;
    private int maniPerse;
    private int maniPareggiate;
    private int totaleCreditiVinti;
    private int totaleCreditiPersi;

    public GiocatoreUmano(String nome, int creditiIniziali) {
        super(nome);
        this.creditiAttuali = creditiIniziali;
        this.maniVinte = 0;
        this.maniPerse = 0;
        this.maniPareggiate = 0;
        this.totaleCreditiVinti = 0;
        this.totaleCreditiPersi = 0;
    }
    public int getCreditiAttuali() {
        return creditiAttuali;
    }
    
    public int getManiVinte() {
        return maniVinte;
    }
    
    public int getManiPerse() {
        return maniPerse;
    }
    
    public int getTotaleCreditiVinti() {
        return totaleCreditiVinti;
    }
    
    public int getTotaleCreditiPersi() {
        return totaleCreditiPersi;
    }
    
    public int maniGiocate() {
        return maniVinte + maniPerse + maniPareggiate;
    }
    public void aggiungiCrediti(int quantita) {
        creditiAttuali = creditiAttuali + quantita;
        totaleCreditiVinti = totaleCreditiVinti + quantita;
    }
    public void rimuoviCrediti(int quantita) {
        creditiAttuali = creditiAttuali - quantita;
        totaleCreditiPersi = totaleCreditiPersi + quantita;
    }
    public boolean puoPuntare(int puntata) {
        return puntata > 0 && puntata <= creditiAttuali;
    }
    public boolean haFinitoCrediti() {
        return creditiAttuali <= 0;
    }
    public void registraVittoria() {
        maniVinte = maniVinte + 1;
    }

    public void registraSconfitta() {
        maniPerse = maniPerse + 1;
    }

    public void registraPareggio() {
        maniPareggiate = maniPareggiate + 1;
    }

    public void stampaStatistiche() {
        int totaliGiocate = maniGiocate();
        double percentualeVittoria = 0;
        double percentualeSconfitta = 0;

        if (totaliGiocate > 0) {
            percentualeVittoria = Math.round(((double) (maniVinte * 100) / totaliGiocate)*10.0)/ 10.0;
            percentualeSconfitta = Math.round(((double) (maniPerse * 100) / totaliGiocate)*10.0)/ 10.0;
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("       STATISTICHE DELLA PARTITA");
        System.out.println("==============================================");
        System.out.println();
        System.out.println("  Mani giocate:    " + totaliGiocate);
        System.out.println("  Mani vinte:      " + VERDE + maniVinte + RESET);
        System.out.println("  Mani perse:      " + ROSSO + maniPerse + RESET);
        System.out.println("  Mani pareggiate: " + GIALLO + maniPareggiate + RESET);
        System.out.println();
        System.out.println("  Percentuale di vittoria:  " + VERDE + percentualeVittoria + " %" + RESET);
        System.out.println("  Percentuale di sconfitta: " + ROSSO + percentualeSconfitta + " %" + RESET);
        System.out.println();
        System.out.println("  Totale crediti vinti: " + VERDE + totaleCreditiVinti + RESET);
        System.out.println("  Totale crediti persi: " + ROSSO + totaleCreditiPersi + RESET);

        String coloreSaldo;
        if (creditiAttuali > 0) {
            coloreSaldo = VERDE;
        } else if (creditiAttuali == 0) {
            coloreSaldo = GIALLO;
        } else {
            coloreSaldo = ROSSO;
        }
        System.out.println("  Saldo attuale:        " + coloreSaldo + creditiAttuali + RESET);
        System.out.println();
        System.out.println("==============================================");
    }
}