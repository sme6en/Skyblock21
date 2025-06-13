package com.skyblock21.config;

public class Skyblock21Config {

    public General general = new General();

    public Mining mining = new Mining();

    public static class General {
        public boolean noFog = false;
        public boolean noMouseReset = true;
        public boolean copyToClipboardRNGs = true;
        public boolean boosterCookieReminder = true;
        public int boosterCookieReminderHours = 6;
        public boolean godPotReminder = true;
    }

    public static class Mining {
        public boolean scathaAlerts = true;
        public boolean mouseLockKeybind = true;
    }
}
