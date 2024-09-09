package org.mynion.knockoutplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

public class DieCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String invalidCommandMessage = KnockoutPlugin.getPlugin().getConfig().getString("invalid-die-message");
        if (sender instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                NpcManager.forceKill(p);
            } else if (invalidCommandMessage != null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidCommandMessage));
            }
        }
        return true;
    }
}
