package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;
public class RegainHealthListener implements Listener {
    @EventHandler
    public void onRegainHealth(EntityRegainHealthEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (e.getEntity() instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
            }
        }
    }
}
