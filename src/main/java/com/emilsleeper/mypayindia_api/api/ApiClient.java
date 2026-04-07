package com.emilsleeper.mypayindia_api.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ApiClient {
    private static final String BASE_URL = "https://mypayindia.com/api/v1";
    private static final Gson gson = new Gson();
    private static String sessionCookie = null;
    private static final Path SESSION_FILE;

    static {
        // Initialize session file path
        Path tempPath;
        try {
            // Use FabricLoader to get config directory
            net.fabricmc.loader.api.FabricLoader loader = net.fabricmc.loader.api.FabricLoader.getInstance();
            Path configDir = loader.getConfigDir();
            tempPath = configDir.resolve("mypayindia_session.txt");
        } catch (Exception e) {
            // Fallback if FabricLoader not available (shouldn't happen in mod environment)
            tempPath = Paths.get("config", "mypayindia_session.txt");
        }
        SESSION_FILE = tempPath;
        loadSessionCookie();
    }

    /**
     * Performs a GET request to the API
     */
    public static JsonObject get(String endpoint) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-Type", "application/json");

        if (sessionCookie != null) {
            conn.setRequestProperty("Cookie", "PHPSESSID=" + sessionCookie);
        }

        return handleResponse(conn);
    }

    /**
     * Performs a POST request to the API
     */
    public static JsonObject post(String endpoint, JsonObject body) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        if (sessionCookie != null) {
            conn.setRequestProperty("Cookie", "PHPSESSID=" + sessionCookie);
        }

        // Write request body
        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return handleResponse(conn);
    }

    /**
     * Handles the HTTP response and extracts session cookie if present
     */
    private static JsonObject handleResponse(HttpURLConnection conn) throws IOException {
        // Extract and save PHPSESSID cookie
        String cookieHeader = conn.getHeaderField("Set-Cookie");
        if (cookieHeader != null && cookieHeader.contains("PHPSESSID")) {
            String[] parts = cookieHeader.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("PHPSESSID=")) {
                    sessionCookie = part.trim().substring(10);
                    saveSessionCookie(); // Save cookie to file
                    break;
                }
            }
        }

        int responseCode = conn.getResponseCode();
        String response = readInputStream(responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream());

        // Debug: Print the raw response
        System.out.println("DEBUG: API Response: " + response);

        // Try to parse as JsonElement first to handle different types
        JsonElement element = gson.fromJson(response, JsonElement.class);
        if (element == null) {
            return new JsonObject();
        }

        if (element.isJsonObject()) {
            return element.getAsJsonObject();
        } else if (element.isJsonPrimitive()) {
            // Handle primitive responses (like error messages)
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("success", false);
            wrapper.addProperty("message", element.getAsString());
            return wrapper;
        } else if (element.isJsonArray()) {
            // Handle array responses
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("success", true);
            wrapper.add("data", element.getAsJsonArray());
            return wrapper;
        } else {
            // Fallback
            JsonObject wrapper = new JsonObject();
            wrapper.addProperty("success", false);
            wrapper.addProperty("message", "Unexpected response format");
            return wrapper;
        }
    }

    /**
     * Reads the response from an input stream
     */
    private static String readInputStream(InputStream is) throws IOException {
        if (is == null) {
            return "{}";
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * Returns the current session cookie
     */
    public static String getSessionCookie() {
        return sessionCookie;
    }

    /**
     * Sets the session cookie manually
     */
    public static void setSessionCookie(String cookie) {
        sessionCookie = cookie;
        saveSessionCookie(); // Save cookie to file
    }

    /**
     * Clears the session cookie (logout)
     */
    public static void clearSessionCookie() {
        sessionCookie = null;
        saveSessionCookie(); // Remove cookie from file
    }

    /**
     * Loads the session cookie from the file
     */
    private static void loadSessionCookie() {
        try {
            if (Files.exists(SESSION_FILE)) {
                String cookie = new String(Files.readAllBytes(SESSION_FILE), StandardCharsets.UTF_8);
                sessionCookie = cookie.trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the session cookie to the file
     */
    private static void saveSessionCookie() {
        try {
            if (sessionCookie != null) {
                Files.write(SESSION_FILE, sessionCookie.getBytes(StandardCharsets.UTF_8));
            } else {
                Files.deleteIfExists(SESSION_FILE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
