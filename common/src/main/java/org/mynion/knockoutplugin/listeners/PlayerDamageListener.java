package org.mynion.knockoutplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;
public class PlayerDamageListener implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (e.getEntity() instanceof Player p) {

            // Check if the player would die
            if (e.getFinalDamage() >= p.getHealth() && !hasDamageCooldown(p) && !hasTotemOfUndying(p)) {
                // Check if the player is knocked out
                if (NpcManager.npcExists(p)) {

                    // Reset knockout
                    NpcManager.resetKnockout(p);
                } else {

                    // Knockout player
                    e.setCancelled(true);
                    NpcManager.knockoutPlayer(p);
                }
            } else if (Knockout.getPlugin().getConfig().getBoolean("drop-on-hit")) {
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

    // Check if the player has totem of undying in either of his hands
    private boolean hasTotemOfUndying(Player p){
        return p.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)
                || p.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING);
    }
}
