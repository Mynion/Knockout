package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class InventoryClickListener implements Listener {
    @EventHandler
    public void onEntityInteract(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        if (NpcManager.npcExists(p)) {
            e.setCancelled(true);
            ChatUtils.sendMessage(p, "not-allowed-message");
        }
    }
}
