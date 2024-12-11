package org.mynion.knockoutplugin.utils;

import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.bukkit.entity.Player;

public interface VersionAdapter {
    ServerPlayer getServerPlayer(Player p);
    Entity getArmorStand(org.bukkit.entity.Entity e);
    //ClientboundTeleportEntityPacket getTeleportPacket(ServerPlayer deadBody, ServerPlayer sp, double yDiff);
    void handleParrotsOnShoulders(Player p);

}
