package org.mynion.knockoutplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PickupArrowListener implements Listener {
    @EventHandler
    public void onPickupArrow(PlayerPickupArrowEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
        }

    }
}
