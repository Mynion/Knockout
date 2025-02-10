package org.mynion.knockoutplugin.compatibility;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class PapiAdapter {
    public static String setPlaceholders(Player p, String text) {
        Plugin papiPlugin = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        if (papiPlugin == null) return text;
        if (!papiPlugin.isEnabled()) return text;
        return PlaceholderAPI.setPlaceholders(p, text);
    }
}
