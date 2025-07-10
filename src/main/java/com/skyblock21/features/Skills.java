package com.skyblock21.features;

import com.skyblock21.events.SkyblockEvents;
import com.skyblock21.util.TextUtils;
import net.minecraft.text.Text;
import com.skyblock21.events.SkyblockEvents.Skill;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Skills {

    private static final Pattern skillRegex = Pattern.compile("\\+([\\d.,]+) (.*)");

    private static String last = "";
    public static void onActionBar(Text message) {
        String[] parts = message.getString().split("\\s{2,}");

        for (String part : parts) {
            Matcher matcher = skillRegex.matcher(part);
            if (last != message.getString()) {
//                System.out.println(part);
            }
            if (!matcher.matches()) continue;

            SkyblockEvents.SKILL_GAINED.invoker().onSkillGained(Skill.fromName(matcher.group(1)), Double.parseDouble(matcher.group(0)));
        }

        last = message.getString();
    }
}
