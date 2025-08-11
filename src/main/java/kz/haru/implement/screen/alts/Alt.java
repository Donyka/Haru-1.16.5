package kz.haru.implement.screen.alts;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Alt {
    @Expose
    private String username;
    @Expose
    private String password;
    @Expose
    private boolean isMicrosoft;
    @Expose
    private boolean isOffline;
    @Expose
    private String accessToken;
    @Expose
    private String uuid;
    @Expose
    private long expiresAt;

    public Alt(String username) {
        this.username = username;
        this.password = "";
        this.isMicrosoft = false;
        this.isOffline = true;
    }

    public Alt(String username, String password) {
        this.username = username;
        this.password = password;
        this.isMicrosoft = false;
        this.isOffline = password == null || password.isEmpty();
    }

    public Alt(String username, String password, boolean isMicrosoft) {
        this.username = username;
        this.password = password;
        this.isMicrosoft = isMicrosoft;
        this.isOffline = !isMicrosoft && (password == null || password.isEmpty());
    }
    
    public Alt(String username, String accessToken, String uuid, boolean isMicrosoft) {
        this.username = username;
        this.accessToken = accessToken;
        this.uuid = uuid;
        this.isMicrosoft = isMicrosoft;
        this.isOffline = false;
        this.expiresAt = System.currentTimeMillis() + 86400000;
    }

    public boolean isMicrosoft() {
        return isMicrosoft;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setMicrosoft(boolean microsoft) {
        isMicrosoft = microsoft;
    }

    public void setOffline(boolean offline) {
        isOffline = offline;
    }

    public boolean isExpired() {
        return isMicrosoft && System.currentTimeMillis() > expiresAt;
    }
    
    @Override
    public String toString() {
        if (isOffline) {
            return username + " (Оффлайн)";
        } else if (isMicrosoft) {
            return username + " (Microsoft)";
        } else {
            return username;
        }
    }
}