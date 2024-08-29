package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onEntityInteract(InventoryClickEvent e) {
        if (NpcManager.npcExists((Player) e.getWhoClicked())) {
            e.setCancelled(true);
        }
    }
}
