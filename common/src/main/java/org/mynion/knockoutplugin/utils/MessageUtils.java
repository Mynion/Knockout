package org.mynion.knockoutplugin.utils;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.compatibility.PapiAdapter;

import java.util.HashMap;

public class MessageUtils {
    public static void sendMessage(Player recipient, String configPath) {
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null && !message.isEmpty()) {
            message = PapiAdapter.setPlaceholders(recipient, message);
            recipient.sendMessage(translateColorCodes(message));
        }
    }

    public static void sendMessage(Player recipient, String configPath, HashMap<String, String> replacements) {
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null && !message.isEmpty()) {
            for (String key : replacements.keySet()) {
                message = message.replace(key, replacements.get(key));
            }
            message = PapiAdapter.setPlaceholders(recipient, message);
            recipient.sendMessage(translateColorCodes(message));
        }
    }

    public static String createMessage(Player recipient, String configPath, HashMap<String, String> replacements){
        String message = Knockout.getPlugin().getConfig().getString(configPath);
        if (message != null && !message.isEmpty()) {
            for (String key : replacements.keySet()) {
                message = message.replace(key, replacements.get(key));
            }
            message = PapiAdapter.setPlaceholders(recipient, message);
            message = translateColorCodes(message);
        }
        return message;
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
            title = translateColorCodes(title);
        }
        if (subtitle != null) {
            if(subtitleReplacements != null) {
                for (String key : subtitleReplacements.keySet()) {
                    subtitle = subtitle.replace(key, subtitleReplacements.get(key));
                }
            }
            subtitle = PapiAdapter.setPlaceholders(p, subtitle);
            subtitle = translateColorCodes(subtitle);
        }
        p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
    }

    static public final String WITH_DELIMITER = "((?<=%1$s)|(?=%1$s))";
    public static String translateColorCodes(String text){

        String[] texts = text.split(String.format(WITH_DELIMITER, "&"));

        StringBuilder finalText = new StringBuilder();

        for (int i = 0; i < texts.length; i++){
            if (texts[i].equalsIgnoreCase("&")){
                //get the next string
                i++;
                if (texts[i].charAt(0) == '#'){
                    finalText.append(net.md_5.bungee.api.ChatColor.of(texts[i].substring(0, 7)) + texts[i].substring(7));
                }else{
                    finalText.append(ChatColor.translateAlternateColorCodes('&', "&" + texts[i]));
                }
            }else{
                finalText.append(texts[i]);
            }
        }

        return finalText.toString();
    }
}
