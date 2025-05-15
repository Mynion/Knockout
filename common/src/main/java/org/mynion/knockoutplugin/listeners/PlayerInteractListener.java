package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Optional;

public class PlayerInteractListener implements Listener {
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        Player interactor = e.getPlayer();
        NpcManager NpcManager = Knockout.getNpcManager();
        if ((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.LEFT_CLICK_AIR) && e.getPlayer().isSneaking()) {

            if(!Knockout.getPlugin().getConfig().getBoolean("click-to-carry-drop")){
                return;
            }

            // Get knocked out passenger
            Optional<Player> knockedOutPlayer = interactor.getPassengers().stream()
                    .filter(passenger -> passenger instanceof Player)
                    .map(passenger -> (Player) passenger)
                    .filter(NpcManager::npcExists)
                    .findFirst();

            if (!interactor.hasPermission("knockout.drop")) {
                // Will fix this later, it spams the message even when not carrying anyone
                //MessageUtils.sendMessage(interactor, "no-permission-message");
                return;
            }


            // Stop carrying a player
            knockedOutPlayer.ifPresent(ko -> NpcManager.dropPlayer(ko, interactor));
            return;
        }

        if (NpcManager.npcExists(e.getPlayer())) {
            e.setCancelled(true);
            MessageUtils.sendMessage(e.getPlayer(), "not-allowed-message");
        }
    }
}
