package org.mynion.knockoutplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnockoutCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        NpcManager NpcManager = Knockout.getNpcManager();

        if (sender instanceof Player p) {
            if (!p.hasPermission("knockout.admin")) {
                MessageUtils.sendMessage(p, "no-permission-message");
                return true;
            }
        }

        if (args.length == 0) {
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            Knockout.getPlugin().reloadConfig();
            sender.sendMessage(ChatColor.GREEN + "Knockout config reloaded.");

        } else if (args[0].equalsIgnoreCase("revive") && args.length >= 2) {
            Player ko = Bukkit.getPlayer(args[1]);
            if (NpcManager.npcExists(ko)) {
                NpcManager.endKnockout(ko, false);
                MessageUtils.sendMessage(sender, "rescuer-revived-message", new HashMap<>(Map.of("%player%", ko.getName())));
                MessageUtils.sendMessage(ko, "rescued-revived-message", new HashMap<>(Map.of("%player%", sender.getName())));
                NpcManager.runConfigCommands("ConsoleAfterReviveCommands", ko, false);
            } else {
                sender.sendMessage(ChatColor.RED + "That player is not knocked out!");
            }

        } else if (args[0].equalsIgnoreCase("knockout") && args.length >= 2) {
            Player ko = Bukkit.getPlayer(args[1]);
            if (NpcManager.npcExists(ko)) {
                sender.sendMessage(ChatColor.RED + "That player is already knocked out!");
            } else if (ko != null) {
                int time = Knockout.getPlugin().getConfig().getInt("knockout-time");
                if (time < 0) {
                    time = 60;
                }
                if (args.length >= 3) {
                    try {
                        time = Integer.parseInt(args[2]);
                    } catch (Exception e) {
                        sender.sendMessage(ChatColor.RED + "Invalid time.");
                        return true;
                    }
                    if (time < 0 || time > 86399) {
                        sender.sendMessage(ChatColor.RED + "Invalid time.");
                        return true;
                    }
                }
                NpcManager.knockoutPlayer(ko, null, time);
                sender.sendMessage(ChatColor.GREEN + "Player knocked out for " + time + " seconds.");
            } else {
                sender.sendMessage(ChatColor.RED + "Player not found.");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && sender.hasPermission("knockout.admin")) {
            return List.of("reload", "revive", "knockout");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("revive") && sender.hasPermission("knockout.admin")) {
            return Knockout.getNpcManager().getNPCs().stream()
                    .map(npc -> npc.getPlayer().getName()).toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("knockout") && sender.hasPermission("knockout.admin")) {
            return List.of("60");
        }
        return null;
    }
}
