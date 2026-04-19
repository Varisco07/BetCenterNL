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
 * Nessun input utente, nessuna animazione, nessun Thread.sleep.
 */
public class Simulazione {

    private static final int    NUM_PARTITE = 100;
    private static final int    BET_INT     = 10;      // puntata intera per Blackjack
    private static final double BET         = 10.0;    // puntata per Dadi e Roulette

    // ── contatori thread-safe ────────────────────────────────────────────────
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

    // ────────────────────────────────────────────────────────────────────────

    public static void avvia() {
        // reset
        bjVinte.set(0);   bjPerse.set(0);   bjPari.set(0);   bjCents.set(0);
        dadiVinte.set(0); dadiPerse.set(0); dadiCents.set(0);
        rouVinte.set(0);  rouPerse.set(0);  rouCents.set(0);

        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       📊 SIMULAZIONE IN CORSO...       ║");
        System.out.printf( "║  %d partite per gioco · puntata €%d   ║%n", NUM_PARTITE, BET_INT);
        System.out.println("╚════════════════════════════════════════╝");

        CountDownLatch latch = new CountDownLatch(3);

        new Thread(() -> { simulaBlackjack(); latch.countDown(); }, "sim-bj").start();
        new Thread(() -> { simulaDadi();      latch.countDown(); }, "sim-dadi").start();
        new Thread(() -> { simulaRoulette();  latch.countDown(); }, "sim-rou").start();

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        mostraRisultati();
    }

    // ── BLACKJACK ────────────────────────────────────────────────────────────
    // Strategia di base: stai su 17+, peschi su 16 o meno.
    private static void simulaBlackjack() {
        MazzoCarte mazzo = new MazzoCarte();

        for (int i = 0; i < NUM_PARTITE; i++) {
            if (mazzo.carteRimaste() < 15) mazzo.ricreaMazzo();

            GiocatoreUmano g = new GiocatoreUmano("Sim", 100_000);
            Banco b = new Banco();

            g.aggiungiCarta(mazzo.pescaCarta());
            b.aggiungiCarta(mazzo.pescaCarta());
            g.aggiungiCarta(mazzo.pescaCarta());
            b.aggiungiCarta(mazzo.pescaCarta());

            // blackjack naturale
            if (g.haBlackjack() && b.haBlackjack()) {
                bjPari.incrementAndGet();
                continue;
            }
            if (g.haBlackjack()) {
                bjVinte.incrementAndGet();
                // payout 3:2
                bjCents.addAndGet(Math.round(BET_INT * 1.5 * 100));
                continue;
            }
            if (b.haBlackjack()) {
                bjPerse.incrementAndGet();
                bjCents.addAndGet(-BET_INT * 100L);
                continue;
            }

            // turno giocatore: stai su 17+
            while (g.valoreMano() < 17 && !g.haSballato()) {
                g.aggiungiCarta(mazzo.pescaCarta());
            }

            if (g.haSballato()) {
                bjPerse.incrementAndGet();
                bjCents.addAndGet(-BET_INT * 100L);
                continue;
            }

            // turno banco
            while (b.devePescare()) {
                b.aggiungiCarta(mazzo.pescaCarta());
            }

            int pg = g.valoreMano();
            int pb = b.valoreMano();

            if (b.haSballato() || pg > pb) {
                bjVinte.incrementAndGet();
                bjCents.addAndGet(BET_INT * 100L);
            } else if (pg < pb) {
                bjPerse.incrementAndGet();
                bjCents.addAndGet(-BET_INT * 100L);
            } else {
                bjPari.incrementAndGet();
            }
        }
    }

    // ── DADI (Pass Line) ─────────────────────────────────────────────────────
    // Vinci con 7 o 11, perdi con 2/3/12, altrimenti si stabilisce un "punto"
    // (per semplicità trattiamo il punto come perdita immediata).
    private static void simulaDadi() {
        for (int i = 0; i < NUM_PARTITE; i++) {
            int sum = random.randomInt(1, 6) + random.randomInt(1, 6);

            if (sum == 7 || sum == 11) {
                dadiVinte.incrementAndGet();
                dadiCents.addAndGet(Math.round(BET * 100));
            } else {
                dadiPerse.incrementAndGet();
                dadiCents.addAndGet(-Math.round(BET * 100));
            }
        }
    }

