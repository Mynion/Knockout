package org.mynion.knockoutplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.KnockoutPlugin;

public class ChatUtils {
    public static void sendPlayerMessage(Player player, String configPath) {
        String message = KnockoutPlugin.getPlugin().getConfig().getString(configPath);
        if (message != null) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
