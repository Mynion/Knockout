package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerDamageListener implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (e.getFinalDamage() >= p.getHealth() && !hasDamageCooldown(p)) {
                if (NpcManager.npcExists(p)) {
                    NpcManager.resetKnockout(NpcManager.getNpc(p));
                } else {
                    e.setCancelled(true);
                    NpcManager.knockoutPlayer(p);
                }
            }
        }
    }

    private boolean hasDamageCooldown(Player p) {
        return p.getNoDamageTicks() > 10;
    }
}
