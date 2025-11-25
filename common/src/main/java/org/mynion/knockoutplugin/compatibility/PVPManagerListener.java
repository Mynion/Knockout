package org.mynion.knockoutplugin.compatibility;

import me.chancesd.pvpmanager.event.PlayerUntagEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PVPManagerListener implements Listener {
    @EventHandler
    public void onCombatEnd(PlayerUntagEvent e) {
        NpcManager npcManager = Knockout.getNpcManager();
        Player p = e.getPlayer();
        if (npcManager.npcExists(p)) {
            npcManager.getVersionController().setCollisions(p, false);
        }
    }
}
