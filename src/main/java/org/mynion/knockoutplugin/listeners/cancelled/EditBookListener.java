package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class EditBookListener implements Listener {
    @EventHandler
    public void onEditBook(PlayerEditBookEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
