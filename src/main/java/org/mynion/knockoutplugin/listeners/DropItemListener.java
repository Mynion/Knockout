package org.mynion.knockoutplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class DropItemListener implements Listener {
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
