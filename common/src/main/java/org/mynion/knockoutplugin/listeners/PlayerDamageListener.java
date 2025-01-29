package org.mynion.knockoutplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerDamageListener implements Listener {
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (e.getEntity() instanceof Player p) {

            // Cancel damage for carried players
            if (e instanceof EntityDamageByEntityEvent ebyEntity) {
                if (NpcManager.npcExists(p) && ebyEntity.getDamager() instanceof Player && !NpcManager.getNpc(p).isVulnerableByPlayerWhenCarried()) {
                    e.setCancelled(true);
                }
            }
            

            // Check if the player would die
            if (e.getFinalDamage() >= p.getHealth() && !hasDamageCooldown(p, e) && !hasTotemOfUndying(p)) {
                // Check if the player is knocked out
                if (NpcManager.npcExists(p)) {

                    // Replace normal damage event with attacker damage event
                    if (!(e instanceof EntityDamageByEntityEvent ebyEntity && ebyEntity.getDamager().equals(NpcManager.getDamager(p)))) {
                        if (NpcManager.getDamager(p) != null) {
                            e.setCancelled(true);

                            // Damage by attacker
                            NpcManager.getNpc(p).setVulnerableByPlayerWhenCarried(true);
                            p.damage(e.getFinalDamage(), NpcManager.getDamager(p));

                            // p.damage() calls EntityDamageEvent, so we return to prevent ending knockout two times
                            return;
                        }
                    }

                    // Reset knockout
                    NpcManager.resetKnockout(p);
                } else {

                    e.setCancelled(true);
                    Entity damager = null;
                    if (e instanceof EntityDamageByEntityEvent ebyEntity) {
                        damager = ebyEntity.getDamager();
                    }

                    // Knockout player
                    NpcManager.knockoutPlayer(p, damager);
                }
            } else if (Knockout.getPlugin().getConfig().getBoolean("drop-on-hit")) {
                p.sendMessage(String.valueOf(p.getNoDamageTicks()));

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
    private boolean hasDamageCooldown(Player p, EntityDamageEvent e) {
        EntityDamageEvent.DamageCause CAUSE = e.getCause();
        if (e instanceof EntityDamageByEntityEvent ebyEntity && p.getLastDamageCause() instanceof EntityDamageByEntityEvent lastEbyEntity) {
            if (!ebyEntity.getDamager().equals(lastEbyEntity.getDamager())) {
                return false;
            }
        }
        if (p.getLastDamageCause() != null) {
            if (p.getLastDamageCause().getCause() != CAUSE) {
                return false;
            }
        }
        return p.getNoDamageTicks() > 10;
    }

    // Check if the player has totem of undying in either of his hands
    private boolean hasTotemOfUndying(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.TOTEM_OF_UNDYING)
                || p.getInventory().getItemInOffHand().getType().equals(Material.TOTEM_OF_UNDYING);
    }
}
