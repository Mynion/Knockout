package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class ExpChangeListener implements Listener {
    @EventHandler
    public void onExpChange(PlayerExpChangeEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setAmount(0);
        }
    }
}
