package org.mynion.knockoutplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;
public class DieCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {

            NpcManager NpcManager = Knockout.getNpcManager();

            if (!p.hasPermission("knockout.die")) {
                ChatUtils.sendMessage(p, "no-permission-message");
                return true;
            }

            if (NpcManager.npcExists(p)) {
                NpcManager.forceKill(p);
            } else {
                ChatUtils.sendMessage(p, "invalid-die-message");
            }
        }
        return true;
    }
}
