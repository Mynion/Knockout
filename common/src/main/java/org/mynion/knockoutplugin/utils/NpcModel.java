package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.mynion.knockoutplugin.Knockout;

// NpcModel is a spigot api part of class Npc

public abstract class NpcModel {
    private final Player player;
    private final ArmorStand armorStand;
    private Player vehicle;
    private boolean isBeingRevived;
    private final GameMode previousGameMode;
    private int knockoutCooldown;
    private EntityDamageEvent.DamageCause koCause;
    private Entity damager;

    public NpcModel(Player player, ArmorStand armorStand, GameMode previousGameMode, @Nullable EntityDamageEvent.DamageCause koCause, @Nullable Entity damager) {
        this.player = player;
        this.armorStand = armorStand;
        this.previousGameMode = previousGameMode;
        isBeingRevived = false;
        this.damager = damager;
        this.koCause = koCause;
    }

    public Player getPlayer() {
        return player;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Player getVehicle() {
        return vehicle;
    }

    public void setVehicle(Player vehicle) {
        this.vehicle = vehicle;
    }

    public boolean isBeingRevived() {
        return isBeingRevived;
    }

    public void setBeingRevived(boolean beingRevived) {
        isBeingRevived = beingRevived;
    }

    public GameMode getPreviousGameMode() {
        return previousGameMode;
    }

    public int getKnockoutCooldown() {
        return knockoutCooldown;
    }

    public void setKnockoutCooldown(int knockoutCooldown) {
        this.knockoutCooldown = knockoutCooldown;
        if (knockoutCooldown <= 0) {
            if (Knockout.getPlugin().getConfig().getBoolean("death-on-end")) {
                Knockout.getNpcManager().forceKill(player);
            } else {
                Knockout.getNpcManager().resetKnockout(player);
            }
        } else {
            String knockoutTitle = Knockout.getPlugin().getConfig().getString("knockout-title");
            if (knockoutTitle != null) {
                player.sendTitle(ChatColor.translateAlternateColorCodes('&', knockoutTitle), Integer.toString(getKnockoutCooldown()), 1, 20 * 3600, 1);
            }
        }
    }

    public EntityDamageEvent.DamageCause getKoCause() {
        return koCause;
    }

    public void setKoCause(EntityDamageEvent.DamageCause koCause) {
        this.koCause = koCause;
    }

    public Entity getDamager() {
        return damager;
    }

    public void setDamager(Entity damager) {
        this.damager = damager;
    }
}
