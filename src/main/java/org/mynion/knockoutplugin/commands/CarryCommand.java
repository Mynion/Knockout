package org.mynion.knockoutplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class CarryCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {

            // Find knocked out player
            Optional<Player> knockedOutPlayer = p.getNearbyEntities(1, 1, 1).stream()
                    .filter(entity -> entity instanceof Player)
                    .map(entity -> (Player) entity)
                    .filter(NpcManager::npcExists)
                    .findFirst();

            // Carry a knocked out player
            knockedOutPlayer.ifPresent(ko -> NpcManager.startCarrying(ko, p));

        }
        return true;
    }
}
