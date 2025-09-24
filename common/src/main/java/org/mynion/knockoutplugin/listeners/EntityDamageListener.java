package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.MessageUtils;
import org.mynion.knockoutplugin.utils.NpcManager;
import org.mynion.knockoutplugin.utils.NpcModel;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        if (e.getDamager() instanceof Player p) {
            if (NpcManager.npcExists(p)) {
                e.setCancelled(true);
                MessageUtils.sendMessage(p, "not-allowed-message");
            }
        }
        if (e.getEntity() instanceof ArmorStand armorStand) {

            if (!NpcManager.npcExists(armorStand)) {
                return;
            }

            NpcModel npc = NpcManager.getNpc(armorStand);

            // Cancel damage for the armor stand of knocked out player
            e.setCancelled(true);

            if (!Knockout.getPlugin().getConfig().getBoolean("knockout-vulnerable")) {
                return;
            }

            if (armorStand.getNoDamageTicks() > 0) return;

            Player p = npc.getPlayer();

            // Prevent hurting ko player by yourself
            if (p.equals(e.getDamager())) return;

            if (NpcManager.useDamageSource()) {
                if (NpcManager.wouldDie(p, e)) {
                    NpcManager.damagePlayerWithDamageSource(npc, e.getDamageSource(), e.getFinalDamage());
                } else {
                    NpcManager.damagePlayerWithoutKB(npc, e.getDamager(), e.getFinalDamage());
                }
            } else {
                NpcManager.damagePlayerWithoutKB(npc, e.getDamager(), e.getFinalDamage());
            }

            armorStand.setNoDamageTicks(10);

        }
    }
}
