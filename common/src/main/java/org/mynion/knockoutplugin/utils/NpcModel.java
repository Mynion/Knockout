package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.GameMode;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

// NpcModel is a spigot api part of class Npc

public abstract class NpcModel {
    private final Player player;
    private final ArmorStand armorStand;
    private Player vehicle;
    private boolean isBeingRevived;
    private final GameMode previousGameMode;
    private int knockoutCooldown;
    private Entity killer;
    private boolean isVulnerableByPlayerWhenCarried;

    private DamageSource knockoutDamageSource;
    private DamageSource lastDamageSource;

    public NpcModel(Player player, ArmorStand armorStand, GameMode previousGameMode, @Nullable Entity killer) {
        this.player = player;
        this.armorStand = armorStand;
        this.previousGameMode = previousGameMode;
        isBeingRevived = false;
        this.killer = killer;
        isVulnerableByPlayerWhenCarried = true;
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
                Knockout.getNpcManager().endKnockout(player, true);
            } else {
                Knockout.getNpcManager().revivePlayer(player);
            }

        } else {
            String knockoutTitle = Knockout.getPlugin().getConfig().getString("knockout-title");

            if (knockoutTitle != null) {
                int seconds = knockoutCooldown;
                LocalTime time = LocalTime.ofSecondOfDay(seconds);
                String formattedTime;
                if (seconds < 60) {
                    formattedTime = time.format(DateTimeFormatter.ofPattern("s"));
                } else if (seconds < 3600) {
                    formattedTime = time.format(DateTimeFormatter.ofPattern("m:ss"));
                } else {
                    formattedTime = time.format(DateTimeFormatter.ofPattern("H:mm:ss"));
                }

                MessageUtils.sendTitle(player, "knockout-title", "knockout-subtitle", new HashMap<>(Map.of("%timer%", formattedTime)), new HashMap<>(Map.of("%timer%", formattedTime)), 1, 20 * 3600, 1);

            }
        }
    }

    public Entity getKiller() {
        return killer;
    }

    public void setKiller(Entity killer) {
        this.killer = killer;
    }

    public boolean isVulnerableByPlayerWhenCarried() {
        return isVulnerableByPlayerWhenCarried;
    }

    public void setVulnerableByPlayerWhenCarried(boolean vulnerableByPlayerWhenCarried) {
        isVulnerableByPlayerWhenCarried = vulnerableByPlayerWhenCarried;
    }

    public DamageSource getKnockoutDamageSource() {
        return knockoutDamageSource;
    }

    public void setKnockoutDamageSource(DamageSource knockoutDamageSource) {
        this.knockoutDamageSource = knockoutDamageSource;
    }

    public DamageSource getLastDamageSource() {
        return lastDamageSource;
    }

    public void setLastDamageSource(DamageSource lastDamageSource) {
        this.lastDamageSource = lastDamageSource;
    }
}
