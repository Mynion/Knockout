package org.mynion.knockoutplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class DropCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {

            // Get knocked out passenger
            Optional<Player> knockedOutPlayer = p.getPassengers().stream()
                    .filter(passenger -> passenger instanceof Player)
                    .map(passenger -> (Player) passenger)
                    .filter(NpcManager::npcExists)
                    .findFirst();

            // Stop carrying a player
            String invalidCommandMessage = KnockoutPlugin.getPlugin().getConfig().getString("invalid-drop-message");
            knockedOutPlayer.ifPresentOrElse(ko -> NpcManager.stopCarrying(ko, p), () -> {
                if (invalidCommandMessage != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidCommandMessage));
                }
            });

        }
        return true;
    }
}
