package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();

        // Kill the player if knocked out
        if (NpcManager.npcExists(p)) {
            NpcManager.forceKill(p);
        }

    }
}
