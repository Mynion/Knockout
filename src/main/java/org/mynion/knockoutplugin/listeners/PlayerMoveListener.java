package org.mynion.knockoutplugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (NpcManager.npcExists(p) && p.isInWater()) {
            Location from = e.getFrom();
            Location to = new Location(p.getWorld(), from.getX(), from.getY(), from.getZ(), e.getTo().getYaw(), e.getTo().getPitch());
            if (to.clone().subtract(0, 0.1, 0).getBlock().isPassable()) {
                to.subtract(0, 0.1, 0);
            }
            e.setTo(to);
        }
    }
}
