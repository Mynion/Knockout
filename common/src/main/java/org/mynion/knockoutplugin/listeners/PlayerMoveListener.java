package org.mynion.knockoutplugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerMoveListener implements Listener {
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        NpcManager NpcManager = Knockout.getNpcManager();

        // Prevent a KO player from moving in water
        if (NpcManager.npcExists(p) && p.isInWater()) {
            Location from = e.getFrom();
            Location to = e.getTo();
            Location newTo = createNewLocation(from, to);

            if (isNotAtBottom(newTo)) {

                // Drown the player if not at the bottom
                newTo.subtract(0, 0.1, 0);
            }

            e.setTo(newTo);
        }
    }

    private Location createNewLocation(Location from, Location to) {
        return new Location(from.getWorld(), from.getX(), from.getY(), from.getZ(), to.getYaw(), to.getPitch());
    }

    private boolean isNotAtBottom(Location location) {
        return location.clone().subtract(0, 0.1, 0).getBlock().isPassable();
    }
}
