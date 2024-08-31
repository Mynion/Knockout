package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (NpcManager.npcExists(p)) {
            Entity currentVehicle = NpcManager.getNpc(p).getVehicle();
            if (currentVehicle != null) {
                currentVehicle.addPassenger(p);
            }
            e.setCancelled(true);
        }
    }
}
