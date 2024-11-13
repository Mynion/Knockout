package org.mynion.knockoutplugin.listeners;

import net.minecraft.network.protocol.game.ClientboundHurtAnimationPacket;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.ChatUtils;
import org.mynion.knockoutplugin.utils.Npc;
import org.mynion.knockoutplugin.utils.NpcManager;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
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

            if(!Knockout.getPlugin().getConfig().getBoolean("knockout-vulnerable")){
                return;
            }

            // Damage attacked knocked out player
            Npc npc = NpcManager.getNpc(armorStand);
            ClientboundHurtAnimationPacket packet = new ClientboundHurtAnimationPacket(npc.getDeadBody().getId(), 0);
            NpcManager.broadcastPacket(packet);
            npc.getPlayer().damage(e.getFinalDamage());

        }
    }
}
