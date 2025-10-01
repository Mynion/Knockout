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

        // End knockout or end temporary and knockout again when player rejoins
        if (NpcManager.npcExists(p)) {
            if(Knockout.getPlugin().getConfig().getBoolean("end-knockout-on-quit")){
                NpcManager.endKnockout(p, Knockout.getPlugin().getConfig().getBoolean("death-on-end"));
            } else {
                NpcManager.endKnockoutTemporary(p, false);
            }
        }

        if (e.getPlayer().hasMetadata("KnockoutLooting")) {
            e.getPlayer().removeMetadata("KnockoutLooting", Knockout.getPlugin());
        }

    }
}
