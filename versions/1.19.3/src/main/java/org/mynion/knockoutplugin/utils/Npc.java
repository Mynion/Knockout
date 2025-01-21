package org.mynion.knockoutplugin.utils;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;

// Npc is a class that represents a knocked out player in the game
// It contains the player that is knocked out, the dead body of the player and the armor stand that displays text above the dead body

public class Npc {
    private final Player player;
    private final ServerPlayer deadBody;
    private final ArmorStand armorStand;
    private Player vehicle;
    private boolean isBeingRevived;
    private final GameMode previousGameMode;

    private int knockoutCooldown;

    public Npc(Player player, ServerPlayer deadBody, ArmorStand armorStand, GameMode previousGameMode) {
        this.player = player;
        this.deadBody = deadBody;
        this.armorStand = armorStand;
        this.previousGameMode = previousGameMode;
        isBeingRevived = false;
    }

    public Player getPlayer() {
        return player;
    }

    public ServerPlayer getDeadBody() {
        return deadBody;
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
}
