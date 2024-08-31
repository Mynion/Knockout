package org.mynion.knockoutplugin.utils;

import net.minecraft.server.level.ServerPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Npc {
    private final Player player;
    private final ServerPlayer deadBody;
    private final ArmorStand armorStand;

    public Entity getVehicle() {
        return vehicle;
    }

    public void setVehicle(Entity vehicle) {
        this.vehicle = vehicle;
    }

    private Entity vehicle;

    public Npc(Player player, ServerPlayer deadBody, ArmorStand armorStand) {
        this.player = player;
        this.deadBody = deadBody;
        this.armorStand = armorStand;
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
}
