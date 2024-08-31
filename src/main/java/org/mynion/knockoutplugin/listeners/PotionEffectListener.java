package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PotionEffectListener implements Listener {
    @EventHandler
    public void onPotionEffect(EntityPotionEffectEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                if (!e.getCause().equals(EntityPotionEffectEvent.Cause.PLUGIN)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}
