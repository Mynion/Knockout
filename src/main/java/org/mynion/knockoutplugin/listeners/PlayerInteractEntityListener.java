package org.mynion.knockoutplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerInteractEntityListener implements Listener {
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
