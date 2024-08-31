package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PickupItemListener implements Listener {
    @EventHandler
    public void onPickupItem(EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
            }
        }
    }
}
