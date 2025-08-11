package kz.haru.implement.screen.alts.auth;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kz.haru.implement.screen.alts.Alt;
import kz.haru.implement.screen.alts.AltManager;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MicrosoftAuth {
    private static final String CLIENT_ID = "00000000402b5328";
    private static final String REDIRECT_URI = "https://login.live.com/oauth20_desktop.srf";
    private static final String SCOPE = "service::user.auth.xboxlive.com::MBI_SSL";
    private static final Pattern CODE_PATTERN = Pattern.compile("code=([\\w.-]+)");
    private static SimpleHttpServer server;
    
    public static CompletableFuture<Alt> loginWithBrowser(Consumer<String> statusConsumer) {
        CompletableFuture<Alt> future = new CompletableFuture<>();
        
        try {
            if (server != null) {
                server.stop();
                server = null;
            }
            
            String authUrl = "https://login.live.com/oauth20_authorize.srf" +
                    "?client_id=" + CLIENT_ID +
                    "&response_type=code" +
                    "&scope=" + URLEncoder.encode(SCOPE, StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8);
            
            statusConsumer.accept("Открывается окно браузера для авторизации...");
            
            openBrowser(authUrl);
            
            statusConsumer.accept("Пожалуйста, введите код из адресной строки...");
            
            server = new SimpleHttpServer(params -> {
                future.complete(null);
                return "Пожалуйста, вернитесь в игру и введите код из адресной строки.";
            });
            server.start();
            
            statusConsumer.accept("Войдите в аккаунт Microsoft в браузере и скопируйте полный URL из адресной строки после завершения авторизации.");
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
        
        return future;
    }
    
    public static Alt processLoginUrl(String url, Consumer<String> statusConsumer) {
        try {
            statusConsumer.accept("Обработка URL авторизации...");
            
            Matcher matcher = CODE_PATTERN.matcher(url);
            if (matcher.find()) {
                String code = matcher.group(1);
                statusConsumer.accept("Код найден, получение токена...");
                AuthData authData = login(code);
                Alt alt = new Alt(authData.username, authData.accessToken, authData.uuid, true);
                
                AltManager.getInstance().addAlt(alt);
                AltManager.getInstance().loginToAlt(alt);
                
                statusConsumer.accept("Авторизация выполнена успешно!");
                return alt;
            } else {
                statusConsumer.accept("Не удалось найти код авторизации в URL");
                return null;
            }
        } catch (Exception e) {
            statusConsumer.accept("Ошибка: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && !GraphicsEnvironment.isHeadless()) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime runtime = Runtime.getRuntime();
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    runtime.exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    runtime.exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось открыть браузер: " + e.getMessage(), e);
        }
    }
    
    public static AuthData login(String code) throws Exception {
        String msToken = getAccessToken(code);

        String xblToken = getXBLToken(msToken);

        XstsResponse xstsResponse = getXSTSToken(xblToken);

        JsonObject mcResponse = getMinecraftToken(xstsResponse.token, xstsResponse.userHash);
        
        String mcToken = mcResponse.get("access_token").getAsString();
        String mcId = UUID.randomUUID().toString();

        JsonObject profile = getMinecraftProfile(mcToken);
        String username = profile.get("name").getAsString();
        String uuid = profile.get("id").getAsString();
        
        return new AuthData(username, uuid, mcToken);
    }
    
    private static String getAccessToken(String code) throws Exception {
        URL url = new URL("https://login.live.com/oauth20_token.srf");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setDoOutput(true);
        
        Map<String, String> params = new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("code", code);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", REDIRECT_URI);
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = buildUrlParams(params).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            StringBuilder errorResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    errorResponse.append(line);
                }
            }
            throw new Exception("Failed to get Microsoft token: " + responseCode + " " + errorResponse);
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            JsonObject jsonResponse = new JsonParser().parse(response.toString()).getAsJsonObject();
            return jsonResponse.get("access_token").getAsString();
        }
    }
    
    private static String getXBLToken(String msToken) throws Exception {
        URL url = new URL("https://user.auth.xboxlive.com/user/authenticate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        
        String body = "{"
                + "\"Properties\": {"
                + "\"AuthMethod\": \"RPS\","
                + "\"SiteName\": \"user.auth.xboxlive.com\","
                + "\"RpsTicket\": \"d=" + msToken + "\""
                + "},"
                + "\"RelyingParty\": \"http://auth.xboxlive.com\","
                + "\"TokenType\": \"JWT\""
                + "}";
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to get XBL token: " + responseCode);
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            JsonObject jsonResponse = new JsonParser().parse(response.toString()).getAsJsonObject();
            return jsonResponse.get("Token").getAsString();
        }
    }
    
    private static XstsResponse getXSTSToken(String xblToken) throws Exception {
        URL url = new URL("https://xsts.auth.xboxlive.com/xsts/authorize");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        
        String body = "{"
                + "\"Properties\": {"
                + "\"SandboxId\": \"RETAIL\","
                + "\"UserTokens\": [\"" + xblToken + "\"]"
                + "},"
                + "\"RelyingParty\": \"rp://api.minecraftservices.com/\","
                + "\"TokenType\": \"JWT\""
                + "}";
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to get XSTS token: " + responseCode);
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            JsonObject jsonResponse = new JsonParser().parse(response.toString()).getAsJsonObject();
            String token = jsonResponse.get("Token").getAsString();
            String userHash = jsonResponse.getAsJsonObject("DisplayClaims")
                    .getAsJsonArray("xui")
                    .get(0)
                    .getAsJsonObject()
                    .get("uhs")
                    .getAsString();
            
            return new XstsResponse(token, userHash);
        }
    }
    
    private static JsonObject getMinecraftToken(String xstsToken, String userHash) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/authentication/login_with_xbox");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");
        conn.setDoOutput(true);
        
        String body = "{"
                + "\"identityToken\": \"XBL3.0 x=" + userHash + ";" + xstsToken + "\","
                + "\"ensureLegacyEnabled\": true"
                + "}";
        
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to get Minecraft token: " + responseCode);
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            return new JsonParser().parse(response.toString()).getAsJsonObject();
        }
    }
    
    private static JsonObject getMinecraftProfile(String mcToken) throws Exception {
        URL url = new URL("https://api.minecraftservices.com/minecraft/profile");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + mcToken);
        
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Failed to get Minecraft profile: " + responseCode);
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            
            return new JsonParser().parse(response.toString()).getAsJsonObject();
        }
    }
    
    private static String buildUrlParams(Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
        }
        
        return result.toString();
    }
    
    public static class AuthData {
        public final String username;
        public final String uuid;
        public final String accessToken;
        
        public AuthData(String username, String uuid, String accessToken) {
            this.username = username;
            this.uuid = uuid;
            this.accessToken = accessToken;
        }
    }
    
    private static class XstsResponse {
        final String token;
        final String userHash;
        
        XstsResponse(String token, String userHash) {
            this.token = token;
            this.userHash = userHash;
        }
    }
} 