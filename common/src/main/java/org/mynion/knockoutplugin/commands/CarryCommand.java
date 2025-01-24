package org.mynion.knockoutplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class CarryCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {

            NpcManager NpcManager = Knockout.getNpcManager();

            if (!p.hasPermission("knockout.carry")) {
                ChatUtils.sendMessage(p, "no-permission-message");
                return true;
            }

            // Check if the player is already carrying a knocked out player
            if (p.getPassengers().stream()
                    .filter(passenger -> passenger instanceof Player)
                    .map(passenger -> (Player) passenger)
                    .anyMatch(NpcManager::npcExists)) {
                ChatUtils.sendMessage(p, "already-carrying-message");
                return true;
            }

            // Find knocked out player
            Optional<Player> knockedOutPlayer = p.getNearbyEntities(1, 1, 1).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .filter(NpcManager::npcExists)
                    .findFirst();

            // Carry a knocked out player
            knockedOutPlayer.ifPresentOrElse(ko -> NpcManager.startCarrying(ko, p), () -> ChatUtils.sendMessage(p, "invalid-carry-message"));

        }
        return true;
    }
}
