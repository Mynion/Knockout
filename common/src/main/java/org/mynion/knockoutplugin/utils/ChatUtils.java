package org.mynion.knockoutplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.mynion.knockoutplugin.Knockout;

import java.util.HashMap;

public class ChatUtils {
    public static void sendMessage(CommandSender recipient, String configPath) {
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null) {
            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public static void sendMessage(CommandSender recipient, String configPath, HashMap<String, String> replacements) {
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null) {
            for (String key : replacements.keySet()) {
                message = message.replace(key, replacements.get(key));
            }
            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
