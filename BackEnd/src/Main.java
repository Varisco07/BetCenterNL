import java.util.Scanner;
import core.Auth;
import core.User;
import core.State;

public class Main {

    public static void main(String[] args) {
        // Mostra il menu di login/registrazione
        Auth.mostraAuthMenu();

        // Dopo il login, mostra il menu principale
        User user = Auth.getCurrentUser();
        if (user != null) {
            // Sincronizza il saldo con State
            State.setBalance(user.getSaldo());
            menuPrincipale(user);
            // Aggiorna il saldo dell'utente dopo il gioco
            user.setSaldo(State.getBalance());
        }
    }

    private static void menuPrincipale(User user) {
        Scanner scanner = new Scanner(System.in);
        boolean continua = true;

        while (continua) {
            mostraMenu(user);
            System.out.print("Scegli un'opzione: ");
            String scelta = scanner.nextLine().trim();

            switch (scelta) {
                case "1":
                    giocaBlackjack(user);
                    break;
                case "2":
                    giocaDadi();
                    break;
                case "3":
                    giocaRoulette();
                    break;
                case "4":
                    giocaSlot();
                    break;
                case "5":
                    giocaVirtualSports();
                    break;
                case "6":
                    mostraPortafoglioStats(user);
                    break;
                case "7":
                    mostraStorico(user);
                    break;
                case "8":
                    mostraBonusGiornaliero(user);
                    break;
                case "9":
                    mostraClassifica();
                    break;
                case "10":
                    mostraGiocoResponsabile();
                    break;
                case "11":
                    continua = false;
                    user.setSaldo(State.getBalance());
                    System.out.println("\n╔════════════════════════════════════════╗");
                    System.out.println("║     Grazie per aver giocato!           ║");
                    System.out.println("║     Saldo: €" + String.format("%.2f", user.getSaldo()) + "                          ║");
                    System.out.println("╚════════════════════════════════════════╝");
                    break;
                default:
                    System.out.println("❌ Opzione non valida!\n");
            }
            
            // Aggiorna il saldo dopo ogni gioco
            user.setSaldo(State.getBalance());
        }

        scanner.close();
    }

    private static void mostraMenu(User user) {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.println("│ Giocatore: " + user.getNome() + " " + user.getCognome());
        System.out.println("│ SALDO: €" + String.format("%.2f", user.getSaldo()) + "                          │");
        System.out.println("├────────────────────────────────────────┤");
        System.out.println("│ CASINO                                 │");
        System.out.println("│ 1. 🎰 BLACKJACK                        │");
        System.out.println("│ 2. 🎲 DADI                             │");
        System.out.println("│ 3. ⭕ ROULETTE                         │");
        System.out.println("│ 4. 🎯 SLOT MACHINE                     │");
        System.out.println("│ 5. ⚽ VIRTUAL SPORTS                   │");
        System.out.println("├────────────────────────────────────────┤");
        System.out.println("│ ACCOUNT                                │");
        System.out.println("│ 6. � PORTAFOGLIO & STATS              │");
        System.out.println("│ 7. 📋 STORICO                          │");
        System.out.println("│ 8. 🎁 BONUS GIORNALIERO                │");
        System.out.println("│ 9. 🏆 CLASSIFICA                       │");
        System.out.println("│ 10. ⚠️  GIOCO RESPONSABILE             │");
        System.out.println("│ 11. 🚪 ESCI                            │");
        System.out.println("└────────────────────────────────────────┘");
    }

    private static void giocaBlackjack(User user) {
        System.out.println("\n🎰 BLACKJACK - Avvio del gioco...\n");
        
        try {
            games.BlackJack.Main.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Blackjack");
        }
    }

    private static void giocaDadi() {
        System.out.println("\n🎲 DADI - Avvio del gioco...\n");
        
        try {
            games.dice.MainDadi.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Dadi");
        }
    }

    private static void giocaRoulette() {
        System.out.println("\n⭕ ROULETTE - Avvio del gioco...\n");
        
        try {
            games.roulette.RouletteMain.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Roulette");
        }
    }

    private static void giocaSlot() {
        System.out.println("\n🎯 SLOT MACHINE - Avvio del gioco...\n");
        
        try {
            games.slot.MainSlot.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Slot Machine");
        }
    }

