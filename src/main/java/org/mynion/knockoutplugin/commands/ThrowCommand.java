package org.mynion.knockoutplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class ThrowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            Optional<Player> knockedOutPlayer = p.getPassengers().stream().filter(pass -> {
                if (pass instanceof Player pl) {
                    return NpcManager.npcExists(pl);
                }
                return false;
            }).findFirst().map(pl -> (Player) pl);

            knockedOutPlayer.ifPresent(ko -> {
                p.removePassenger(ko);
                NpcManager.getNpc(ko).setVehicle(null);
            });
        }
        return true;
    }
}
