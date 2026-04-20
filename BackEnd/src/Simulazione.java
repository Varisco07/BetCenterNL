import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.CountDownLatch;

import games.BlackJack.modello.MazzoCarte;
import games.BlackJack.giocatori.GiocatoreUmano;
import games.BlackJack.giocatori.Banco;
import games.roulette.ruotaRoulette;
import core.random;

/**
 * Simulazione statistica: dimostra che alla lunga il banco vince sempre.
 * Ogni gioco gira in un thread separato, 100 partite ciascuno.
 */
public class Simulazione {

    private static final int    NUM_PARTITE = 100;
    private static final int    BET_INT     = 10;
    private static final double BET         = 10.0;

    private static final AtomicInteger bjVinte    = new AtomicInteger();
    private static final AtomicInteger bjPerse    = new AtomicInteger();
    private static final AtomicInteger bjPari     = new AtomicInteger();
    private static final AtomicLong    bjCents    = new AtomicLong();

    private static final AtomicInteger dadiVinte  = new AtomicInteger();
    private static final AtomicInteger dadiPerse  = new AtomicInteger();
    private static final AtomicLong    dadiCents  = new AtomicLong();

    private static final AtomicInteger rouVinte   = new AtomicInteger();
    private static final AtomicInteger rouPerse   = new AtomicInteger();
    private static final AtomicLong    rouCents   = new AtomicLong();

    public static void avvia() {
        // Reset dei contatori
        bjVinte.set(0);
        bjPerse.set(0);
        bjPari.set(0);
        bjCents.set(0);
        
        dadiVinte.set(0);
        dadiPerse.set(0);
        dadiCents.set(0);
        
        rouVinte.set(0);
        rouPerse.set(0);
        rouCents.set(0);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       📊 SIMULAZIONE IN CORSO...       ║");
        System.out.printf("║  %d partite per gioco · puntata €%d   ║%n", NUM_PARTITE, BET_INT);
        System.out.println("╚════════════════════════════════════════╝");

        CountDownLatch latch = new CountDownLatch(3);
        
        // Avvio dei thread per ogni gioco
        new Thread(() -> {
            simulaBlackjack();
            latch.countDown();
        }, "sim-bj").start();
        
        new Thread(() -> {
            simulaDadi();
            latch.countDown();
        }, "sim-dadi").start();
        
        new Thread(() -> {
            simulaRoulette();
            latch.countDown();
        }, "sim-rou").start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        mostraRisultati();
    }

    private static void simulaBlackjack() {
        MazzoCarte mazzo = new MazzoCarte();
        
        for (int i = 0; i < NUM_PARTITE; i++) {
            // Ricrea il mazzo se le carte sono poche
            if (mazzo.carteRimaste() < 15) {
                mazzo.ricreaMazzo();
            }
            
            // Inizializza giocatori
            GiocatoreUmano giocatore = new GiocatoreUmano("Sim", 100_000);
            Banco banco = new Banco();
            
            // Distribuisci le carte iniziali
            giocatore.aggiungiCarta(mazzo.pescaCarta());
            banco.aggiungiCarta(mazzo.pescaCarta());
            giocatore.aggiungiCarta(mazzo.pescaCarta());
            banco.aggiungiCarta(mazzo.pescaCarta());
            
            // Controlla blackjack naturali
            if (giocatore.haBlackjack() && banco.haBlackjack()) {
                bjPari.incrementAndGet();
                continue;
            }
            
            if (giocatore.haBlackjack()) {
                bjVinte.incrementAndGet();
                bjCents.addAndGet(Math.round(BET_INT * 1.5 * 100));
                continue;
            }
            
            if (banco.haBlackjack()) {
                bjPerse.incrementAndGet();
                bjCents.addAndGet(-BET_INT * 100L);
                continue;
            }
            
            // Giocatore pesca fino a 17 o sballato
            while (giocatore.valoreMano() < 17 && !giocatore.haSballato()) {
                giocatore.aggiungiCarta(mazzo.pescaCarta());
            }
            
            if (giocatore.haSballato()) {
                bjPerse.incrementAndGet();
                bjCents.addAndGet(-BET_INT * 100L);
                continue;
            }
            
            // Banco pesca secondo le regole
            while (banco.devePescare()) {
                banco.aggiungiCarta(mazzo.pescaCarta());
            }
            
            // Determina il vincitore
            int valoreGiocatore = giocatore.valoreMano();
            int valoreBanco = banco.valoreMano();
            
            if (valoreBanco > 21 || valoreGiocatore > valoreBanco) {
                bjVinte.incrementAndGet();
                bjCents.addAndGet(BET_INT * 100L);
            } else if (valoreGiocatore < valoreBanco) {
                bjPerse.incrementAndGet();
                bjCents.addAndGet(-BET_INT * 100L);
            } else {
                bjPari.incrementAndGet();
            }
        }
    }

