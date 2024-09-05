package org.mynion.knockoutplugin.utils;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

public class Npc {
    private final Player player;
    private final ServerPlayer deadBody;
    private final ArmorStand armorStand;
    private Player vehicle;
    private boolean isBeingRevived;

    public Npc(Player player, ServerPlayer deadBody, ArmorStand armorStand) {
        this.player = player;
        this.deadBody = deadBody;
        this.armorStand = armorStand;
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
}
