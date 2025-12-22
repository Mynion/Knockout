package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

public class GameModeListener implements Listener {
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
            MessageUtils.sendMessage(e.getPlayer(), "not-allowed-message");
        }
    }
}
