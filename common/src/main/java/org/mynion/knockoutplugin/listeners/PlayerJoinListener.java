package org.mynion.knockoutplugin.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;
import org.mynion.knockoutplugin.utils.NpcModel;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        NpcManager NpcManager = Knockout.getNpcManager();
        NpcManager.endKnockoutEffects(p);

        if (Knockout.getPlugin().getConfig().getBoolean("end-knockout-on-quit")) return;

        // Restart knockout if a player left the server and joined again
        if (NpcManager.npcExists(p.getUniqueId())) {
            NpcModel npc = NpcManager.getNpc(p.getUniqueId());

            Entity killer = npc.getKiller();
            int timer = npc.getKnockoutCooldown();
            double health = npc.getPlayer().getHealth();
            boolean isNewNpcDead = npc.isDead();
            NpcManager.getNPCs().remove(npc);

            // Delay new knockout to fix hologram issues
            new BukkitRunnable() {

                @Override
                public void run() {
                    int newTimer = timer;
                    if (newTimer <= 0) newTimer = 5;
                    NpcManager.knockoutPlayer(p, killer, newTimer);
                    p.setHealth(health);
                    if (isNewNpcDead) {
                        NpcManager.endKnockout(p, Knockout.getPlugin().getConfig().getBoolean("death-on-end"));
                    }
                }
            }.runTaskLater(Knockout.getPlugin(), 0);
        }

        NpcManager.refreshNPCsForPlayer(p);

    }
}
