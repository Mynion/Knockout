package org.mynion.knockoutplugin.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PotionSplashListener implements Listener {
    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        if(!Knockout.getPlugin().getConfig().getBoolean("revive-by-instant-health")) {
            return;
        }

        NpcManager npcManager = Knockout.getNpcManager();
        for(Entity entity : e.getAffectedEntities()) {
            if (entity instanceof Player player && npcManager.npcExists(player)) {
                for(PotionEffect effect : e.getPotion().getEffects()){
                    if (effect.getType().equals(PotionEffectType.INSTANT_HEALTH) && effect.getAmplifier() >= 1){
                        if(e.getPotion().getShooter() instanceof Player shooter){
                            npcManager.revivePlayer(player, shooter);
                        } else {
                            npcManager.revivePlayer(player);
                        }
                        return;
                    }
                }
            }
        }
    }
}
