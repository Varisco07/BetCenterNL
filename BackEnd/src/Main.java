import java.util.Scanner;
import java.util.List;
import core.*;
import games.baccarat.BaccaratMain;
import games.poker.PokerMain;
import stats.*;

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
            Database.saveUsers();
        }
    }

    private static void menuPrincipale(User user) {
        Scanner scanner = new Scanner(System.in);
        boolean continua = true;

        while (continua) {
            // Sincronizza il saldo prima di mostrare il menu
            user.setSaldo(State.getBalance());
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
                    giocaBaccarat();
                    break;
                case "6":
                    giocaPoker();
                    break;
                case "7":
                    giocaChicken();
                    break;
                case "8":
                    giocaVirtualSports();
                    break;
                case "9":
                    mostraPortafoglioStats(user);
                    break;
                case "10":
                    mostraStorico(user);
                    break;
                case "11":
                    mostraBonusGiornaliero(user);
                    break;
                case "12":
                    Leaderboard.displayLeaderboard();
                    break;
                case "13":
                    Achievements.checkAndUnlock(user);
                    Achievements.displayAchievements(user);
                    break;
                case "14":
                    mostraGiocoResponsabile();
                    break;
                case "15":
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
            Database.saveUsers();
        }

        scanner.close();
    }

    private static void mostraMenu(User user) {
        System.out.println("\n┌────────────────────────────────────────┐");
        System.out.printf("│ Giocatore: %s %s%n", user.getNome(), user.getCognome());
        System.out.printf("│ SALDO: €%.2f%n", user.getSaldo());
        System.out.printf("│ LIVELLO: %d - %s%n", user.getCurrentLevel(), user.getLevelName());
        System.out.printf("│ XP: %d%n", user.getXp());
        System.out.println("├────────────────────────────────────────┤");
        System.out.println("│ CASINO                                 │");
        System.out.println("│ 1. 🎰 BLACKJACK                        │");
        System.out.println("│ 2. 🎲 DADI                             │");
        System.out.println("│ 3. ⭕ ROULETTE                         │");
        System.out.println("│ 4. 🎯 SLOT MACHINE                     │");
        System.out.println("│ 5. 💎 BACCARAT                         │");
        System.out.println("│ 6. ♠ VIDEO POKER                       │");
        System.out.println("│ 7. 🐔 CHICKEN ROAD                     │");
        System.out.println("│ 8. ⚽ VIRTUAL SPORTS                   │");
        System.out.println("├────────────────────────────────────────┤");
        System.out.println("│ ACCOUNT                                │");
        System.out.println("│ 9. 💳 PORTAFOGLIO & STATS              │");
        System.out.println("│ 10. 📋 STORICO                         │");
        System.out.println("│ 11. 🎁 BONUS GIORNALIERO               │");
        System.out.println("│ 12. 🏆 CLASSIFICA                      │");
        System.out.println("│ 13. 🏅 TRAGUARDI                       │");
        System.out.println("│ 14. ⚠️  GIOCO RESPONSABILE             │");
        System.out.println("│ 15. � ESCI                            │");
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
            games.dadi.MainDadi.main(new String[]{});
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

    private static void giocaBaccarat() {
        System.out.println("\n💎 BACCARAT - Avvio del gioco...\n");
        try {
            BaccaratMain.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Baccarat");
        }
    }

    private static void giocaPoker() {
        System.out.println("\n♠ VIDEO POKER - Avvio del gioco...\n");
        try {
            PokerMain.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Video Poker");
        }
    }

    private static void giocaChicken() {
        System.out.println("\n🐔 CHICKEN ROAD - Avvio del gioco...\n");
        try {
            games.chicken.ChickenMain.main(new String[]{});
        } catch (Exception e) {
            System.out.println("Errore nel caricamento di Chicken Road");
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
        System.out.printf("║ Giocatore: %s %s%n", user.getNome(), user.getCognome());
        System.out.printf("║ Email: %s%n", user.getEmail());
        System.out.printf("║ Username: %s%n", user.getUsername());
        System.out.println("╠════════════════════════════════════════╣");
        System.out.printf("║ 💰 SALDO ATTUALE: €%.2f%n", user.getSaldo());
        System.out.println("╠════════════════════════════════════════╣");
        System.out.println("║ 📊 STATISTICHE                         ║");
        System.out.printf("║ Giochi giocati: %d%n", user.getGiociGiocati());
        System.out.printf("║ Giochi vinti: %d%n", user.getGiociVinti());
        System.out.printf("║ Giochi persi: %d%n", user.getGiociPersi());
        System.out.printf("║ Tasso di vittoria: %.1f%%%n", user.getWinRate());
        System.out.printf("║ Guadagno totale: €%.2f%n", user.getGuadagnoTotale());
        System.out.printf("║ Livello: %d - %s%n", user.getCurrentLevel(), user.getLevelName());
        System.out.printf("║ XP: %d%n", user.getXp());
        System.out.printf("║ Traguardi: %d%n", user.getAchievements().size());
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void mostraStorico(User user) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║         📋 STORICO SCOMMESSE           ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        List<GameRecord> history = Database.getGameHistory(user.getId());
        if (history.isEmpty()) {
            System.out.println("║ Nessuna scommessa ancora               ║");
        } else {
            int count = 0;
            for (GameRecord record : history) {
                if (count >= 10) break;
                System.out.println("║ " + record.toString());
                count++;
            }
        }
        
        System.out.println("╚════════════════════════════════════════╝\n");
    }

    private static void mostraBonusGiornaliero(User user) {
        System.out.println("\n╔════════════════════════════════════════╗");
        System.out.println("║       🎁 BONUS GIORNALIERO             ║");
        System.out.println("╠════════════════════════════════════════╣");
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate lastBonus = user.getLastBonusDate() != null ? 
            user.getLastBonusDate().toLocalDate() : null;
        
        if (lastBonus != null && lastBonus.equals(today)) {
            System.out.println("║ Hai già ritirato il bonus oggi!        ║");
            System.out.println("║ Torna domani per il prossimo bonus.    ║");
        } else {
            int streak = user.getBonusStreak();
            int[] bonuses = {50, 75, 100, 150, 200, 300, 500};
            int bonus = bonuses[Math.min(streak, 6)];
            
            System.out.printf("║ Bonus disponibile: €%d                 ║%n", bonus);
            System.out.printf("║ Serie: %d/7                            ║%n", (streak + 1));
            System.out.println("║                                        ║");
            System.out.println("║ Premi della serie:                     ║");
            for (int i = 0; i < bonuses.length; i++) {
                String check = i < streak ? "✓" : " ";
                System.out.printf("║ Giorno %d: €%-3d %s                     ║%n", (i+1), bonuses[i], check);
            }
        }
        
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
