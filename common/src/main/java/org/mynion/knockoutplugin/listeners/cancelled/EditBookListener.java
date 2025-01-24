package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class EditBookListener implements Listener {
    @EventHandler
    public void onEditBook(PlayerEditBookEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
            ChatUtils.sendMessage(e.getPlayer(), "not-allowed-message");
        }
    }
}
