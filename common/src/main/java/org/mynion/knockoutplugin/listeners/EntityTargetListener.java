package org.mynion.knockoutplugin.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.List;
import java.util.Optional;

public class EntityTargetListener implements Listener {
    @EventHandler
    public void onPlayerTarget(EntityTargetEvent e) {
        if (e.getTarget() instanceof Player p) {
            NpcManager NpcManager = Knockout.getNpcManager();
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
                Optional<Player> newTarget = p.getNearbyEntities(10, 5, 10).stream()
                        .filter(entity -> entity instanceof Player)
                        .map(entity -> (Player) entity)
                        .filter(player -> List.of(GameMode.ADVENTURE, GameMode.SURVIVAL).contains(player.getGameMode()))
                        .findFirst();
                newTarget.ifPresent(e::setTarget);
            }
        }
    }
}
