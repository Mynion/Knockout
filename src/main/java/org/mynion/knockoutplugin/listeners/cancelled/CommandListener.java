package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

public class CommandListener implements Listener {
    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            if (!e.getMessage().startsWith("/die")) {
                e.setCancelled(true);
                String message = KnockoutPlugin.getPlugin().getConfig().getString("not-allowed-message");
                if (message != null) {
                    e.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                }
            }
        }
    }
}
