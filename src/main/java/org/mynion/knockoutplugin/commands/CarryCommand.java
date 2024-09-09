package org.mynion.knockoutplugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class CarryCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {

            if (!p.hasPermission("knockout.carry")) {
                String noPermissionMessage = KnockoutPlugin.getPlugin().getConfig().getString("no-permission-message");
                if (noPermissionMessage != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', noPermissionMessage));
                }
                return true;
            }

            // Check if the player is already carrying a knocked out player
            if (p.getPassengers().stream()
                    .filter(passenger -> passenger instanceof Player)
                    .map(passenger -> (Player) passenger)
                    .anyMatch(NpcManager::npcExists)) {
                String alreadyCarryingMessage = KnockoutPlugin.getPlugin().getConfig().getString("already-carrying-message");
                if (alreadyCarryingMessage != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', alreadyCarryingMessage));
                }
                return true;
            }

            // Find knocked out player
            Optional<Player> knockedOutPlayer = p.getNearbyEntities(1, 1, 1).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .filter(NpcManager::npcExists)
                    .findFirst();

            // Carry a knocked out player
            String invalidCommandMessage = KnockoutPlugin.getPlugin().getConfig().getString("invalid-carry-message");
            knockedOutPlayer.ifPresentOrElse(ko -> NpcManager.startCarrying(ko, p), () -> {
                if (invalidCommandMessage != null) {
                    p.sendMessage(ChatColor.translateAlternateColorCodes('&', invalidCommandMessage));
                }
            });

        }
        return true;
    }
}
