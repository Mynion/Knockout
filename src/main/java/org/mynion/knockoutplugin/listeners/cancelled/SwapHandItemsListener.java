package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class SwapHandItemsListener implements Listener {
    @EventHandler
    public void onSwapHandItems(PlayerSwapHandItemsEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
            ChatUtils.sendPlayerMessage(e.getPlayer(), "not-allowed-message");

        }
    }
}
