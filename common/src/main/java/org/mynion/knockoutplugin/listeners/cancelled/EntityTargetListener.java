package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;
public class EntityTargetListener implements Listener {
    @EventHandler
    public void onPlayerTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player p) {
            NpcManager NpcManager = Knockout.getNpcManager();
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
            }
        }
    }
}