    // ── ROULETTE (sempre sul rosso, payout 1.9x) ─────────────────────────────
    private static void simulaRoulette() {
        ruotaRoulette wheel = new ruotaRoulette();

        for (int i = 0; i < NUM_PARTITE; i++) {
            String color = wheel.getColor(wheel.spin());

            if (color.equals("red")) {
                rouVinte.incrementAndGet();
                // payout 1.9x → guadagno netto = +0.9 * BET
                rouCents.addAndGet(Math.round(BET * 0.9 * 100));
            } else {
                rouPerse.incrementAndGet();
                rouCents.addAndGet(-Math.round(BET * 100));
            }
        }
    }

    // ── STAMPA RISULTATI ─────────────────────────────────────────────────────
    private static void mostraRisultati() {
        double bjGain    = bjCents.get()   / 100.0;
        double dadiGain  = dadiCents.get() / 100.0;
        double rouGain   = rouCents.get()  / 100.0;
        double totale    = bjGain + dadiGain + rouGain;
        double investito = NUM_PARTITE * BET * 3;
        double roi       = (totale / investito) * 100;

        // Valori pre-formattati
        String sBjGain   = String.format("%s€%.2f", bjGain   >= 0 ? "+" : "", bjGain);
        String sDadiGain = String.format("%s€%.2f", dadiGain >= 0 ? "+" : "", dadiGain);
        String sRouGain  = String.format("%s€%.2f", rouGain  >= 0 ? "+" : "", rouGain);
        String sTotale   = String.format("%s€%.2f", totale   >= 0 ? "+" : "-", Math.abs(totale));
        String sInv      = String.format("€%.2f", investito);
        String sRoi      = String.format("%.2f%%", roi);
        String sPart     = NUM_PARTITE + " partite per gioco  ·  puntata fissa €" + BET_INT;

        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.printf( "║       📊 RISULTATI SIMULAZIONE                   ║%n");
        System.out.printf( "║  %-48s║%n", sPart);
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.println("║                                                  ║");
        System.out.printf( "║  🎰 BLACKJACK  (strategia: stai su 17+)   %-7s║%n", "");
        System.out.printf( "║    Vinte: %-3d  Perse: %-3d  Pari: %-3d             ║%n",
                bjVinte.get(), bjPerse.get(), bjPari.get());
        System.out.printf( "║    Guadagno netto: %-30s║%n", sBjGain);
        System.out.println("║                                                  ║");
        System.out.printf( "║  🎲 DADI  (Pass Line: vinci con 7 o 11)   %-7s║%n", "");
        System.out.printf( "║    Vinte: %-3d  Perse: %-3d                        ║%n",
                dadiVinte.get(), dadiPerse.get());
        System.out.printf( "║    Guadagno netto: %-30s║%n", sDadiGain);
        System.out.println("║                                                  ║");
        System.out.printf( "║  ⭕ ROULETTE  (rosso, payout 1.9x)        %-7s║%n", "");
        System.out.printf( "║    Vinte: %-3d  Perse: %-3d                        ║%n",
                rouVinte.get(), rouPerse.get());
        System.out.printf( "║    Guadagno netto: %-30s║%n", sRouGain);
        System.out.println("║                                                  ║");
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf( "║  💸 Totale investito:   %-25s║%n", sInv);
        System.out.printf( "║  💰 Guadagno/Perdita:   %-25s║%n", sTotale);
        System.out.printf( "║  📉 ROI:                %-25s║%n", sRoi);
        System.out.println("╠══════════════════════════════════════════════════╣");
        System.out.printf( "║  ⚠️  Alla lunga il banco vince sempre.    %-7s║%n", "");
        System.out.printf( "║     Gioca solo per divertimento.          %-7s║%n", "");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
