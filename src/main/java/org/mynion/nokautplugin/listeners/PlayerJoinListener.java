package org.mynion.nokautplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mynion.nokautplugin.utils.NpcManager;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        NpcManager.resetNokautEffects(e.getPlayer());
    }
}
