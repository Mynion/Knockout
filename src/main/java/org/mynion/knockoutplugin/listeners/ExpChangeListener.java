package org.mynion.knockoutplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class ExpChangeListener implements Listener {
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setAmount(0);
        }
    }
}
