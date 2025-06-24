package com.skyblock21.features.commandaliases;

import java.util.Objects;

public class Alias {
    public String aliasCommand;
    public String targetCommand;
    public boolean enabled;

    public Alias() {
        this.aliasCommand = "";
        this.targetCommand = "";
        this.enabled = true;
    }

    public Alias(String aliasCommand, String targetCommand, boolean enabled) {
        this.aliasCommand = aliasCommand;
        this.targetCommand = targetCommand;
        this.enabled = enabled;
    }

    public String getDisplayString() {
        if (targetCommand.length() > 40) {
            return targetCommand.substring(0, 37) + "...";
        }
        return targetCommand;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alias that = (Alias) o;
        return Objects.equals(aliasCommand, that.aliasCommand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aliasCommand);
    }
}