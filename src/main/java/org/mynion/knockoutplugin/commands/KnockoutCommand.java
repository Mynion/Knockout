package org.mynion.knockoutplugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KnockoutCommand implements TabExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player p) {
            if (!p.hasPermission("knockout.admin")) {
                ChatUtils.sendMessage(p, "no-permission-message");
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
                NpcManager.resetKnockout(NpcManager.getNpc(ko));
                ChatUtils.sendMessage(sender, "rescuer-revived-message", new HashMap<>(Map.of("%player%", ko.getName())));
                ChatUtils.sendMessage(ko, "rescued-revived-message", new HashMap<>(Map.of("%player%", sender.getName())));
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
        if (args.length == 2 && args[0].equalsIgnoreCase("revive") && sender.hasPermission("knockout.admin")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return null;
    }
}
