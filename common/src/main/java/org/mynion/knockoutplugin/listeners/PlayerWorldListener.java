package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerWorldListener implements Listener {
    @EventHandler
    public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
        Player p = e.getPlayer();
        NpcManager NpcManager = Knockout.getNpcManager();
        NpcManager.refreshNPCsForPlayer(p);
        if (NpcManager.npcExists(p)) {
            NpcManager.getNpc(p).getArmorStand().teleport(p.getLocation());
        }
    }
}
