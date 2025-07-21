package com.skyblock21.util.dev;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.skyblock21.Skyblock21;
import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.util.TextUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
public class AutoUpdater {

    private static final String REPO = "sme6en/Skyblock21";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases/latest";
    private static final Path MODS_FOLDER = FabricLoader.getInstance().getGameDir().resolve("mods");
    private static final Pattern SKYBLOCK21_JAR_PATTERN = Pattern.compile("skyblock21-(.+)\\.jar", Pattern.CASE_INSENSITIVE);

    // Periodic update checker
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ScheduledFuture<?> updateCheckTask;

    // State tracking
    public static String latestAvailableVersion = "";
    public static final Set<String> downloadedVersions = Collections.synchronizedSet(new HashSet<>());
    private static volatile boolean isCheckingForUpdates = false;
    private static File currentModFile = null;

    /**
     * Initialize the auto-updater system
     */
    public static void initialize() {
        try {
            currentModFile = new File(Skyblock21.class.getProtectionDomain()
                                                      .getCodeSource()
                                                      .getLocation()
                                                      .toURI()
                                                      .getPath());
            Skyblock21.LOGGER.info("Current mod file: {}", currentModFile.getAbsolutePath());
        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to get current mod file path", e);
        }

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            shutdown();
        });

        SkyblockEvents.JOIN.register(() -> {
            if (!downloadedVersions.isEmpty()) {
                CompletableFuture.runAsync(() -> {
                    try {
                        Thread.sleep(3000);
                        if (!"".equals(latestAvailableVersion)) {
                            showChatNotification(latestAvailableVersion);
                        }

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        });

        registerCommands();

        checkForUpdateAsync(true);

        startPeriodicUpdateCheck();

        cleanupOldModFilesOnStartup();
    }

    /**
     * Register update commands
     */
    private static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("restart_skyblock21")
                                                    .executes(context -> {
                                                        MinecraftClient client = MinecraftClient.getInstance();
                                                        if (client.player != null) {
                                                            TextUtils.addMessage("§aRestarting game to apply update...", true, false);

                                                            CompletableFuture.runAsync(() -> {
                                                                try {
                                                                    Thread.sleep(1500);
                                                                    client.execute(client::scheduleStop);
                                                                } catch (InterruptedException e) {
                                                                    Thread.currentThread().interrupt();
                                                                }
                                                            });
                                                        }
                                                        return 1;
                                                    }));

            dispatcher.register(ClientCommandManager.literal("dismiss_skyblock21")
                                                    .executes(context -> {
                                                        MinecraftClient client = MinecraftClient.getInstance();
                                                        if (client.player != null) {
                                                            TextUtils.addMessage("§7Update notification dismissed. You can restart later to apply the update.", true, false);
                                                            Skyblock21.LOGGER.info("User dismissed update notification");
                                                        }
                                                        return 1;
                                                    }));
        });
    }

    /**
     * Start periodic update checking every 60 minutes
     */
    private static void startPeriodicUpdateCheck() {
        if (updateCheckTask != null && !updateCheckTask.isDone()) {
            updateCheckTask.cancel(false);
        }

        int intervalMinutes = 60;

        updateCheckTask = scheduler.scheduleWithFixedDelay(() -> checkForUpdateAsync(false),
                intervalMinutes,
                intervalMinutes,
                TimeUnit.MINUTES);
    }

    /**
     * Check for updates asynchronously
     */
    public static void checkForUpdateAsync(boolean isInitialCheck) {
        if (isCheckingForUpdates) {
            Skyblock21.LOGGER.debug("Update check already in progress, skipping");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                checkForUpdate(isInitialCheck);
            } catch (Exception e) {
                Skyblock21.LOGGER.error("Failed to check for updates", e);
            }
        });
    }

    public static void checkForUpdateAsyncAndRestart() {
        if (isCheckingForUpdates) {
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                checkForUpdate(false);
                if (!latestAvailableVersion.isEmpty()) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    TextUtils.addMessage("§aUpdate downloaded! Restarting game in 3s to apply the update...", true, false);
                    Thread.sleep(3000);
                    client.execute(client::scheduleStop);
                } else {
                    TextUtils.addMessage("§eYou are already on the latest version!", true, false);
                }
            } catch (Exception e) {
                Skyblock21.LOGGER.error("Failed to check for updates", e);
            }
        });
    }

    /**
     * Main update checking logic
     */
    private static void checkForUpdate(boolean isInitialCheck) {
        if (isCheckingForUpdates) return;

        isCheckingForUpdates = true;

        try {
            if (isInitialCheck) {
                Skyblock21.LOGGER.info("Checking for updates on client startup...");
            } else {
                Skyblock21.LOGGER.info("Performing periodic update check...");
            }

            String response = fetch(API_URL);
            JsonObject json = JsonParser.parseString(response).getAsJsonObject();

            String latestVersion = json.get("tag_name").getAsString().replaceFirst("^v", "");

            if (isNewerVersion(latestVersion, Skyblock21.MOD_VERSION) && !isVersionDownloaded(latestVersion)) {
                Skyblock21.LOGGER.info("New version found: {} (current: {})", latestVersion, Skyblock21.MOD_VERSION);

                JsonArray assets = json.getAsJsonArray("assets");
                String downloadUrl = null;

                for (JsonElement assetElem : assets) {
                    JsonObject asset = assetElem.getAsJsonObject();
                    String url = asset.get("browser_download_url").getAsString();
                    if (url.endsWith(".jar")) {
                        downloadUrl = url;
                        break;
                    }
                }

                if (downloadUrl != null) {
                    latestAvailableVersion = latestVersion;
                    downloadUpdate(downloadUrl, latestVersion);
                    showUpdateNotification(latestVersion, isInitialCheck);
                } else {
                    Skyblock21.LOGGER.warn("No .jar asset found in release");
                }
            } else if (isVersionDownloaded(latestVersion)) {
                Skyblock21.LOGGER.debug("Version {} already downloaded, skipping", latestVersion);
            } else {
                if (isInitialCheck) {
                    Skyblock21.LOGGER.info("You're on the latest version: {}", Skyblock21.MOD_VERSION);
                }
            }
        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to check for updates", e);
        } finally {
            isCheckingForUpdates = false;
        }
    }

    /**
     * Check if a version is already downloaded
     */
    private static boolean isVersionDownloaded(String version) {
        if (downloadedVersions.contains(version)) {
            return true;
        }

        Path expectedPath = MODS_FOLDER.resolve("skyblock21-" + version + ".jar");
        boolean exists = Files.exists(expectedPath);

        if (exists) {
            downloadedVersions.add(version);
        }

        return exists;
    }

    /**
     * Download the update
     */
    private static void downloadUpdate(String urlStr, String version) throws IOException {
        Skyblock21.LOGGER.info("Downloading update version {}...", version);

        String filename = "skyblock21-" + version + ".jar";
        Path downloadPath = MODS_FOLDER.resolve(filename);

        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, downloadPath, StandardCopyOption.REPLACE_EXISTING);
        }

        downloadedVersions.add(version);

        setupCleanupOnShutdown();

        Skyblock21.LOGGER.info("Update downloaded successfully: {}", downloadPath);
    }

    /**
     * Setup cleanup to run when the game shuts down
     */
    private static volatile boolean cleanupSetup = false;
    private static void setupCleanupOnShutdown() {
        if (cleanupSetup) return;

        cleanupSetup = true;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                cleanupOldModFilesOnShutdown();
            } catch (Exception e) {
                Skyblock21.LOGGER.error("Failed to cleanup old mod files on shutdown", e);
            }
        }));
    }

    /**
     * Clean up old mod files on startup (from previous sessions)
     */
    private static void cleanupOldModFilesOnStartup() {
        try {
            List<Path> skyblockJars = findAllSkyblockJars();
            String currentVersion = Skyblock21.MOD_VERSION;

            Skyblock21.LOGGER.info("Found {} Skyblock21 jar files in mods folder", skyblockJars.size());

            for (Path jar : skyblockJars) {
                String version = extractVersionFromFileName(jar.getFileName().toString());
                if (version != null && !version.equals(currentVersion)) {
                    try {
                        Files.deleteIfExists(jar);
                        Skyblock21.LOGGER.info("Cleaned up old mod file from previous session: {}", jar.getFileName());
                    } catch (IOException e) {
                        Skyblock21.LOGGER.warn("Failed to delete old mod file: {}", jar, e);
                    }
                }
            }
        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to cleanup old mod files on startup", e);
        }
    }

    /**
     * Clean up old mod files on shutdown, keeping only the latest version
     */
    private static void cleanupOldModFilesOnShutdown() {
        try {
            List<Path> skyblockJars = findAllSkyblockJars();

            if (skyblockJars.isEmpty()) {
                return;
            }

            String latestVersion = findLatestVersionFromJars(skyblockJars);

            if (latestVersion == null) {
                Skyblock21.LOGGER.warn("Could not determine latest version from jar files");
                return;
            }

            Skyblock21.LOGGER.info("Latest version found: {}, cleaning up older versions", latestVersion);

            List<Path> jarsToDelete = new ArrayList<>();
            for (Path jar : skyblockJars) {
                String version = extractVersionFromFileName(jar.getFileName().toString());
                if (version != null && !version.equals(latestVersion)) {
                    jarsToDelete.add(jar);
                }
            }

            if (jarsToDelete.isEmpty()) {
                Skyblock21.LOGGER.info("No old versions to clean up");
                return;
            }

            if (Util.getOperatingSystem() != Util.OperatingSystem.WINDOWS) {
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        for (Path jar : jarsToDelete) {
                            try {
                                Files.deleteIfExists(jar);
                                Skyblock21.LOGGER.info("Deleted old version: {}", jar.getFileName());
                            } catch (IOException e) {
                                Skyblock21.LOGGER.warn("Failed to delete old version: {}", jar, e);
                            }
                        }
                }));

                Skyblock21.LOGGER.info("Scheduled {} old jar files for deletion on shutdown", jarsToDelete.size());
            } else {
                createWindowsCleanupScript(jarsToDelete);
            }
        } catch (Exception e) {
            Skyblock21.LOGGER.error("Failed to cleanup old mod files on shutdown", e);
        }
    }

    /**
     * Find all Skyblock21 jar files in the mods folder
     */
    private static List<Path> findAllSkyblockJars() throws IOException {
        if (!Files.exists(MODS_FOLDER)) {
            return Collections.emptyList();
        }

        return Files.list(MODS_FOLDER)
                    .filter(path -> {
                        String fileName = path.getFileName().toString().toLowerCase();
                        return fileName.startsWith("skyblock21") && fileName.endsWith(".jar");
                    })
                    .collect(Collectors.toList());
    }

    /**
     * Extract version from jar filename
     */
    private static String extractVersionFromFileName(String fileName) {
        Matcher matcher = SKYBLOCK21_JAR_PATTERN.matcher(fileName);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Find the latest version among all jar files
     */
    private static String findLatestVersionFromJars(List<Path> jarFiles) {
        String latestVersion = null;

        for (Path jar : jarFiles) {
            String version = extractVersionFromFileName(jar.getFileName().toString());
            if (version != null) {
                if (latestVersion == null || isNewerVersion(version, latestVersion)) {
                    latestVersion = version;
                }
            }
        }

        return latestVersion;
    }

    /**
     * Create Windows cleanup script for multiple files
     */
    private static void createWindowsCleanupScript(List<Path> filesToDelete) {

        // NOTE: This is a workaround for Windows, which doesn't work well with File.deleteOnExit()
        // Instead, we create a batch file that will delete the old mod file on game shutdown.
        // I know it looks very sus, but it's the only way to ensure the file is deleted properly

        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            File deleter = new File(tempDir, "skyblock21_cleanup.bat");

            StringBuilder deleterProgram = new StringBuilder();
            deleterProgram.append("@echo off\n");

            deleterProgram.append("timeout /t 3 /nobreak >nul\n\n");

            for (Path jarFile : filesToDelete) {
                String jarPath = jarFile.toAbsolutePath().toString();
                String jarName = jarFile.getFileName().toString();

                deleterProgram.append("rem Processing: ").append(jarName).append("\n");
                deleterProgram.append(":TestFile_").append(jarName.replace(".", "_").replace("-", "_")).append("\n");
                deleterProgram.append("if not exist \"").append(jarPath).append("\" goto FileGone_").append(jarName.replace(".", "_").replace("-", "_")).append("\n");
                deleterProgram.append("REN \"").append(jarPath).append("\" \"").append(jarName).append("\" 2>nul\n");
                deleterProgram.append("IF ERRORLEVEL 1 (\n");
                deleterProgram.append("    timeout /t 1 /nobreak >nul\n");
                deleterProgram.append("    goto TestFile_").append(jarName.replace(".", "_").replace("-", "_")).append("\n");
                deleterProgram.append(")\n");

                deleterProgram.append("ECHO Deleting old version: ").append(jarName).append("\n");
                deleterProgram.append("DEL /F /Q \"").append(jarPath).append("\" >nul 2>&1\n");
                deleterProgram.append("if exist \"").append(jarPath).append("\" (\n");
                deleterProgram.append("    ECHO Failed to delete ").append(jarName).append("\n");
                deleterProgram.append(") else (\n");
                deleterProgram.append("    ECHO Successfully deleted ").append(jarName).append("\n");
                deleterProgram.append(")\n");

                deleterProgram.append(":FileGone_").append(jarName.replace(".", "_").replace("-", "_")).append("\n");
                deleterProgram.append("\n");
            }

            deleterProgram.append("ECHO Cleanup completed\n");
            deleterProgram.append("EXIT\n");

            String finalScript = deleterProgram.toString();

            Files.writeString(deleter.toPath(), finalScript, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            ProcessBuilder pb = new ProcessBuilder("cmd", "/c", deleter.getAbsolutePath());
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            pb.start();
        } catch (IOException e) {
            Skyblock21.LOGGER.error("Failed to create Windows cleanup script", e);
        }
    }

    /**
     * Show update notification
     */
    private static void showUpdateNotification(String newVersion, boolean isInitialCheck) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null && client.player != null) {
            showChatNotification(newVersion);
        } else {
            Skyblock21.LOGGER.info("Update {} available! Restart the game to apply the update.", newVersion);
        }
    }

    /**
     * Show in-game chat notification
     */
    private static void showChatNotification(String newVersion) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client != null && client.player != null) {
            client.execute(() -> {
                try {
                    // Show how many updates are available
                    int totalUpdates = downloadedVersions.size();
                    String updateText = totalUpdates > 1 ?
                            String.format("§3§l[Skyblock21] §r§b%d Updates Available!", totalUpdates) :
                            "§3§l[Skyblock21] §r§bUpdate Available!";

                    Text updateMessage = Text.literal(updateText).styled(style -> style.withBold(true));

                    Text versionMessage = Text.literal(String.format("§7Current: §f%s §8→ §7Latest: §a%s", Skyblock21.MOD_VERSION, newVersion));

                    Text restartButton = Text.literal("§a§l[RESTART NOW]")
                                             .styled(style -> style.withBold(true)
                                                                   .withClickEvent(new ClickEvent.RunCommand("/restart_skyblock21"))
                                                                   .withHoverEvent(new HoverEvent.ShowText(Text.literal("§eClick to restart the game and apply the update"))));

                    Text laterButton = Text.literal("§c§l[LATER]")
                                           .styled(style -> style.withBold(true)
                                                                 .withClickEvent(new ClickEvent.RunCommand("/dismiss_skyblock21"))
                                                                 .withHoverEvent(new HoverEvent.ShowText(Text.literal("§eClick to dismiss this notification"))));

                    Text infoButton = Text.literal("§b§l[INFO]")
                                          .styled(style -> style.withBold(true)
                                                                .withClickEvent(new ClickEvent.OpenUrl(URI.create("https://github.com/" + REPO + "/releases/latest")))
                                                                .withHoverEvent(new HoverEvent.ShowText(Text.literal("§eClick to view release notes on GitHub"))));

                    Text buttonRow = Text.literal("")
                                         .append(restartButton)
                                         .append(Text.literal(" §7| "))
                                         .append(laterButton)
                                         .append(Text.literal(" §7| "))
                                         .append(infoButton);

                    client.player.sendMessage(Text.literal("§7§m                                                    "), false);
                    client.player.sendMessage(updateMessage, false);
                    client.player.sendMessage(versionMessage, false);
                    if (totalUpdates > 1) {
                        client.player.sendMessage(Text.literal(String.format("§7%d updates downloaded! Restart to apply all updates.", totalUpdates)), false);
                    } else {
                        client.player.sendMessage(Text.literal("§7Update downloaded! Choose an option:"), false);
                    }
                    client.player.sendMessage(buttonRow, false);
                    client.player.sendMessage(Text.literal("§7§m                                                    "), false);

                    Skyblock21.LOGGER.info("Update notification sent to chat");

                } catch (Exception e) {
                    Skyblock21.LOGGER.error("Failed to send chat notification", e);
                }
            });
        }
    }

    /**
     * Compare version strings
     */
    private static boolean isNewerVersion(String latest, String current) {
        try {
            String[] l = latest.split("\\.");
            String[] c = current.split("\\.");

            for (int i = 0; i < Math.max(l.length, c.length); i++) {
                int li = i < l.length ? Integer.parseInt(l[i]) : 0;
                int ci = i < c.length ? Integer.parseInt(c[i]) : 0;

                if (li > ci) return true;
                if (li < ci) return false;
            }
            return false;
        } catch (NumberFormatException e) {
            Skyblock21.LOGGER.warn("Failed to parse version numbers, falling back to string comparison");
            return !latest.equals(current);
        }
    }

    private static String findLatestDownloadedVersion() {
        if (downloadedVersions.isEmpty()) {
            return null;
        }

        String latest = null;
        for (String version : downloadedVersions) {
            if (latest == null || isNewerVersion(version, latest)) {
                latest = version;
            }
        }
        return latest;
    }

    /**
     * Fetch content from URL
     */
    private static String fetch(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestProperty("User-Agent", "Skyblock21-AutoUpdater/1.0");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            return out.toString();
        }
    }

    /**
     * Shutdown the auto-updater
     */
    public static void shutdown() {
        if (updateCheckTask != null && !updateCheckTask.isDone()) {
            updateCheckTask.cancel(true);
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }

        Skyblock21.LOGGER.info("Auto-updater shutdown complete");
    }
}