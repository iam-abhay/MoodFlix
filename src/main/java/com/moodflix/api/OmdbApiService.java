package com.moodflix.api;

import com.moodflix.config.AppConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * OMDb API integration with timeout, retry, and invalid-key handling.
 */
public class OmdbApiService {

    private static final String BASE_URL = "https://www.omdbapi.com/";

    private volatile String lastWarning;

    public Optional<String> getLastWarning() {
        return lastWarning == null ? Optional.empty() : Optional.of(lastWarning);
    }

    public Optional<JSONObject> search(String query) {
        String apiKey = AppConfig.getOmdbApiKey();
        if (!HttpClientService.isApiKeyConfigured(apiKey)) {
            lastWarning = "OMDb API key is not configured. Movie search is unavailable.";
            return Optional.empty();
        }

        String url = BASE_URL + "?apikey=" + apiKey + "&s="
                + URLEncoder.encode(query, StandardCharsets.UTF_8);
        return HttpClientService.get(url).flatMap(this::parseJson);
    }

    public Optional<JSONObject> fetchByImdbId(String imdbId) {
        String apiKey = AppConfig.getOmdbApiKey();
        if (!HttpClientService.isApiKeyConfigured(apiKey)) {
            lastWarning = "OMDb API key is not configured.";
            return Optional.empty();
        }

        String url = BASE_URL + "?apikey=" + apiKey + "&i=" + imdbId;
        return HttpClientService.get(url).flatMap(this::parseJson);
    }

    public Optional<JSONObject> fetchByTitle(String title, String typeParam, String seasonParam, String episodeParam) {
        String apiKey = AppConfig.getOmdbApiKey();
        if (!HttpClientService.isApiKeyConfigured(apiKey)) {
            lastWarning = "OMDb API key is not configured.";
            return Optional.empty();
        }

        String url = BASE_URL + "?apikey=" + apiKey + "&t="
                + URLEncoder.encode(title, StandardCharsets.UTF_8)
                + typeParam + seasonParam + episodeParam;
        return HttpClientService.get(url).flatMap(this::parseJson);
    }

    public Optional<JSONArray> searchResults(String query) {
        return search(query).flatMap(obj -> {
            if ("True".equals(obj.optString("Response")) && obj.has("Search")) {
                return Optional.of(obj.getJSONArray("Search"));
            }
            if (obj.has("Error")) {
                lastWarning = obj.getString("Error");
            }
            return Optional.empty();
        });
    }

    private Optional<JSONObject> parseJson(String body) {
        try {
            JSONObject obj = new JSONObject(body);
            if (obj.has("Error") && !"True".equals(obj.optString("Response"))) {
                lastWarning = obj.getString("Error");
            }
            return Optional.of(obj);
        } catch (Exception e) {
            lastWarning = "Invalid OMDb response.";
            return Optional.empty();
        }
    }
}
