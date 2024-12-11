package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class CommandListener implements Listener {
    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent e) {

        Player p =e.getPlayer();

        if (!NpcManager.npcExists(p)) {
            return;
        }

        if(p.hasPermission("knockout.command")) {
            return;
        }

        if (e.getMessage().startsWith("/die")) {
            return;
        }

        // Cancel the command
        e.setCancelled(true);
        ChatUtils.sendMessage(p, "not-allowed-message");
    }
}
