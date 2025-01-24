package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PotionEffectListener implements Listener {
    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (e.getEntity() instanceof Player p) {
            if (NpcManager.npcExists(p) && !e.getCause().equals(EntityPotionEffectEvent.Cause.PLUGIN)) {
                e.setCancelled(true);
            }
        }
    }
}
