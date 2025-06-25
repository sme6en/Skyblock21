package com.skyblock21.features.commandaliases;


import com.skyblock21.config.persistent.PersistentData;

public class CommandAliases {

    public static String processCommand(String originalCommand) {
        String commandWithoutSlash = originalCommand.startsWith("/") ? originalCommand.substring(1).trim() : originalCommand.trim();
        String[] parts = commandWithoutSlash.split(" ", 2);
        String baseCommand = parts[0];
        String args = parts.length > 1 ? parts[1] : "";

        for (Alias alias : PersistentData.get().aliases) {
            if (alias.enabled && alias.aliasCommand.equals(baseCommand)) {
                String targetCommand = alias.targetCommand;

                if (!args.isEmpty()) {
                    targetCommand = targetCommand + " " + args;
                }

                if (!targetCommand.startsWith("/")) {
                    targetCommand = "/" + targetCommand;
                }

                return targetCommand;
            }
        }

        return null;
    }

}
