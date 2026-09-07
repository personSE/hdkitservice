package com.huaweicloud.hdkitservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "hdkit.telemetry")
@Component
public class HdkitTelemetryConfig {

    private String salt = "changeme";
    private boolean userHashCheckEnabled = true;
    private boolean persistAkUserHash = true;
    private Set<String> userHashWhitelist = new HashSet<>(Set.of("test", "sha256hash1234"));

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public boolean isUserHashCheckEnabled() {
        return userHashCheckEnabled;
    }

    public void setUserHashCheckEnabled(boolean userHashCheckEnabled) {
        this.userHashCheckEnabled = userHashCheckEnabled;
    }

    public boolean isPersistAkUserHash() {
        return persistAkUserHash;
    }

    public void setPersistAkUserHash(boolean persistAkUserHash) {
        this.persistAkUserHash = persistAkUserHash;
    }

    public Set<String> getUserHashWhitelist() {
        return userHashWhitelist;
    }

    public void setUserHashWhitelist(Set<String> userHashWhitelist) {
        this.userHashWhitelist = normalizeWhitelist(userHashWhitelist);
    }

    private static Set<String> normalizeWhitelist(Set<String> raw) {
        if (raw == null) {
            return new HashSet<>();
        }
        Set<String> normalized = new HashSet<>();
        for (String item : raw) {
            if (item != null) {
                normalized.add(item.trim().toLowerCase());
            }
        }
        return normalized;
    }
}