package org.mynion.knockoutplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class DropCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {

            NpcManager NpcManager = Knockout.getNpcManager();

            if (!p.hasPermission("knockout.drop")) {
                MessageUtils.sendMessage(p, "no-permission-message");
                return true;
            }

            // Get knocked out passenger
            Optional<Player> knockedOutPlayer = p.getPassengers().stream()
                    .filter(passenger -> passenger instanceof Player)
                    .map(passenger -> (Player) passenger)
                    .filter(NpcManager::npcExists)
                    .findFirst();

            // Stop carrying a player
            knockedOutPlayer.ifPresentOrElse(ko -> NpcManager.stopCarrying(ko, p), () -> MessageUtils.sendMessage(p, "invalid-drop-message"));

        }
        return true;
    }
}
