package org.mynion.knockoutplugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerPortalListener implements Listener {
    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        Player p = e.getPlayer();
        p.getPassengers().stream()
                .filter(passenger -> passenger instanceof Player)
                .filter(player -> NpcManager.npcExists((Player) player))
                .forEach(ko -> {
                    if (e.getTo() != null) {
                        ko.teleport(e.getTo());
                        NpcManager.getNpc((Player) ko).getArmorStand().teleport(e.getTo());
                    }
                });
    }
}
