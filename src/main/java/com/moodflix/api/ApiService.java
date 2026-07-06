package com.moodflix.api;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiService {

    private static final String ACCESS_KEY = "aBP_F7Q5wCSVk4PeWZHG6ajx8xHSQUZQTvF4038Os5E"; // Replace with actual key

    public String getImageUrl(String keyword) {
        String link = "https://api.unsplash.com/photos/random?client_id=" + ACCESS_KEY + "&query=" + keyword;
        String imageUrl = "";

        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder json = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
                json.append(line);

            JSONObject obj = new JSONObject(json.toString());
            imageUrl = obj.getJSONObject("urls").getString("regular");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return imageUrl;
    }
}
