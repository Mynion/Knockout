package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
                ChatUtils.sendPlayerMessage(p, "not-allowed-message");
            }
        }
    }
}
