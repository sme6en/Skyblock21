package com.skyblock21.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyblock21.Skyblock21;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class AutoUpdater {

    private static final String REPO = "sme6en/Skyblock21";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";

    private static final Path MODS_FOLDER = FabricLoader.getInstance().getGameDir().resolve("mods");
    public static String latestUpdatedVersion = "";

    public static void onStartup() {

        if (AutoUpdater.latestUpdatedVersion.isEmpty()) return;

        try {
            File oldModFile = new File(Skyblock21.class.getProtectionDomain()
                                                       .getCodeSource()
                                                       .getLocation()
                                                       .toURI()
                                                       .getPath());
            if (Util.getOperatingSystem() != Util.OperatingSystem.WINDOWS) {
                Skyblock21.LOGGER.info("Deleting old mod file via File.deleteOnExit()");
                oldModFile.deleteOnExit();
            } else { // screw you windows
                // NOTE: This is a workaround for Windows, which doesn't work well with File.deleteOnExit()
                // Instead, we create a batch file that will delete the old mod file on game shutdown.
                // I know it looks very sus, but it's the only way to ensure the file is deleted properly
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    File deleter = new File(FabricLoader.getInstance()
                                                        .getGameDir()
                                                        .toFile(), "deleteOldJar.bat");

                    String comment = """
                            :: Skyblock21 AutoUpdater Deleter Script, Used with Skyblock21's AutoUpdater (https://github.com/sme6en/Skyblock21/blob/main/src/main/java/com/skyblock21/util/AutoUpdater.java)
                            :: NOTE: This is a workaround for Windows, which doesn't work well with File.deleteOnExit()
                            :: Instead, we create a batch file that will delete the old mod file on game shutdown.
                            :: I know it looks very sus, but it's the only way to ensure the file is deleted properly
                            :: or else your game won't start next time you launch it.
                            """;


                    String deleterProgram =
                            "@echo off\n" +
                                    comment +
                                    ":TestFile\n" +
                                    "REN \"" + oldModFile.getAbsolutePath() + "\" \"" + oldModFile.getName() + "\" 2>nul\n" +
                                    "IF not ERRORLEVEL 1 GOTO Continue\n" +
                                    "GOTO TestFile\n" +
                                    ":Continue\n" +
                                    "ECHO Deleting \"" + oldModFile.getAbsolutePath() + "\"\n" +
                                    "DEL /F \"" + oldModFile.getAbsolutePath() + "\"\n" +
                                    "EXIT\n";

                    try {
                        // Create the directory if it doesn't exist, re-write the file if it does
                        deleter.mkdirs();
                        if (deleter.exists()) {
                            deleter.delete();
                        }
                        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(deleter)));
                        writer.println(deleterProgram);
                        writer.close();

                        ProcessBuilder pb = new ProcessBuilder("cmd", "/c", deleter.getAbsolutePath());
                        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                        pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                        pb.start();
                    } catch (IOException e) {
                        Skyblock21.LOGGER.error("Deleting old file with windows method (screw microsoft)", e);
                    }
                }));
            }
        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to delete old mod file: " + e.getMessage());
        }
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
                        latestUpdatedVersion = latestVersion;
                        downloadUpdate(downloadUrl);
                        return;
                    }
                }
                System.err.println("[AutoUpdater] No .jar asset found in release.");
            } else {
                System.out.println("[AutoUpdater] You're on the latest version.");
            }
        } catch (Exception e) {
            System.err.println("[AutoUpdater] Failed to check for update: " + e.getStackTrace());
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
            Files.copy(in, MODS_FOLDER.resolve("skyblock21-" + latestUpdatedVersion + ".jar"), StandardCopyOption.REPLACE_EXISTING);
        }
        System.out.println("[AutoUpdater] Update downloaded. Will be installed on game shutdown.");
    }

}