    private static void simulaDadi() {
        for (int i = 0; i < NUM_PARTITE; i++) {
            // Lancia due dadi
            int dado1 = random.randomInt(1, 6);
            int dado2 = random.randomInt(1, 6);
            int somma = dado1 + dado2;
            
            // Pass Line: vinci con 7 o 11
            if (somma == 7 || somma == 11) {
                dadiVinte.incrementAndGet();
                dadiCents.addAndGet(Math.round(BET * 100));
            } else {
                dadiPerse.incrementAndGet();
                dadiCents.addAndGet(-Math.round(BET * 100));
            }
        }
    }

    private static void simulaRoulette() {
        ruotaRoulette ruota = new ruotaRoulette();
        
        for (int i = 0; i < NUM_PARTITE; i++) {
            // Gira la ruota e ottieni il colore
            int numero = ruota.spin();
            String colore = ruota.getColor(numero);
            
            // Punta sul rosso con payout 1.9x
            if (colore.equals("red")) {
                rouVinte.incrementAndGet();
                rouCents.addAndGet(Math.round(BET * 0.9 * 100));
            } else {
                rouPerse.incrementAndGet();
                rouCents.addAndGet(-Math.round(BET * 100));
            }
        }
    }

    private static void mostraRisultati() {
        // Calcola i guadagni
        double bjGuadagno = bjCents.get() / 100.0;
        double dadiGuadagno = dadiCents.get() / 100.0;
        double rouGuadagno = rouCents.get() / 100.0;
        double totaleGuadagno = bjGuadagno + dadiGuadagno + rouGuadagno;
        
        // Calcola statistiche
        double totaleInvestito = NUM_PARTITE * BET * 3;
        double roi = (totaleGuadagno / totaleInvestito) * 100;

        // Formatta i valori per la visualizzazione
        String sBjGuadagno = String.format("%s€%.2f", bjGuadagno >= 0 ? "+" : "", bjGuadagno);
        String sDadiGuadagno = String.format("%s€%.2f", dadiGuadagno >= 0 ? "+" : "", dadiGuadagno);
        String sRouGuadagno = String.format("%s€%.2f", rouGuadagno >= 0 ? "+" : "", rouGuadagno);
        String sTotaleGuadagno = String.format("%s€%.2f", totaleGuadagno >= 0 ? "+" : "-", Math.abs(totaleGuadagno));
        String sInvestito = String.format("€%.2f", totaleInvestito);
        String sRoi = String.format("%.2f%%", roi);
        String sPartite = NUM_PARTITE + " partite per gioco  ·  puntata fissa €" + BET_INT;

        // Mostra i risultati
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.printf("║       📊 RISULTATI SIMULAZIONE                   ║%n");
        System.out.printf("║  %-48s║%n", sPartite);
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║                                                  ║");
        
        // Risultati Blackjack
        System.out.printf("║  🎰 BLACKJACK  (strategia: stai su 17+)   %-7s║%n", "");
        System.out.printf("║    Vinte: %-3d  Perse: %-3d  Pari: %-3d             ║%n",
                bjVinte.get(), bjPerse.get(), bjPari.get());
        System.out.printf("║    Guadagno netto: %-30s║%n", sBjGuadagno);
        System.out.println("║                                                  ║");
        
        // Risultati Dadi
        System.out.printf("║  🎲 DADI  (Pass Line: vinci con 7 o 11)   %-7s║%n", "");
        System.out.printf("║    Vinte: %-3d  Perse: %-3d                        ║%n",
                dadiVinte.get(), dadiPerse.get());
        System.out.printf("║    Guadagno netto: %-30s║%n", sDadiGuadagno);
        System.out.println("║                                                  ║");
        
        // Risultati Roulette
        System.out.printf("║  ⭕ ROULETTE  (rosso, payout 1.9x)        %-7s║%n", "");
        System.out.printf("║    Vinte: %-3d  Perse: %-3d                        ║%n",
                rouVinte.get(), rouPerse.get());
        System.out.printf("║    Guadagno netto: %-30s║%n", sRouGuadagno);
        System.out.println("║                                                  ║");
        
        // Totali
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  💸 Totale investito:   %-25s║%n", sInvestito);
        System.out.printf("║  💰 Guadagno/Perdita:   %-25s║%n", sTotaleGuadagno);
        System.out.printf("║  📉 ROI:                %-25s║%n", sRoi);
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf("║  ⚠️  Alla lunga il banco vince sempre.    %-7s║%n", "");
        System.out.printf("║     Gioca solo per divertimento.          %-7s║%n", "");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
