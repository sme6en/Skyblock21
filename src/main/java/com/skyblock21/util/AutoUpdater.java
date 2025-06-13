package com.skyblock21.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.*;
import com.skyblock21.Skyblock21;
import net.fabricmc.loader.api.FabricLoader;
import java.net.URL;

public class AutoUpdater {

    private static final String REPO = "sme6en/Skyblock21";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";

    private static final Path MODS_FOLDER = FabricLoader.getInstance().getGameDir().resolve("mods");
    private static final Path TEMP_UPDATE_JAR = MODS_FOLDER.resolve("__mod_update__.jar");
    public static String latestUpdatedVersion = "";

    public static void onStartup() {
        // Register shutdown hook once at game startup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (Files.exists(TEMP_UPDATE_JAR)) {
                    Files.list(MODS_FOLDER)
                            .filter(p -> p.toString().endsWith(".jar"))
                            .filter(p -> p.getFileName().toString().contains(Skyblock21.MOD_ID))
                            .forEach(p -> {
                                try {
                                    Files.delete(p);
                                } catch (IOException e) {
                                    System.err.println("[AutoUpdater] Could not delete " + p.getFileName());
                                }
                            });

                    Path newPath = MODS_FOLDER.resolve(Skyblock21.MOD_ID + "-" + latestUpdatedVersion + ".jar");
                    Files.move(TEMP_UPDATE_JAR, newPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("[AutoUpdater] Update applied successfully on shutdown.");
                }
            } catch (IOException e) {
                System.err.println("[AutoUpdater] Shutdown update failed: " + e.getMessage());
            }
        }));
    }

    public static void checkForUpdate() {
        try {
            String response = fetch(API_URL);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            String latestVersion = json.get("tag_name").getAsString().replaceFirst("^v", "");
            if (isNewerVersion(latestVersion, Skyblock21.MOD_VERSION)) {
                System.out.println("[AutoUpdater] New version found: " + latestVersion);
                JsonArray assets = json.getAsJsonArray("assets");

                for (JsonElement assetElem : assets) {
                    JsonObject asset = assetElem.getAsJsonObject();
                    String downloadUrl = asset.get("browser_download_url").getAsString();
                    if (downloadUrl.endsWith(".jar")) {
                        downloadUpdate(downloadUrl);
                        latestUpdatedVersion = latestVersion;
                        return;
                    }
                }
                System.err.println("[AutoUpdater] No .jar asset found in release.");
            } else {
                System.out.println("[AutoUpdater] You're on the latest version.");
            }
        } catch (Exception e) {
            System.err.println("[AutoUpdater] Failed to check for update: " + e.getMessage());
        }
    }

    private static boolean isNewerVersion(String latest, String current) {
        String[] l = latest.split("\\.");
        String[] c = current.split("\\.");
        for (int i = 0; i < Math.max(l.length, c.length); i++) {
            int li = i < l.length ? Integer.parseInt(l[i]) : 0;
            int ci = i < c.length ? Integer.parseInt(c[i]) : 0;
            if (li > ci) return true;
            if (li < ci) return false;
        }
        return false;
    }

    private static String fetch(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestProperty("User-Agent", "AutoUpdater");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) out.append(line);
            return out.toString();
        }
    }

    private static void downloadUpdate(String urlStr) throws IOException {
        System.out.println("[AutoUpdater] Downloading update...");
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, TEMP_UPDATE_JAR, StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("[AutoUpdater] Update downloaded. Will be installed on game shutdown.");
    }

}
