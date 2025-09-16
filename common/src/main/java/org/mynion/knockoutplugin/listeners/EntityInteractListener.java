package org.mynion.knockoutplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;
import org.mynion.knockoutplugin.utils.NpcModel;

import java.util.Optional;

public class EntityInteractListener implements Listener {
    @EventHandler
    public void onInteractWithEntity(PlayerInteractAtEntityEvent e) {
        if (!(e.getRightClicked() instanceof ArmorStand)) {
            return;
        }

        Player interactor = e.getPlayer();

        if (interactor.isSneaking()) {

            NpcManager NpcManager = Knockout.getNpcManager();

            if(!Knockout.getPlugin().getConfig().getBoolean("click-to-carry-drop")){
                return;
            }

            if (!interactor.hasPermission("knockout.carry")) {
                MessageUtils.sendMessage(interactor, "no-permission-message");
                return;
            }

            // Check if the player is already carrying a knocked out player
            if (interactor.getPassengers().stream()
                    .filter(passenger -> passenger instanceof Player)
                    .map(passenger -> (Player) passenger)
                    .anyMatch(NpcManager::npcExists))
            {
                return;
            }

            // Find knocked out player
            Optional<Player> knockedOutPlayer = NpcManager.findNearbyKnockedOutPlayer(interactor);

            // Carry a knocked out player
            knockedOutPlayer.ifPresentOrElse(ko -> NpcManager.carryPlayer(ko, interactor), () -> MessageUtils.sendMessage(interactor, "invalid-carry-message"));
            return;
        }

        NpcManager npcManager = Knockout.getNpcManager();

        if(!npcManager.npcExists((ArmorStand) e.getRightClicked())){
            return;
        }

        NpcModel npc = npcManager.getNpc((ArmorStand) e.getRightClicked());
        Player clickedPlayer = npc.getPlayer();

        // Check if looting allowed
        if (!Knockout.getPlugin().getConfig().getBoolean("looting-allowed", false) || clickedPlayer == interactor) {
            return;
        }

        // If found, open knocked player's inventory
        // Can improve: allow taking armor from knocked player
        interactor.openInventory(clickedPlayer.getInventory());
        interactor.setMetadata("KnockoutLooting", new FixedMetadataValue(Knockout.getPlugin(), clickedPlayer.getName()));
    }


    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (e.getPlayer().hasMetadata("KnockoutLooting")) {
            e.getPlayer().removeMetadata("KnockoutLooting", Knockout.getPlugin());
        }
    }
}
