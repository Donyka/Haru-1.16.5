package kz.haru.implement.screen.alts.auth;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

public class SimpleHttpServer {
    private HttpServer server;
    private int port;
    private final Function<Map<String, String>, String> callback;
    
    public SimpleHttpServer(Function<Map<String, String>, String> callback) {
        this.callback = callback;
        this.port = new Random().nextInt(10000) + 50000;
    }
    
    public int start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        
        server.createContext("/callback", (HttpExchange exchange) -> {
            try {
                URI requestURI = exchange.getRequestURI();
                String query = requestURI.getQuery();
                
                Map<String, String> params = parseQueryParams(query);
                String responseText = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>Minecraft Auth</title>" +
                                     "<style>body {font-family: Arial, sans-serif; margin: 0; padding: 20px; background-color: #f0f0f0; color: #333;}" +
                                     "div {background-color: white; max-width: 600px; margin: 0 auto; padding: 20px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.1);}" +
                                     "h1 {color: #2d6b2d;} p {line-height: 1.6;}</style></head><body><div>" +
                                     "<h1>Microsoft Authentication</h1>" +
                                     "<p>" + callback.apply(params) + "</p>" +
                                     "</div></body></html>";
                
                exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, responseBytes.length);
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(responseBytes);
                }
            } catch (Exception e) {
                String response = "Ошибка: " + e.getMessage();
                exchange.sendResponseHeaders(500, response.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes(StandardCharsets.UTF_8));
                }
            } finally {
                exchange.close();
            }
        });
        
        server.setExecutor(null);
        server.start();
        
        return port;
    }
    
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> result = new HashMap<>();
        
        if (query != null && !query.isEmpty()) {
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(
                        URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(entry[1], StandardCharsets.UTF_8)
                    );
                } else {
                    result.put(
                        URLDecoder.decode(entry[0], StandardCharsets.UTF_8),
                        ""
                    );
                }
            }
        }
        
        return result;
    }
    
    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }
    
    public int getPort() {
        return port;
    }
} 