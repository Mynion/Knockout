package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerQuitListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        Player p = e.getPlayer();

        // Kill the player if knocked out
        if (NpcManager.npcExists(p)) {
            NpcManager.endKnockout(p, true);
        }

        if (e.getPlayer().hasMetadata("KnockoutLooting")) {
            e.getPlayer().removeMetadata("KnockoutLooting", Knockout.getPlugin());
        }

    }
}
