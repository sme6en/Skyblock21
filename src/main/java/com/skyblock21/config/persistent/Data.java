package com.skyblock21.config.persistent;

import com.skyblock21.features.Scathas.ScathasData;
import com.skyblock21.features.commandaliases.Alias;
import com.skyblock21.features.keyshortcuts.KeyShortcuts;
import com.skyblock21.features.keyshortcuts.Shortcut;
import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.HashSet;
import java.util.Set;

public class Data {

    @SerialEntry
    public ScathasData scathasData = new ScathasData();

    @SerialEntry
    public final Set<Shortcut> shortcuts = new HashSet<>();

    @SerialEntry
    public final Set<Alias> aliases = new HashSet<>();

}
