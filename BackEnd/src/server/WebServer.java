package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import core.*;
import games.slot.SlotMachine;
import games.BlackJack.gioco.Blackjack;
import games.roulette.Roulette;

public class WebServer {
    private static final int PORT = 8080;
    private static HttpServer server;
    
    public static void main(String[] args) throws IOException {
        startServer();
    }
    
    public static void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        
        // CORS headers per tutti gli endpoint
        server.createContext("/", new CorsHandler());
        
        // API endpoints
        server.createContext("/api/auth/login", new LoginHandler());
        server.createContext("/api/auth/register", new RegisterHandler());
        server.createContext("/api/games/slots/spin", new SlotsHandler());
        server.createContext("/api/games/blackjack/deal", new BlackjackHandler());
        server.createContext("/api/games/roulette/spin", new RouletteHandler());
        server.createContext("/api/user/profile", new ProfileHandler());
        server.createContext("/api/wallet/balance", new BalanceHandler());
        server.createContext("/api/wallet/deposit", new DepositHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🚀 BetCenterNL Backend Server avviato!");
        System.out.println("📡 Porta: " + PORT);
        System.out.println("🌐 URL: http://localhost:" + PORT);
        System.out.println("⚠️  Premi Ctrl+C per fermare");
    }
    
    // Handler per CORS
    static class CorsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Aggiungi headers CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, 0);
                exchange.close();
                return;
            }
            
            // Default response
            String response = "{\"status\":\"BetCenterNL Backend Running\",\"port\":" + PORT + "}";
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    // Login Handler
    static class LoginHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                System.out.println("Login request: " + requestBody);
                
                // Simulazione login semplice
                String response = "{\"success\":true,\"user\":{\"id\":\"1\",\"username\":\"player1\",\"saldo\":1000},\"token\":\"fake-token-123\"}";
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0); // Method not allowed
                exchange.close();
            }
        }
    }
    
    // Register Handler
    static class RegisterHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                System.out.println("Register request: " + requestBody);
                
                String response = "{\"success\":true,\"user\":{\"id\":\"1\",\"username\":\"newplayer\",\"saldo\":1000},\"token\":\"fake-token-456\"}";
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
    
    // Slots Handler
    static class SlotsHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                System.out.println("Slots spin: " + requestBody);
                
                // Simulazione slot semplice
                String[] symbols = {"🍒", "🍋", "⭐", "💎", "🔔", "🍀", "7️⃣", "💰"};
                String reel1 = symbols[(int)(Math.random() * symbols.length)];
                String reel2 = symbols[(int)(Math.random() * symbols.length)];
                String reel3 = symbols[(int)(Math.random() * symbols.length)];
                
                int bet = 10; // Default bet
                int win = 0;
                
                if (reel1.equals(reel2) && reel2.equals(reel3)) {
                    win = bet * 5; // Tre uguali
                } else if (reel1.equals(reel2) || reel2.equals(reel3) || reel1.equals(reel3)) {
                    win = bet * 2; // Due uguali
                }
                
                int gain = win - bet;
                
                String response = String.format(
                    "{\"reels\":[\"%s\",\"%s\",\"%s\"],\"bet\":%d,\"win\":%d,\"gain\":%d,\"balance\":1000}",
                    reel1, reel2, reel3, bet, win, gain
                );
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
    
    // Blackjack Handler
    static class BlackjackHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                System.out.println("Blackjack deal: " + requestBody);
                
                // Simulazione blackjack semplice
                String[] cards = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
                String card1 = cards[(int)(Math.random() * cards.length)];
                String card2 = cards[(int)(Math.random() * cards.length)];
                String dealerCard = cards[(int)(Math.random() * cards.length)];
                
                String response = String.format(
                    "{\"playerCards\":[\"%s\",\"%s\"],\"dealerCards\":[\"%s\",\"?\"],\"playerValue\":15,\"result\":\"continue\",\"balance\":1000}",
                    card1, card2, dealerCard
                );
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
    
    // Roulette Handler
    static class RouletteHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = getRequestBody(exchange);
                System.out.println("Roulette spin: " + requestBody);
                
                int number = (int)(Math.random() * 37); // 0-36
                String color = number == 0 ? "green" : (number % 2 == 0 ? "black" : "red");
                
                String response = String.format(
                    "{\"number\":%d,\"color\"%s\",\"win\":false,\"winAmount\":0,\"gain\":-10,\"balance\":990}",
                    number, color
                );
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
    
    // Profile Handler
    static class ProfileHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            String response = "{\"id\":\"1\",\"username\":\"player1\",\"saldo\":1000,\"gamesPlayed\":10,\"gamesWon\":5}";
            
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    // Balance Handler
    static class BalanceHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            String response = "{\"balance\":1000}";
            
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    // Deposit Handler
    static class DepositHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String response = "{\"success\":true,\"newBalance\":1100,\"deposited\":100}";
                
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                exchange.sendResponseHeaders(405, 0);
                exchange.close();
            }
        }
    }
    
    // Utility methods
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
    
    private static String getRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}