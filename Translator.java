package jp.kotoba.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class Translator {
    private static final Pattern TRANSLATED_TEXT = Pattern.compile("\\\"translatedText\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"");
    private final KotobaConfig config;
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    private final ConcurrentMap<String, CompletableFuture<String>> inFlight = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> cache = new ConcurrentHashMap<>();

    Translator(KotobaConfig config) { this.config = config; }

    String getCached(String text) { return cache.get(text); }

    CompletableFuture<String> translate(String text) {
        String cached = cache.get(text);
        if (cached != null) return CompletableFuture.completedFuture(cached);
        return inFlight.computeIfAbsent(text, key -> request(key).whenComplete((result, error) -> {
            if (result != null && !result.isBlank()) cache.put(key, result);
            inFlight.remove(key);
        }));
    }

    private CompletableFuture<String> request(String text) {
        String body = "{\"q\":\"" + jsonEscape(text) + "\",\"source\":\"en\",\"target\":\"ja\",\"format\":\"text\""
                + (config.apiKey.isBlank() ? "" : ",\"api_key\":\"" + jsonEscape(config.apiKey) + "\"") + "}";
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.endpoint))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8)).build();
        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) return null;
                    Matcher matcher = TRANSLATED_TEXT.matcher(response.body());
                    return matcher.find() ? jsonUnescape(matcher.group(1)) : null;
                }).exceptionally(error -> {
                    System.err.println("[Kotoba Client] Translation request failed: " + error.getMessage());
                    return null;
                });
    }

    private static String jsonEscape(String s) {
        StringBuilder out = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> out.append("\\\""); case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n"); case '\r' -> out.append("\\r"); case '\t' -> out.append("\\t");
                default -> { if (c < 0x20) out.append(String.format("\\u%04x", (int)c)); else out.append(c); }
            }
        }
        return out.toString();
    }

    private static String jsonUnescape(String s) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c != '\\' || i + 1 >= s.length()) { out.append(c); continue; }
            char e = s.charAt(++i);
            switch (e) {
                case '"', '\\', '/' -> out.append(e); case 'n' -> out.append('\n'); case 'r' -> out.append('\r'); case 't' -> out.append('\t');
                case 'b' -> out.append('\b'); case 'f' -> out.append('\f');
                case 'u' -> { if (i + 4 < s.length()) { out.append((char)Integer.parseInt(s.substring(i + 1, i + 5), 16)); i += 4; } }
                default -> out.append(e);
            }
        }
        return out.toString();
    }
}
