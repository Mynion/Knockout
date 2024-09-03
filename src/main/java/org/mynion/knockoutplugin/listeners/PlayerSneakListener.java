package org.mynion.knockoutplugin.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.KnockoutPlugin;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.List;
import java.util.Optional;

public class PlayerSneakListener implements Listener {
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        if (NpcManager.npcExists(p)) {
            e.setCancelled(true);
        } else {
            // Find nearby KO player
            Optional<Player> knockedOutPlayer = p.getNearbyEntities(1, 1, 1).stream().filter(entity -> {
                if (entity instanceof Player p2) {
                    return NpcManager.npcExists(p2);
                }
                return false;
            }).findFirst().map(pl -> (Player) pl);

            // Start reviving if present
            knockedOutPlayer.ifPresent(ko -> NpcManager.startReviving(p, ko));
        }
    }
}
