package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerSneakListener implements Listener {
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (NpcManager.npcExists(p)) {
            e.setCancelled(true);
        }
    }
}
