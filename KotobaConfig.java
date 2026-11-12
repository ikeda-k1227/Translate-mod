package jp.kotoba.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class KotobaConfig {
    private static final Path FILE = Path.of("config", "kotoba-client.properties");

    volatile boolean enabled;
    volatile boolean chatEnabled;
    volatile boolean gameMessagesEnabled;
    volatile boolean scoreboardEnabled;
    volatile boolean hologramsEnabled;
    volatile boolean showTranslations;
    final String endpoint;
    final String apiKey;
    final int maxCharacters;

    private KotobaConfig(Properties p) {
        enabled = Boolean.parseBoolean(p.getProperty("enabled", "false"));
        chatEnabled = Boolean.parseBoolean(p.getProperty("chatEnabled", "true"));
        gameMessagesEnabled = Boolean.parseBoolean(p.getProperty("gameMessagesEnabled", "true"));
        scoreboardEnabled = Boolean.parseBoolean(p.getProperty("scoreboardEnabled", "true"));
        hologramsEnabled = Boolean.parseBoolean(p.getProperty("hologramsEnabled", "true"));
        showTranslations = Boolean.parseBoolean(p.getProperty("showTranslations", "true"));
        endpoint = safeEndpoint(p.getProperty("endpoint", "https://libretranslate.com/translate").trim());
        apiKey = p.getProperty("apiKey", "").trim();
        int max;
        try { max = Math.max(1, Math.min(2000, Integer.parseInt(p.getProperty("maxCharacters", "500")))); }
        catch (NumberFormatException e) { max = 500; }
        maxCharacters = max;
    }

    static KotobaConfig load() {
        Properties p = new Properties();
        try {
            Files.createDirectories(FILE.getParent());
            if (Files.exists(FILE)) {
                try (var in = Files.newInputStream(FILE)) { p.load(in); }
            } else {
                p.setProperty("enabled", "false");
                p.setProperty("chatEnabled", "true");
                p.setProperty("gameMessagesEnabled", "true");
                p.setProperty("scoreboardEnabled", "true");
                p.setProperty("hologramsEnabled", "true");
                p.setProperty("showTranslations", "true");
                p.setProperty("endpoint", "https://libretranslate.com/translate");
                p.setProperty("apiKey", "");
                p.setProperty("maxCharacters", "500");
                store(p);
            }
        } catch (IOException e) {
            System.err.println("[Kotoba Client] Could not load settings: " + e.getMessage());
        }
        return new KotobaConfig(p);
    }

    synchronized void toggleDisplayMode() {
        showTranslations = !showTranslations;
        Properties p = new Properties();
        try (var in = Files.newInputStream(FILE)) { p.load(in); }
        catch (IOException ignored) { }
        p.setProperty("showTranslations", Boolean.toString(showTranslations));
        store(p);
    }

    private static void store(Properties p) {
        try (var out = Files.newOutputStream(FILE)) {
            p.store(out, "Kotoba Client settings");
        } catch (IOException e) {
            System.err.println("[Kotoba Client] Could not save settings: " + e.getMessage());
        }
    }

    private static String safeEndpoint(String endpoint) {
        if (endpoint.startsWith("https://") || endpoint.startsWith("http://localhost") || endpoint.startsWith("http://127.0.0.1")) return endpoint;
        return "https://libretranslate.com/translate";
    }
}
