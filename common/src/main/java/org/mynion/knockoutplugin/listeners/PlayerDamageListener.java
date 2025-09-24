package org.mynion.knockoutplugin.listeners;

import org.bukkit.damage.DamageSource;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
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
            if (NpcManager.wouldDie(p, e)) {
                // Check if the player is knocked out
                if (NpcManager.npcExists(p)) {

                    if (NpcManager.useDamageSource()) {
                        if (!Knockout.getPlugin().getConfig().getBoolean("remember-death-cause")) {
                            NpcManager.endKnockout(p, false);
                            return;
                        }

                        DamageSource damageSource;
                        if (Knockout.getPlugin().getConfig().getBoolean("remember-death-cause")) {
                            damageSource = NpcManager.getNpc(p).getKnockoutDamageSource();
                        } else {
                            damageSource = NpcManager.getNpc(p).getLastDamageSource();
                        }

                        if (damageSource.getDamageType() != e.getDamageSource().getDamageType() || damageSource.getCausingEntity() != e.getDamageSource().getCausingEntity()) {
                            // Replace normal damage event with attacker damage event
                            e.setCancelled(true);

                            // Damage by attacker
                            NpcManager.getNpc(p).setVulnerableByPlayerWhenCarried(true);
                            if (Knockout.getPlugin().getServer().getName().equals("Purpur")) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        p.damage(e.getFinalDamage(), damageSource);
                                    }
                                }.runTaskLater(Knockout.getPlugin(), 0);
                            } else {
                                p.damage(e.getFinalDamage(), damageSource);
                            }
                        } else {
                            // End knockout
                            NpcManager.endKnockout(p, false);
                        }
                    } else {
                        // Replace normal damage event with attacker damage event
                        if (!(e instanceof EntityDamageByEntityEvent ebyEntity && ebyEntity.getDamager().equals(NpcManager.getKiller(p)))) {
                            if (NpcManager.getKiller(p) != null) {
                                e.setCancelled(true);

                                // Damage by attacker
                                NpcManager.getNpc(p).setVulnerableByPlayerWhenCarried(true);
                                Entity killer = NpcManager.getKiller(p);
                                if (Knockout.getPlugin().getServer().getName().equals("Purpur")) {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            p.damage(e.getFinalDamage(), killer);
                                        }
                                    }.runTaskLater(Knockout.getPlugin(), 0);
                                } else {
                                    p.damage(e.getFinalDamage(), killer);
                                }

                                // p.damage() calls EntityDamageEvent, so we return to prevent ending knockout two times
                                return;
                            }
                        }

                        // End knockout
                        NpcManager.endKnockout(p, false);
                    }
                } else {

                    // Check world white/black list
                    if (Knockout.getPlugin().getConfig().getList("world-blacklist").contains(p.getWorld().getName()))
                        return;
                    if (Knockout.getPlugin().getConfig().getBoolean("enable-world-whitelist")) {
                        if (!Knockout.getPlugin().getConfig().getList("world-whitelist").contains(p.getWorld().getName()))
                            return;
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
                    if (NpcManager.useDamageSource()) NpcManager.getNpc(p).setKnockoutDamageSource(e.getDamageSource());
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
}
