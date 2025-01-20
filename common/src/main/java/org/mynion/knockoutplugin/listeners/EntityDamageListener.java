package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.NpcManager;
public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (e.getDamager() instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
                ChatUtils.sendMessage(p, "not-allowed-message");
            }
        }
        if (e.getEntity() instanceof ArmorStand armorStand) {

            if (!NpcManager.npcExists(armorStand)) {
                return;
            }

            // Cancel damage for the armor stand of knocked out player
            e.setCancelled(true);

            if (!Knockout.getPlugin().getConfig().getBoolean("knockout-vulnerable")) {
                return;
            }

            if(armorStand.getNoDamageTicks() > 0) return;

            NpcManager.damageKOPlayer(armorStand, e.getDamager(), e.getFinalDamage());

            armorStand.setNoDamageTicks(10);

        }
    }
}
