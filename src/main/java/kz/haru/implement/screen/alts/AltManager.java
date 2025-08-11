package kz.haru.implement.screen.alts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import kz.haru.implement.screen.alts.auth.MicrosoftAuth;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AltManager {
    private static final AltManager INSTANCE = new AltManager();
    private static final String ALTS_FILE = "alts.json";
    private static final Gson GSON;
    
    static {
        GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    }
    
    private List<Alt> alts = new ArrayList<>();
    
    private AltManager() {
        loadAlts();
    }
    
    public static AltManager getInstance() {
        return INSTANCE;
    }
    
    public List<Alt> getAlts() {
        if (alts == null) {
            alts = new ArrayList<>();
        }
        return alts;
    }
    
    public void addAlt(Alt alt) {
        if (!containsAlt(alt.getUsername())) {
            alts.add(alt);
            saveAlts();
        }
    }
    
    public void removeAlt(int index) {
        if (index >= 0 && index < alts.size()) {
            alts.remove(index);
            saveAlts();
        }
    }
    
    public boolean containsAlt(String username) {
        for (Alt alt : getAlts()) {
            if (alt.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }
        return false;
    }
    
    public void saveAlts() {
        try {
            File configFolder = new File(Minecraft.getInstance().gameDir, "config");
            if (!configFolder.exists()) {
                configFolder.mkdirs();
            }
            
            File file = new File(configFolder, ALTS_FILE);
            try (FileWriter writer = new FileWriter(file)) {
                GSON.toJson(alts, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void loadAlts() {
        try {
            File configFolder = new File(Minecraft.getInstance().gameDir, "config");
            File file = new File(configFolder, ALTS_FILE);
            
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Type listType = new TypeToken<ArrayList<Alt>>(){}.getType();
                    List<Alt> loadedAlts = GSON.fromJson(reader, listType);
                    if (loadedAlts != null) {
                        alts = loadedAlts;
                    } else {
                        alts = new ArrayList<>();
                    }
                }
            } else {
                alts = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            alts = new ArrayList<>();
        }
    }
    
    public boolean loginToAlt(Alt alt) {
        try {
            if (alt.isOffline()) {
                setOfflineSession(alt.getUsername());
                return true;
            } else if (alt.isMicrosoft()) {
                if (alt.isExpired()) {
                    return false;
                }
                
                setMicrosoftSession(alt.getUsername(), alt.getUuid(), alt.getAccessToken());
                return true;
            } else {
                setSessionUsername(alt.getUsername());
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Входит в оффлайн-аккаунт, создает случайный UUID
     * @param username Имя пользователя
     * @return Созданный оффлайн-аккаунт
     */
    public Alt loginOffline(String username) {
        try {
            Alt alt = new Alt(username);
            
            if (!containsAlt(alt.getUsername())) {
                addAlt(alt);
            }
            
            setOfflineSession(alt.getUsername());
            
            return alt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Alt loginWithMicrosoftCode(String authCode) {
        try {
            MicrosoftAuth.AuthData authData = MicrosoftAuth.login(authCode);
            
            Alt alt = new Alt(authData.username, authData.accessToken, authData.uuid, true);
            
            if (!containsAlt(alt.getUsername())) {
                addAlt(alt);
            } else {
                for (Alt existingAlt : alts) {
                    if (existingAlt.getUsername().equalsIgnoreCase(alt.getUsername())) {
                        existingAlt.setAccessToken(alt.getAccessToken());
                        existingAlt.setUuid(alt.getUuid());
                        existingAlt.setExpiresAt(alt.getExpiresAt());
                        existingAlt.setMicrosoft(true);
                        existingAlt.setOffline(false);
                        break;
                    }
                }
                saveAlts();
            }
            
            setMicrosoftSession(authData.username, authData.uuid, authData.accessToken);
            
            return alt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Настраивает сессию для оффлайн-аккаунта
     * @param username Имя пользователя
     */
    private void setOfflineSession(String username) {
        try {
            Object session = Minecraft.getInstance().getSession();
            
            UUID offlineUUID = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
            String uuidString = offlineUUID.toString().replace("-", "");
            
            java.lang.reflect.Field usernameField = session.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(session, username);
            
            java.lang.reflect.Field playerIDField = session.getClass().getDeclaredField("playerID");
            playerIDField.setAccessible(true);
            playerIDField.set(session, uuidString);
            
            java.lang.reflect.Field tokenField = session.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(session, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setSessionUsername(String username) {
        try {
            Object session = Minecraft.getInstance().getSession();
            
            java.lang.reflect.Field field = session.getClass().getDeclaredField("username");
            field.setAccessible(true);
            field.set(session, username);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setMicrosoftSession(String username, String uuid, String accessToken) {
        try {
            Object session = Minecraft.getInstance().getSession();
            
            java.lang.reflect.Field usernameField = session.getClass().getDeclaredField("username");
            usernameField.setAccessible(true);
            usernameField.set(session, username);
            
            java.lang.reflect.Field playerIDField = session.getClass().getDeclaredField("playerID");
            playerIDField.setAccessible(true);
            playerIDField.set(session, uuid);
            
            java.lang.reflect.Field tokenField = session.getClass().getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(session, accessToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 