package org.mynion.knockoutplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.List;

public class KnockoutCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player p) {
            if (!p.hasPermission("knockout.admin")) {
                String noPermissionMessage = KnockoutPlugin.getPlugin().getConfig().getString("no-permission-message");
                if (noPermissionMessage != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                }
                return true;
            }
        }

        if (args.length == 0) {
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            KnockoutPlugin.getPlugin().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Knockout config reloaded.");
        } else if (args[0].equalsIgnoreCase("revive") && args.length >= 2) {
            Player ko = Bukkit.getPlayer(args[1]);
            if (NpcManager.npcExists(ko)) {
                NpcManager.resetKnockout(NpcManager.getNpc(ko));
                String revivedMessage = KnockoutPlugin.getPlugin().getConfig().getString("rescuer-revived-message");
                if (revivedMessage != null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', revivedMessage.replace("%player%", ko.getName())));
                }
            } else {
                sender.sendMessage(ChatColor.RED + "That player is not knocked out!");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("knockout.admin")) {
            return List.of("reload", "revive");
        }
        return null;
    }
}
