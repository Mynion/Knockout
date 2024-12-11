//
//package org.mynion.knockoutplugin.utils;
//
////import net.minecraft.nbt.CompoundTag;
////import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
////import net.minecraft.server.level.ServerLevel;
////import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.Entity;
////import net.minecraft.world.entity.TamableAnimal;
//import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
//import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
//import org.bukkit.entity.Player;
//import org.mynion.knockoutplugin.utils.VersionAdapter;
//
//
//public class v1_21_R1 implements VersionAdapter {
//    public v1_21_R1() {
//    }
//
//    @Override
//    public ServerPlayer getServerPlayer(Player p) {
//        return ((CraftPlayer) p).getHandle();
//    }
//    @Override
//    public Entity getArmorStand(org.bukkit.entity.Entity e) {
//        return ((CraftEntity) e).getHandle();
//    }
//    //@Override
//    //public ClientboundTeleportEntityPacket getTeleportPacket(ServerPlayer deadBody, ServerPlayer sp, double yDiff) {
//    //    return new ClientboundTeleportEntityPacket(deadBody);
//    //}
//
//    @Override
//    public void handleParrotsOnShoulders(Player p) {
//        /*
//        ServerPlayer sp = getServerPlayer(p);
//        new BukkitRunnable() {
//            @Override
//            public void run() {
//                if (!NpcManager.npcExists(p)) this.cancel();
//
//                if (!sp.getShoulderEntityLeft().isEmpty()) {
//                    net.minecraft.world.entity.EntityType.create(sp.getShoulderEntityLeft(), sp.level()).map((entity) -> {
//                        if (entity instanceof TamableAnimal) {
//                            ((TamableAnimal) entity).setOwnerUUID(p.getUniqueId());
//                        }
//                        entity.setPos(sp.getX(), sp.getY() + 0.699999988079071, sp.getZ());
//                        return ((ServerLevel) sp.level()).addWithUUID(entity, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY);
//                    });
//                    sp.setShoulderEntityLeft(new CompoundTag());
//                }
//
//                if (!sp.getShoulderEntityRight().isEmpty()) {
//                    net.minecraft.world.entity.EntityType.create(sp.getShoulderEntityRight(), sp.level()).map((entity) -> {
//                        if (entity instanceof TamableAnimal) {
//                            ((TamableAnimal) entity).setOwnerUUID(p.getUniqueId());
//                        }
//                        entity.setPos(sp.getX(), sp.getY() + 0.699999988079071, sp.getZ());
//                        return ((ServerLevel) sp.level()).addWithUUID(entity, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY);
//                    });
//                    sp.setShoulderEntityRight(new CompoundTag());
//                }
//            }
//        }.runTaskTimer(Knockout.getPlugin(), 0, 2);
//
//         */
//    }
//
//}
