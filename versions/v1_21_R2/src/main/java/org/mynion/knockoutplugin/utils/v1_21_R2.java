//
//package org.mynion.knockoutplugin.utils;
//
////import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
////import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.PositionMoveRotation;
//import net.minecraft.world.entity.Relative;
////import net.minecraft.world.phys.Vec3;
//import org.bukkit.craftbukkit.v1_21_R2.entity.CraftEntity;
//import org.bukkit.craftbukkit.v1_21_R2.entity.CraftPlayer;
//import org.bukkit.entity.Player;
//import org.mynion.knockoutplugin.utils.VersionAdapter;
//
//public class v1_21_R2 implements VersionAdapter {
//    public v1_21_R2() {
//    }
//
//    @Override
//    public ServerPlayer getServerPlayer(Player p) {
//        return ((CraftPlayer) p).getHandle();
//    }
//
//    @Override
//    public Entity getArmorStand(org.bukkit.entity.Entity e) {
//        return ((CraftEntity) e).getHandle();
//    }
//
//    //@Override
//    //public ClientboundTeleportEntityPacket getTeleportPacket(ServerPlayer deadBody, ServerPlayer sp, double yDiff) {
//    //    return new ClientboundTeleportEntityPacket(deadBody.getId(), new PositionMoveRotation(new Vec3(sp.getX() - deadBody.getX(), sp.getY() + yDiff - deadBody.getY(), sp.getZ() - deadBody.getZ()), new Vec3(0, 0, 0), 0, 0), Relative.ALL, deadBody.onGround());
//    //}
//
//    @Override
//    public void handleParrotsOnShoulders(Player p) {
//        //TODO
//    }
//}
