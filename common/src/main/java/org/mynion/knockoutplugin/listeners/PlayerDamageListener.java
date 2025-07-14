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
                    if (!(e instanceof EntityDamageByEntityEvent ebyEntity && ebyEntity.getDamager().equals(NpcManager.getKiller(p)))) {
                        if (NpcManager.getKiller(p) != null) {
                            e.setCancelled(true);

                            // Damage by attacker
                            NpcManager.getNpc(p).setVulnerableByPlayerWhenCarried(true);
                            Entity killer = NpcManager.getKiller(p);
                            p.damage(e.getFinalDamage(), killer);

                            // p.damage() calls EntityDamageEvent, so we return to prevent ending knockout two times
                            return;
                        }
                    }

                    // End knockout
                    NpcManager.endKnockout(p, false);
                } else {

                    // Check world white/black list
                    if(Knockout.getPlugin().getConfig().getList("world-blacklist").contains(p.getWorld().getName())) return;
                    if(Knockout.getPlugin().getConfig().getBoolean("enable-world-whitelist")){
                        if(!Knockout.getPlugin().getConfig().getList("world-whitelist").contains(p.getWorld().getName())) return;
                    }

                    e.setCancelled(true);
                    Entity damager = null;
                    if (e instanceof EntityDamageByEntityEvent ebyEntity) {
                        damager = ebyEntity.getDamager();
                    }

                    // Knockout player
                    int seconds = Knockout.getPlugin().getConfig().getInt("knockout-time");
                    if (seconds < 0) {
                        seconds = 60;
                    }
                    NpcManager.knockoutPlayer(p, damager, seconds);
                }
            } else {
                if (NpcManager.npcExists(p)) {
                    NpcManager.playDamageAnimation(NpcManager.getNpc(p));
                }
                if (Knockout.getPlugin().getConfig().getBoolean("drop-on-hit")) {
                    // Drop knocked out player when hit
                    p.getPassengers().stream()
                            .filter(passenger -> passenger instanceof Player)
                            .map(passenger -> (Player) passenger)
                            .filter(NpcManager::npcExists)
                            .forEach(knockedOutPlayer -> NpcManager.dropPlayer(knockedOutPlayer, p));
                }
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
