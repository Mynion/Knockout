package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerDeathListener implements Listener {

    // Reset knockout on player death caused for any reason, for example by different plugin
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player p = e.getEntity();

        // Check if the player is knocked out
        if (NpcManager.npcExists(p)) {

            // Reset knockout
            NpcManager.resetKnockout(NpcManager.getNpc(p));
        }
    }
}
