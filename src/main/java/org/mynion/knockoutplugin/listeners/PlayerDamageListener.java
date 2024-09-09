package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerDamageListener implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {

            // Check if the player would die
            if (e.getFinalDamage() >= p.getHealth() && !hasDamageCooldown(p)) {

                // Check if the player is knocked out
                if (NpcManager.npcExists(p)) {

                    // Reset knockout
                    NpcManager.resetKnockout(NpcManager.getNpc(p));
                } else {

                    // Knockout player
                    e.setCancelled(true);
                    NpcManager.knockoutPlayer(p);
                }
            } else if (KnockoutPlugin.getPlugin().getConfig().getBoolean("drop-when-hit")) {
                // Drop knocked out player when hit
                p.getPassengers().stream()
                        .filter(passenger -> passenger instanceof Player)
                        .map(passenger -> (Player) passenger)
                        .filter(NpcManager::npcExists)
                        .forEach(knockedOutPlayer -> NpcManager.stopCarrying(knockedOutPlayer, p));
            }
        }
    }

    // Check if the player has damage cooldown to prevent false event calls
    private boolean hasDamageCooldown(Player p) {
        return p.getNoDamageTicks() > 10;
    }
}
