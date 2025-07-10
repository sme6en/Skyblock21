package com.skyblock21.events;

import com.skyblock21.util.Area;
import com.skyblock21.util.Location;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.StringIdentifiable;

import java.util.Arrays;

@Environment(EnvType.CLIENT)
public final class SkyblockEvents {
    public static final Event<SkyblockJoin> JOIN = EventFactory.createArrayBacked(SkyblockJoin.class, callbacks -> () -> {
        for (SkyblockEvents.SkyblockJoin callback : callbacks) {
            callback.onSkyblockJoin();
        }
    });

    public static final Event<SkyblockLeave> LEAVE = EventFactory.createArrayBacked(SkyblockLeave.class, callbacks -> () -> {
        for (SkyblockLeave callback : callbacks) {
            callback.onSkyblockLeave();
        }
    });

    public static final Event<SkyblockLocationChange> LOCATION_CHANGE = EventFactory.createArrayBacked(SkyblockLocationChange.class, callbacks -> location -> {
        for (SkyblockLocationChange callback : callbacks) {
            callback.onSkyblockLocationChange(location);
        }
    });

    public static final Event<SkyblockAreaChange> AREA_CHANGE = EventFactory.createArrayBacked(SkyblockAreaChange.class, callbacks -> area -> {
        for (SkyblockAreaChange callback : callbacks) {
            callback.onSkyblockAreaChange(area);
        }
    });

    public static final Event<SkillGained> SKILL_GAINED = EventFactory.createArrayBacked(SkillGained.class, listeners -> (skill, amount) -> {
       for (SkillGained listener : listeners) {
           listener.onSkillGained(skill, amount);
       }
    });

    /**
     * Called when the player's Skyblock profile changes.
     *
     * @implNote This is called upon receiving the chat message for the profile change rather than the exact moment of profile change, so it may be delayed by a few seconds.
     */
    public static final Event<ProfileChange> PROFILE_CHANGE = EventFactory.createArrayBacked(ProfileChange.class, callbacks -> (prev, profile) -> {
        for (ProfileChange callback : callbacks) {
            callback.onSkyblockProfileChange(prev, profile);
        }
    });

    /**
     * <p>Called when the player's skyblock profile is first detected via chat messages.</p>
     * <p>This is useful for initializing data on features that track data for separate profiles separately.</p>
     *
     * @implNote This is called upon receiving the chat message for the profile change rather than the exact moment of profile change, so it may be delayed by a few seconds.
     */
    public static final Event<ProfileInit> PROFILE_INIT = EventFactory.createArrayBacked(ProfileInit.class, callbacks -> profile -> {
        for (ProfileInit callback : callbacks) {
            callback.onSkyblockProfileInit(profile);
        }
    });

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockJoin {
        void onSkyblockJoin();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockLeave {
        void onSkyblockLeave();
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockLocationChange {
        void onSkyblockLocationChange(Location location);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface SkyblockAreaChange {
        void onSkyblockAreaChange(Area area);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface ProfileChange {
        void onSkyblockProfileChange(String prevProfileId, String profileId);
    }

    @Environment(EnvType.CLIENT)
    @FunctionalInterface
    public interface ProfileInit {
        void onSkyblockProfileInit(String profileId);
    }

    public enum Skill implements StringIdentifiable {
        FARMING("Farming"),
        MINING("Mining"),
        COMBAT("Combat"),
        FORAGING("Foraging"),
        HUNTING("Hunting"),
        TAMING("Taming"),
        CARPENTRY("Carpentry"),
        ENCHANTING("Enchanting"),
        SOCIAL("Social"),
        UNKNOWN("Unknown");

        public final String name;

        Skill(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        public static Skill fromName(String displayName) {
            return Arrays.stream(Skill.values())
                    .filter(s -> s.name.equals(displayName))
                    .findFirst()
                    .orElse(UNKNOWN);
        }
    }

    @FunctionalInterface
    public interface SkillGained {
        void onSkillGained(Skill skill, double amount);
    }
}
