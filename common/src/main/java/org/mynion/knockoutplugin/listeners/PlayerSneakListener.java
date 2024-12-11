package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class PlayerSneakListener implements Listener {
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();

        if (NpcManager.npcExists(p)) {
            e.setCancelled(true);

        } else {

            // Find nearby KO player
            Optional<Player> knockedOutPlayer = findNearbyKnockedOutPlayer(p);

            // Start reviving if present
            knockedOutPlayer.ifPresent(ko -> NpcManager.startReviving(p, ko));
        }
    }

    private Optional<Player> findNearbyKnockedOutPlayer(Player p) {
        return p.getNearbyEntities(1, 1, 1).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(NpcManager::npcExists)
                .findFirst();
    }
}
