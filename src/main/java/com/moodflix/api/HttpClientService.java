package com.moodflix.api;

import com.moodflix.config.AppConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * HTTP client with timeouts, retries, and graceful offline handling.
 */
public final class HttpClientService {

    private HttpClientService() {
    }

    public static Optional<String> get(String urlString) {
        return get(urlString, AppConfig.getMaxRetries());
    }

    public static Optional<String> get(String urlString, int maxRetries) {
        int attempts = 0;
        Exception lastError = null;

        while (attempts <= maxRetries) {
            attempts++;
            try {
                return Optional.of(executeGet(urlString));
            } catch (IOException e) {
                lastError = e;
                if (attempts <= maxRetries) {
                    try {
                        Thread.sleep(250L * attempts);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        System.err.println("HTTP GET failed after " + attempts + " attempt(s): " + urlString
                + (lastError != null ? " — " + lastError.getMessage() : ""));
        return Optional.empty();
    }

    private static String executeGet(String urlString) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(AppConfig.getConnectTimeoutMs());
        conn.setReadTimeout(AppConfig.getReadTimeoutMs());
        conn.setRequestProperty("User-Agent", "MoodFlix/1.0");
        conn.setRequestProperty("Accept", "application/json");

        int code = conn.getResponseCode();
        InputStream stream = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        if (stream == null) {
            throw new IOException("HTTP " + code + " with empty body");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            StringBuilder body = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
            if (code >= 400) {
                throw new IOException("HTTP " + code + ": " + body);
            }
            return body.toString();
        } finally {
            conn.disconnect();
        }
    }

    public static boolean isApiKeyConfigured(String key) {
        return key != null && !key.isBlank();
    }
}
