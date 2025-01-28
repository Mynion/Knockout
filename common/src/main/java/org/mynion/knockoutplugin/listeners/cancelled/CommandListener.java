package org.mynion.knockoutplugin.listeners.cancelled;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.List;

public class CommandListener implements Listener {
    @EventHandler
    public void onCommandSend(PlayerCommandPreprocessEvent e) {

        Player p =e.getPlayer();
        NpcManager NpcManager = Knockout.getNpcManager();

        if (!NpcManager.npcExists(p)) {
            return;
        }

        if(p.hasPermission("knockout.command")) {
            return;
        }

        if (e.getMessage().startsWith("/die")) {
            return;
        }

        List<String> alliases = Knockout.getPlugin().getConfig().getStringList("aliases.die");
        for (String alias : alliases) {
            if (e.getMessage().startsWith("/" + alias)) {
                return;
            }
        }

        // Cancel the command
        e.setCancelled(true);
        MessageUtils.sendMessage(p, "not-allowed-message");
    }
}
