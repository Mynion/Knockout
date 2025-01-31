package org.mynion.knockoutplugin.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import jline.internal.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.enums.PacketType;
import org.mynion.knockoutplugin.enums.PotionType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VersionController_v1_20_6 implements VersionController {
    @Override
    public void setMaxHealth(Player p) {
        AttributeInstance maxHealthAttribute = getServerPlayer(p).getAttribute(Attributes.MAX_HEALTH);
        double maxHealth = maxHealthAttribute.getValue();
        p.setHealth(maxHealth);
    }

    @Override
    public void setXpDelay(Player p, int delay) {
        p.setExpCooldown(delay);
    }

    @Override
    public void setCollisions(Player p, boolean on) {
        ServerPlayer sp = getServerPlayer(p);
        PlayerTeam team = new PlayerTeam(new Scoreboard(), "mannequin");
        if (on) {
            if (sp.getTeam() != null) {
                team.setCollisionRule(sp.getTeam().getCollisionRule());
            } else {
                team.setCollisionRule(Team.CollisionRule.ALWAYS);
            }
        } else {
            team.setCollisionRule(Team.CollisionRule.NEVER);
        }
        team.getPlayers().add(p.getName());

        ClientboundSetPlayerTeamPacket packet = ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true);
        MinecraftServer server = MinecraftServer.getServer();
        List<ServerPlayer> onlinePlayers = server.getPlayerList().players;
        onlinePlayers.forEach(player -> player.connection.send(packet));
    }

    @Override
    public void teleportMannequin(NpcModel npc, double x, double y, double z) {
        ServerPlayer mannequin = ((Npc) npc).getMannequin();
        mannequin.teleportTo(x, y, z);
    }

    @Override
    public void teleportHologram(NpcModel npc, double x, double y, double z) {
        CraftEntity craftArmorStand = (CraftEntity) npc.getArmorStand();
        net.minecraft.world.entity.Entity armorStand = craftArmorStand.getHandle();
        armorStand.teleportTo(x, y, z);
    }

    @Override
    public NpcModel createNpc(Player player, ArmorStand armorStand, GameMode previousGameMode, @Nullable Entity damager) {
        ServerPlayer mannequin = createMannequin(player);
        return new Npc(player, mannequin, armorStand, previousGameMode, damager);
    }

    @Override
    public void addPotionEffect(LivingEntity p, @NotNull PotionType type, int duration, int amplifier, boolean ambient, boolean particles) {
        p.addPotionEffect(new PotionEffect(getPotionEffectType(type), duration, amplifier, ambient, particles));
    }

    @Override
    public void removePotionEffect(LivingEntity p, @NotNull PotionType type) {
        p.removePotionEffect(getPotionEffectType(type));
    }

    @Override
    public int getPotionAmplifier(LivingEntity p, @NotNull PotionType type) {
        return p.getPotionEffect(getPotionEffectType(type)).getAmplifier();
    }

    @Override
    public void sendPacket(Player p, NpcModel npcmodel, PacketType packetType) {
        ServerPlayer receiver = getServerPlayer(p);
        Packet<?> packet = createPacket((Npc) npcmodel, packetType);
        receiver.connection.send(packet);
    }

    @Override
    public void setAbleToJump(Player p, boolean able) {
        ServerPlayer sp = getServerPlayer(p);
        if (able) {
            AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
            jumpAttribute.setBaseValue(0.42);
        } else {
            AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
            jumpAttribute.setBaseValue(0);
        }
    }

    @Override
    public void removeParrotFromShoulder(Player p, boolean rightShoulder) {
        ServerPlayer sp = getServerPlayer(p);

        CompoundTag parrot;
        if (rightShoulder) {
            parrot = sp.getShoulderEntityRight();
        } else {
            parrot = sp.getShoulderEntityLeft();
        }

        if (parrot.isEmpty()) return;

        Optional<net.minecraft.world.entity.Entity> left = EntityType.create(parrot, sp.level());

        if (left.isEmpty()) return;

        net.minecraft.world.entity.Entity entity = left.get();
        if (entity instanceof TamableAnimal tamableAnimal) {
            tamableAnimal.setOwnerUUID(p.getUniqueId());
        }
        entity.setPos(sp.getX(), sp.getY() + 0.699999988079071, sp.getZ());
        ((ServerLevel) sp.level()).addWithUUID(entity, CreatureSpawnEvent.SpawnReason.SHOULDER_ENTITY);

        if (rightShoulder) {
            sp.setShoulderEntityRight(new CompoundTag());
        } else {
            sp.setShoulderEntityLeft(new CompoundTag());
        }
    }

    @Override
    public void broadcastPacket(NpcModel npc, PacketType packetType) {
        Packet<?> packet = createPacket((Npc) npc, packetType);
        MinecraftServer server = MinecraftServer.getServer();
        List<ServerPlayer> onlinePlayers = server.getPlayerList().players;
        onlinePlayers.forEach(p -> p.connection.send(packet));
    }

    private Packet<?> createPacket(Npc npc, PacketType packetType) {
        ServerPlayer sp = getServerPlayer(npc.getPlayer());
        ServerPlayer mannequin = npc.getMannequin();
        return switch (packetType) {
            case ANIMATE -> new ClientboundHurtAnimationPacket(mannequin.getId(), 0);
            case ADD_ENTITY -> new ClientboundAddEntityPacket(mannequin);
            case INFO_UPDATE ->
                    new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, mannequin);
            case SET_ENTITY_DATA ->
                    new ClientboundSetEntityDataPacket(mannequin.getId(), mannequin.getEntityData().getNonDefaultValues());
            case SET_EQUIPMENT -> new ClientboundSetEquipmentPacket(mannequin.getId(), getItems(sp));
            case INFO_REMOVE -> new ClientboundPlayerInfoRemovePacket(List.of(mannequin.getGameProfile().getId()));
            case REMOVE_ENTITY -> new ClientboundRemoveEntitiesPacket(mannequin.getId());
            case TELEPORT -> new ClientboundTeleportEntityPacket(mannequin);
        };
    }

    private List<Pair<EquipmentSlot, ItemStack>> getItems(ServerPlayer sp) {
        return List.of(
                Pair.of(EquipmentSlot.HEAD, sp.getItemBySlot(EquipmentSlot.HEAD)),
                Pair.of(EquipmentSlot.CHEST, sp.getItemBySlot(EquipmentSlot.CHEST)),
                Pair.of(EquipmentSlot.LEGS, sp.getItemBySlot(EquipmentSlot.LEGS)),
                Pair.of(EquipmentSlot.FEET, sp.getItemBySlot(EquipmentSlot.FEET)),
                Pair.of(EquipmentSlot.MAINHAND, sp.getItemBySlot(EquipmentSlot.MAINHAND)),
                Pair.of(EquipmentSlot.OFFHAND, sp.getItemBySlot(EquipmentSlot.OFFHAND))
        );
    }

    private PotionEffectType getPotionEffectType(PotionType potionType) {
        if (potionType == PotionType.JUMP_BOOST) return PotionEffectType.JUMP_BOOST;
        if (potionType == PotionType.SLOWNESS) return PotionEffectType.SLOWNESS;
        return null;
    }

    private ServerPlayer createMannequin(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();
        MinecraftServer server = sp.getServer();
        ServerLevel level = sp.serverLevel();

        UUID mannequinUUID = UUID.randomUUID();
        String mannequinName = p.getName();
        GameProfile mannequinProfile = new GameProfile(mannequinUUID, mannequinName);

        ServerPlayer mannequin = new ServerPlayer(server, level, mannequinProfile, new ClientInformation("en_us", 10, ChatVisiblity.FULL, true, sp.clientInformation().modelCustomisation(), net.minecraft.world.entity.player.Player.DEFAULT_MAIN_HAND, false, false));

        mannequin.setPos(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
        mannequin.setXRot(sp.getXRot());
        mannequin.setYRot(sp.getYRot());
        mannequin.setYHeadRot(sp.getYHeadRot());
        mannequin.setPose(Pose.SWIMMING);
        mannequin.setUUID(mannequinUUID);
        mannequin.setGameMode(GameType.SURVIVAL);

        // Set mannequin skin
        try {
            Property skin = (Property) sp.getGameProfile().getProperties().get("textures").toArray()[0];
            String textures = skin.value();
            String signature = skin.signature();
            mannequin.getGameProfile().getProperties().put("textures", new Property("textures", textures, signature));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        // Create mannequin server connection
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.CLIENTBOUND), mannequin, CommonListenerCookie.createInitial(mannequin.getGameProfile(), false));

        //TODO
        // Set mannequin model customization
        mannequin.restoreFrom(sp, false);
        mannequin.setShoulderEntityLeft(new CompoundTag());
        mannequin.setShoulderEntityRight(new CompoundTag());
        mannequin.setGameMode(GameType.SURVIVAL);

        return mannequin;
    }

    private ServerPlayer getServerPlayer(Player p) {
        return ((CraftPlayer) p).getHandle();
    }
}
