package com.skyblock21.features.items;

import com.skyblock21.config.Skyblock21Config;
import com.skyblock21.config.Skyblock21ConfigManager;
import com.skyblock21.util.TextUtils;
import com.skyblock21.util.Utils;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompactStars {

    private static final String star = "✪";
    private static final List<String> masterStars = Arrays.asList("➊", "➋", "➌", "➍", "➎");
    private static final List<String> circledNumbers = Arrays.asList("➊", "➋", "➌", "➍", "➎", "❻", "❼", "❽", "❾", "❿");
    private static final Pattern starRegex = Pattern.compile("§6" + Pattern.quote(star) + "+");
    private static final Pattern masterStarRegex = Pattern.compile("§c(?<tier>[➊➋➌➍➎])");

    public static Text modifyText(Text text) {
        if (text == null || !Utils.isOnSkyblock() || Skyblock21ConfigManager.get().general.compactStarMode == Skyblock21Config.General.CompactStarMode.NONE || !text.getString().contains(star)) return text;

        String modifiedName = modifyStars(TextUtils.toLegacy(text));

        if (modifiedName.equals(TextUtils.toLegacy(text))) {
            return text;
        }

        return Text.literal(modifiedName);
    }

    private static String modifyStars(String displayName) {
        if (!Utils.isOnSkyblock() || Skyblock21ConfigManager.get().general.compactStarMode == Skyblock21Config.General.CompactStarMode.NONE || !displayName.contains(star)) {
            return displayName;
        }

        try {
            // Count regular stars (✪)
            int regularStars = countMatches(displayName, star);

            // Count master stars by finding the master star symbol
            int masterStars = 0;
            Matcher masterStarMatcher = masterStarRegex.matcher(displayName);
            if (masterStarMatcher.find()) {
                String masterStarSymbol = masterStarMatcher.group("tier");
                masterStars = CompactStars.masterStars.indexOf(masterStarSymbol) + 1;
            }

            int totalStarLevel = regularStars + masterStars;

            if (totalStarLevel == 0) {
                return displayName;
            }

            String cleanedName = displayName.replaceAll(starRegex.pattern(), "")
                                            .replaceAll(masterStarRegex.pattern(), "");

            switch (Skyblock21ConfigManager.get().general.compactStarMode) {
                case COMPACT: {
                    if (totalStarLevel <= 5) {
                        String circledNumber = circledNumbers.get(totalStarLevel - 1);
                        return cleanedName + "§6" + circledNumber;
                    } else {
                        int masterLevel = totalStarLevel - 5;
                        String circledNumber = circledNumbers.get(masterLevel - 1);
                        return cleanedName + "§c" + circledNumber;
                    }
                }
                case COMPACT_TILL_TEN: {
                    String colorCode = totalStarLevel > 5 ? "§c" : "§6";
                    String circledNumber = circledNumbers.get(totalStarLevel - 1);
                    return cleanedName + colorCode + circledNumber;
                }
            }

        } catch (Exception ignored) {
        }

        return displayName;
    }

    // Helper method to count string occurrences (equivalent to Kotlin's countMatches)
    private static int countMatches(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }

    // Helper method for string repetition (if not using Java 11+)
    private static String repeat(String str, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
}
