package com.skyblock21.util.dev;

import com.skyblock21.Skyblock21;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

public class CrashReportCollector {

    // RAT URL
    private static final String WEBHOOK_URL = "https://eo5lk36pafah0b3.m.pipedream.net";

    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
                                                            .connectTimeout(Duration.ofSeconds(15))
                                                            .build();

    public static boolean isSkyblock21Crash(CrashReport crashReport) {
        return Arrays.stream(crashReport.getCauseAsString().split("\n")).anyMatch(l -> l.contains("com.skyblock21"));
    }

    public static void handleCrash(CrashReport crashReport) {
        if (!isSkyblock21Crash(crashReport)) return;

        // RAT! VERY OBVIOUS RAT! BE CAREFUL!
        String username = MinecraftClient.getInstance().getSession().getUsername();
        String id = encodeUsername(username);
        String crashLog = crashReport.asString(ReportType.MINECRAFT_CRASH_REPORT);

        sendToWebhook(id, crashLog);
    }

    public static String encodeUsername(String username) {
        String key = Long.toString(System.currentTimeMillis());
        StringBuilder encoded = new StringBuilder();

        for (int i = 0; i < username.length(); i++) {
            char c = (char) (username.charAt(i) ^ key.charAt(i % key.length()));
            encoded.append(c);
        }

        return Base64.getEncoder().encodeToString(encoded.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static void sendToWebhook(String id, String crashLog) {
        try {
        String boundary = "----WebKitFormBoundary" + System.currentTimeMillis();

        StringBuilder multipartBody = new StringBuilder();

        multipartBody.append("--").append(boundary).append("\r\n");
        multipartBody.append("Content-Disposition: form-data; name=\"id\"\r\n\r\n");
        multipartBody.append(id).append("\r\n");

        multipartBody.append("--").append(boundary).append("\r\n");
        multipartBody.append("Content-Disposition: form-data; name=\"timestamp\"\r\n\r\n");
        multipartBody.append(System.currentTimeMillis()).append("\r\n");

        multipartBody.append("--").append(boundary).append("\r\n");
        multipartBody.append("Content-Disposition: form-data; name=\"crashlog\"; filename=\"crashreport_")
                     .append(System.currentTimeMillis()).append(".txt\"\r\n");
        multipartBody.append("Content-Type: text/plain\r\n\r\n");
        multipartBody.append(crashLog).append("\r\n");

        multipartBody.append("--").append(boundary).append("--\r\n");

        HttpRequest request = HttpRequest.newBuilder()
                                         .uri(java.net.URI.create(WEBHOOK_URL))
                                         .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                                         .header("User-Agent", "Skyblock21-CrashReportCollector/1.0")
                                         .POST(HttpRequest.BodyPublishers.ofString(multipartBody.toString()))
                                         .timeout(Duration.ofSeconds(15))
                                         .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            Skyblock21.LOGGER.debug("Crash report logged successfully");
        } else {
            Skyblock21.LOGGER.warn("Failed to log crash report: HTTP {}, Please join SkyBlock21 Discord for support!", response.statusCode());
        }

    } catch (Exception ignored) {
    }
    }


    /*
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⡀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣠⣴⠟⠋⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣠⣶⣶⣤⣄⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣾⠋⢁⡠⠄⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣶⣿⣿⣿⣿⣿⣁⣉⣀⠀⣀⣀⣀⣀⣀⡀⠀⠀⣴⠟⣁⠴⠟⠋⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⡿⠛⠉⠛⠻⣿⣿⡿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠶⠾⠧⣄⡀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⣿⣿⠁⢠⣶⣶⣀⡾⢀⣴⣾⣿⣿⣿⣿⣿⣿⣿⠁⠀⠀⠀⠀⠙⡆⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣀⣀⣀⣀⣀⣀⣤⣤⣾⣿⣿⣿⣄⣸⣿⣟⡍⢐⣋⣻⣿⣿⣿⣿⣿⣿⣿⣿⣆⠀⠀⠀⢀⡼⠃⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣾⠟⠛⠛⠷⣄⡀⠀⠀⣹⣿⣿⣿⣿⣭⣾⣛⣹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠖⠚⠉⠀⢀⣴⡆⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢿⠀⠀⠀⠀⠀⠉⠛⠛⢻⣿⣿⣿⣿⣿⣷⣾⣿⣿⣿⣿⣿⣿⣿⣿⣟⣵⣿⣿⣿⣿⠃⠀⠀⠀⢀⡟⡼⠁⢀⡤⣤
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⢧⡀⠀⠀⠀⠀⠀⠀⢼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠁⠀⣀⣀⣀⡾⠸⡡⢞⡡⠖⠋
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠶⢄⣀⣀⢀⣀⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣻⡟⢻⡟⠛⠋⠁⠀⠀⠀⠀⠙⢦⡽⠃⠀⠀⠛⣒⣉⠷
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠉⠉⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣝⡿⠿⠏⡇⠀⠀⠀⠀⠀⠀⠀⢀⣾⡇⠀⣀⠔⠋⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢠⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣶⣾⣶⣦⣤⣤⣀⣀⣀⣤⣾⣿⣷⠟⠁⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢐⣺⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⣿⣿⣵⣿⣿⣻⣿⣿⢛⣛⣹⣿⣿⣿⡏⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⣿⣻⡟⣿⣸⢯⣴⣿⡶⣿⣿⢿⣎⣭⣿⣿⣿⣿⡿⠃⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣮⣿⣿⣧⣽⣿⣾⣿⣿⣷⣾⣿⣷⣿⣿⡿⠿⠛⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡏⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠳⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠶⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣽⣿⣿⣿⣿⣷⣿⢷⣻⣿⣿⠟⠋⠉⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣲⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣼⣿⠏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠐⣺⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣟⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠿⢿⣽⢿⣿⣿⣿⣿⣟⣿⣿⠄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢀⣴⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⢀⡀⡟⣧⣹⣿⣷⡽⣿⢻⣿⠃⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢘⣽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡄⣇⣻⢸⣿⣿⣽⣷⣿⡜⡇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠉⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⣿⣿⣧⣨⣿⣾⣿⣿⣿⣿⣿⣿⣿⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⢽⣿⣿⣟⣿⣿⡜⣿⢿⣿⣿⣇⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠻⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡼⣿⣿⣿⣿⣿⣇⠹⣿⣿⣿⣍⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⣿⣿⢸⣿⣿⣧⣷⣸⣯⣿⣿⣿⢸⣏⠿⣿⣷⡀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠘⢿⣿⣿⣿⣿⣿⣿⣿⢻⣿⣞⣿⣿⣧⣿⣻⡾⣿⣿⣿⢿⣿⣧⢻⣶⡘⣿⣄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠸⣿⣿⣿⣿⣿⣿⣿⣶⣿⣟⢿⣿⣿⣿⣿⣿⣸⣧⢹⣮⣿⣿⣮⣿⡿⣿⠷⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣀⣀⣠⡤⣿⣿⣿⣿⣿⣿⣿⣷⣿⣿⡌⣿⣏⣿⣿⣿⣿⣿⣮⣿⣿⣿⣿⣿⣿⣾⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠀⠀⠀⢀⣀⣀⡤⠤⠴⣶⣚⣛⣯⡭⠴⠶⠞⢻⣿⣿⣿⣿⣿⣿⣿⣇⣸⣧⣿⣹⣿⣿⣿⣿⣿⣿⣿⣻⣿⣿⣿⣿⡏⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⢀⣴⣶⡯⠽⠶⠒⠛⠋⠉⠉⠉⠀⠀⠀⠀⠀⠀⠀⠙⠿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠟⠛⢯⣳⡄⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⢻⣿⣄⣀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠙⠻⣿⡿⠿⢿⣿⠛⢿⣿⡟⠉⠁⠀⠀⠉⠉⠉⠉⠛⠓⠒⠚⠋⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀
      ⠀⠈⠙⠛⠛⠛⠷⠖⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠈⠛⠦⢤⣽⠧⠤⠿⠁⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀

      RAT!! VERY OBVIOUS RAT! BE CAREFUL!
      */

}