    private static void giocaVirtualSports() {
        System.out.println("\n⚽ VIRTUAL SPORTS - Avvio del gioco...\n");
        
        try {
            games.virtual.VirtualMain.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Virtual Sports");
        }
    }

    private static void mostraPortafoglioStats(User user) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      💳 PORTAFOGLIO & STATS            ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Giocatore: " + user.getNome() + " " + user.getCognome());
        System.out.println("║ Email: " + user.getEmail());
        System.out.println("║ Username: " + user.getUsername());
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 💰 SALDO ATTUALE: €" + String.format("%.2f", user.getSaldo()));
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 📊 STATISTICHE                         ║");
        System.out.println("║ Giochi giocati: " + user.getGiociGiocati());
        System.out.println("║ Giochi vinti: " + user.getGiociVinti());
        System.out.println("║ Giochi persi: " + user.getGiociPersi());
        System.out.println("║ Tasso di vittoria: " + String.format("%.1f%%", user.getWinRate()));
        System.out.println("║ Guadagno totale: €" + String.format("%.2f", user.getGuadagnoTotale()));
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void mostraStorico(User user) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         📋 STORICO SCOMMESSE           ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Funzionalità in sviluppo               ║");
        System.out.println("║ Qui vedrai la cronologia di tutte      ║");
        System.out.println("║ le tue scommesse e i risultati.        ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void mostraBonusGiornaliero(User user) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       🎁 BONUS GIORNALIERO             ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Bonus disponibile: €50.00              ║");
        System.out.println("║ Prossimo bonus tra: 23h 45m             ║");
        System.out.println("║                                        ║");
        System.out.println("║ Premi accumulati questa settimana:     ║");
        System.out.println("║ Lunedì: €50 ✓                          ║");
        System.out.println("║ Martedì: €50 ✓                         ║");
        System.out.println("║ Mercoledì: €50 ✓                       ║");
        System.out.println("║ Giovedì: €50 ✓                         ║");
        System.out.println("║ Venerdì: €50 ✓                         ║");
        System.out.println("║ Sabato: €50 ✓                          ║");
        System.out.println("║ Domenica: €50 (domani)                 ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void mostraClassifica() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║          🏆 CLASSIFICA TOP 10          ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 1. 🥇 LuckyPlayer - €5,250.00          ║");
        System.out.println("║ 2. 🥈 HighRoller - €4,890.00           ║");
        System.out.println("║ 3. 🥉 WinnerKing - €4,120.00           ║");
        System.out.println("║ 4. ⭐ CasinoMaster - €3,890.00         ║");
        System.out.println("║ 5. ⭐ BetGod - €3,450.00               ║");
        System.out.println("║ 6. ⭐ LuckyDuck - €3,120.00            ║");
        System.out.println("║ 7. ⭐ ProPlayer - €2,890.00            ║");
        System.out.println("║ 8. ⭐ VirtualKing - €2,650.00          ║");
        System.out.println("║ 9. ⭐ SlotMaster - €2,340.00           ║");
        System.out.println("║ 10. ⭐ GoldenHand - €2,100.00          ║");
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void mostraGiocoResponsabile() {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║      ⚠️  GIOCO RESPONSABILE            ║");
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ Il gioco d'azzardo può creare          ║");
        System.out.println("║ dipendenza. Gioca responsabilmente.    ║");
        System.out.println("║                                        ║");
        System.out.println("║ 📞 Numeri di aiuto:                    ║");
        System.out.println("║ • Telefono Azzardo: 1-800-GAMBLER     ║");
        System.out.println("║ • Gamblers Anonymous: 1-800-GA-HELP   ║");
        System.out.println("║                                        ║");
        System.out.println("║ 💡 Consigli:                           ║");
        System.out.println("║ • Gioca solo per divertimento          ║");
        System.out.println("║ • Imposta limiti di spesa               ║");
        System.out.println("║ • Non inseguire le perdite              ║");
        System.out.println("║ • Prendi pause regolari                ║");
        System.out.println("║                                        ║");
        System.out.println("║ Saldo attuale: €" + String.format("%.2f", State.getBalance()));
        System.out.println("╚════════════════════════════════════════╝\n");
    }
}
