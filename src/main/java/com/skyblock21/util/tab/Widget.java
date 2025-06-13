package com.skyblock21.util.tab;

import com.skyblock21.util.Location;

import java.util.Arrays;
import java.util.HashMap;

public enum Widget {
    GENERAL_INFO("General Info", "Area"),
    PROFILE("Profile", "Profile"),
    STATS("Stats", "Stats"),
    COLLECTIONS("Collections", "Collection"),
    DAILY_QUESTS("Daily Quests", "Daily Quests"),
    EFFECTS("Effects", "Active Effects"),
    ELECTION("Election", "Election"),
    EVENTS("Events", "Event"),
    FIRE_SALES("Fire Sales", "Fire Sales"),
    FORGE("Forge", "Forges"),
    TIMERS("Timers", "Timers"),
    BESTIARY("Bestiary", "Bestiary"),
    PET("Pet", "Pet"),
    SKILLS("Skill", "Skill"),
    ESSENCE("Essence", "Essence"),

    MINIONS("Minions", "Minions", Location.PRIVATE_ISLAND),
    CATACOMBS("Catacombs", "Dungeons", Location.DUNGEON_HUB),
    PARTY("Party", "Party", Location.DUNGEON_HUB),
    TRAPPER("Trapper", "Trapper", Location.THE_FARMING_ISLAND),
    COMPOSTER("Composter", "Composter", Location.GARDEN),
    CROP_MILESTONES("Crop Milestones", "Crop Milestones", Location.GARDEN),
    VISITORS("Visitors", "Visitors", Location.GARDEN),
    PESTS("Pests", "Pests", Location.GARDEN),
    JACOBS_CONTEST("Jacob's Contest", "Jacob's Contest", Location.HUB, Location.THE_FARMING_ISLAND, Location.GARDEN),
    CRYSTALS("Crystals", "Crystals", Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS),
    COMMISSIONS("Commissions", "Commissions", Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS),
    POWDER("Powder", "Powders", Location.DWARVEN_MINES, Location.CRYSTAL_HOLLOWS),
    DRAGON("Dragon", "Dragon", Location.THE_END),

    MAGE_REPUTATION("Reputation", "Mage Reputation", Location.CRIMSON_ISLE),
    BARBARIAN_REPUTATION("Reputation", "Barbarian Reputation", Location.CRIMSON_ISLE),

    FACTION_QUESTS("Faction Quests", "Faction Quests", Location.CRIMSON_ISLE),
    TROPHY_FISH("Trophy Fish", "Trophy Fish", Location.CRIMSON_ISLE),
    RIFT("Rift", "Good to know", Location.THE_RIFT),
    BARRY("Barry", "Advertisement", Location.THE_RIFT),
    SLAYER("Slayer", "Slayer", Location.HUB, Location.THE_PARK, Location.SPIDERS_DEN, Location.THE_END, Location.CRIMSON_ISLE, Location.THE_RIFT);

    Widget(String name, String prefix, Location... locations) {
        this.name = name;
        this.locations = locations;
        this.prefix = prefix;
        this.everywhere = false;

    }


    Widget(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
        this.locations = null;
        this.everywhere = true;
    }

    private final String name;
    private final String prefix;
    private final Location[] locations;
    private final boolean everywhere;


    public String getPrefix() {
        return prefix;
    }


    public boolean isActive(Location location) {
        if (everywhere) return true;
        //noinspection ConstantConditions
        return Arrays.stream(locations).anyMatch(loc -> loc == location);
    }

    public static Widget byPrefix(String prefix) {
        return BY_PREFIX.get(prefix);
    }

    private static final HashMap<String, Widget> BY_PREFIX;

    static {
        BY_PREFIX = new HashMap<>();
        for (Widget type : values()) {
            BY_PREFIX.put(type.prefix, type);
        }

    }
}
