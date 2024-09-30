package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class DropItemListener implements Listener {
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
            ChatUtils.sendMessage(e.getPlayer(), "not-allowed-message");
        }
    }
}
