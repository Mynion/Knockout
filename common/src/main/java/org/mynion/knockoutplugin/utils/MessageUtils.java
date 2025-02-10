package org.mynion.knockoutplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.compatibility.PapiAdapter;

import java.util.HashMap;

public class MessageUtils {
    public static void sendMessage(Player recipient, String configPath) {
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null && !message.isEmpty()) {
            message = PapiAdapter.setPlaceholders(recipient, message);
            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public static void sendMessage(Player recipient, String configPath, HashMap<String, String> replacements) {
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null && !message.isEmpty()) {
            for (String key : replacements.keySet()) {
                message = message.replace(key, replacements.get(key));
            }
            message = PapiAdapter.setPlaceholders(recipient, message);
            recipient.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public static void sendTitle(Player p, String titleConfigPath, String subtitleConfigPath, HashMap<String, String> titleReplacements, HashMap<String, String> subtitleReplacements, int fadeIn, int stay, int fadeOut) {
        String title = Knockout.getPlugin().getConfig().getString(titleConfigPath);
        String subtitle = Knockout.getPlugin().getConfig().getString(subtitleConfigPath);
        if (title != null) {
            if(titleReplacements != null) {
                for (String key : titleReplacements.keySet()) {
                    title = title.replace(key, titleReplacements.get(key));
                }
            }
            title = PapiAdapter.setPlaceholders(p, title);
            title = ChatColor.translateAlternateColorCodes('&', title);
        }
        if (subtitle != null) {
            if(subtitleReplacements != null) {
                for (String key : subtitleReplacements.keySet()) {
                    subtitle = subtitle.replace(key, subtitleReplacements.get(key));
                }
            }
            subtitle = PapiAdapter.setPlaceholders(p, subtitle);
            subtitle = ChatColor.translateAlternateColorCodes('&', subtitle);
        }
        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }
}
