package org.mynion.knockoutplugin.compatibility;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PapiAdapter {
    public static String setPlaceholders(Player p, String text) {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            return PlaceholderAPI.setPlaceholders(p, text);
        }
        return text;
    }
}
