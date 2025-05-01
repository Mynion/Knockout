package org.mynion.knockoutplugin.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.Objects;
import java.util.Optional;

public class PlayerSneakListener implements Listener {
    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        NpcManager npcManager = Knockout.getNpcManager();

        if (npcManager.npcExists(player)) {
            e.setCancelled(true);
            return;
        }

        String reviveItemMaterial = Knockout.getPlugin().getConfig().getString("revive-item");
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();

        if (reviveItemMaterial != null && mainHandItem.getType().toString().contains(reviveItemMaterial)) {
            MessageUtils.sendMessage(player, "revive-item-missing");
            return;
        }

        // Find and revive nearby KO player
        npcManager.findNearbyKnockedOutPlayer(player).ifPresent(ko -> npcManager.startReviving(player, ko));
    }
}
