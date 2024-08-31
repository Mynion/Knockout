package org.mynion.knockoutplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerSneakListener implements Listener {
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
        }
    }
}
