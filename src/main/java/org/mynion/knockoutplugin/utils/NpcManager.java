package org.mynion.knockoutplugin.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.KnockoutPlugin;

import java.util.*;

public class NpcManager {
    private static final List<Npc> NPCs = new ArrayList<>();

    public static void knockoutPlayer(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();
        MinecraftServer server = sp.getServer();
        ServerLevel level = sp.serverLevel();

        // Create dead body
        ServerPlayer deadBodyPlayer = createDeadBody(p);

        // Create dead body server connection
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.CLIENTBOUND), deadBodyPlayer, CommonListenerCookie.createInitial(deadBodyPlayer.getGameProfile(), false));

        // Broadcast dead body info packets
        ServerEntity deadBodyEntity = new ServerEntity(level, deadBodyPlayer, 20, false, null, null);
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(deadBodyPlayer, deadBodyEntity);
        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), List.of(deadBodyPlayer));
        ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(deadBodyPlayer.getId(), deadBodyPlayer.getEntityData().getNonDefaultValues());
        broadcastPacket(infoUpdatePacket);
        broadcastPacket(addEntityPacket);
        broadcastPacket(setEntityDataPacket);

        // Create hologram
        ArmorStand armorStand = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
        armorStand.setSmall(true);
        armorStand.setCustomName(ChatColor.RED + "Knockout");
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(true);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        // Create npc
        Npc npc = new Npc(p, deadBodyPlayer, armorStand);
        NPCs.add(npc);

        applyKnockoutEffects(p);
        setNoCollisions(npc);
        teleportBody(npc);

    }

    private static ServerPlayer createDeadBody(Player p) {
        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();
        MinecraftServer server = sp.getServer();
        ServerLevel level = sp.serverLevel();

        UUID deadBodyUUID = UUID.randomUUID();
        String deadBodyName = p.getDisplayName();
        GameProfile deadBodyProfile = new GameProfile(deadBodyUUID, deadBodyName);

        ServerPlayer deadBodyPlayer = new ServerPlayer(server, level, deadBodyProfile, new ClientInformation("en_us", 10, ChatVisiblity.FULL, true, sp.clientInformation().modelCustomisation(), net.minecraft.world.entity.player.Player.DEFAULT_MAIN_HAND, false, false));
        deadBodyPlayer.setPos(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
        deadBodyPlayer.setXRot(sp.getXRot());
        deadBodyPlayer.setYRot(sp.getYRot());
        deadBodyPlayer.setYHeadRot(sp.getYHeadRot());
        deadBodyPlayer.setShoulderEntityLeft(sp.getShoulderEntityLeft());
        deadBodyPlayer.setPose(Pose.SWIMMING);
        deadBodyPlayer.setUUID(deadBodyUUID);
        deadBodyPlayer.setGameMode(GameType.SURVIVAL);

        // Set dead body skin
        Property skin = (Property) sp.getGameProfile().getProperties().get("textures").toArray()[0];
        PropertyMap properties = sp.getGameProfile().getProperties();
        Set<String> keys = properties.keySet();
        keys.forEach(p::sendMessage);
        String textures = skin.value();
        String signature = skin.signature();
        deadBodyPlayer.getGameProfile().getProperties().put("textures", new Property("textures", textures, signature));

        return deadBodyPlayer;
    }

    public static void resetKnockout(Npc npc) {

        // Reset knockout effects
        resetKnockoutEffects(npc.getPlayer());

        // Remove dead body
        ClientboundPlayerInfoRemovePacket removeNpcPacket = new ClientboundPlayerInfoRemovePacket(List.of(npc.getDeadBody().getGameProfile().getId()));
        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(npc.getDeadBody().getId());
        broadcastPacket(removeNpcPacket);
        broadcastPacket(removeEntityPacket);

        // Remove hologram
        npc.getArmorStand().remove();

        // Remove npc from npc list
        NPCs.remove(npc);

    }

    public static void forceKill(Player p) {
        resetKnockout(getNpc(p));
        p.setHealth(0);
    }

    private static void applyKnockoutEffects(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();

        AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
        jumpAttribute.setBaseValue(0);

        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 999999999, 1, false, false);
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100, false, false);
        p.addPotionEffect(blindness);
        p.addPotionEffect(invisibility);
        p.removePotionEffect(PotionEffectType.POISON);

        p.setHealth(2);
        p.setWalkSpeed(0);
        p.setFlySpeed(0);
        p.setFireTicks(0);
        p.setFlying(false);
        p.setInvisible(true);
        p.setCollidable(false);
        p.setGameMode(GameMode.SURVIVAL);
        p.leaveVehicle();
        p.getPassengers().forEach(p::removePassenger);

        List<org.bukkit.entity.Entity> nearbyMobs = p.getNearbyEntities(50, 50, 50);
        nearbyMobs.forEach(mob -> {
            if (mob instanceof Mob m) {
                if (m.getTarget() instanceof Player targetPlayer) {
                    if (targetPlayer.getUniqueId().equals(p.getUniqueId())) {
                        m.setTarget(null);
                    }
                }
            }
        });

        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.hidePlayer(KnockoutPlugin.getPlugin(), p));

        startTimer(p);

    }

    private static void startTimer(Player p) {

        final int[] knockoutCooldown = {60};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (knockoutCooldown[0] > 0 && NpcManager.npcExists(p)) {
                    if (!p.isInsideVehicle()) {
                        p.sendTitle(ChatColor.RED + "Knockout", Integer.toString(knockoutCooldown[0]), 1, 20 * 60, 1);
                        knockoutCooldown[0]--;
                    }
                } else {
                    this.cancel();
                    if (NpcManager.npcExists(p)) {
                        NpcManager.resetKnockout(NpcManager.getNpc(p));
                        p.setHealth(0);
                    }
                }
            }
        }.runTaskTimer(KnockoutPlugin.getPlugin(), 0, 20);

    }

    public static void resetKnockoutEffects(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();

        AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
        jumpAttribute.setBaseValue(0.42);

        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.2f);
        p.setInvisible(false);
        p.setCollidable(true);

        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.showPlayer(KnockoutPlugin.getPlugin(), p));

        p.resetTitle();

    }

    private static void teleportBody(Npc npc) {

        CraftEntity craftArmorStand = (CraftEntity) npc.getArmorStand();
        Entity armorStand = craftArmorStand.getHandle();
        ServerPlayer deadBody = npc.getDeadBody();
        Player p = npc.getPlayer();

        // Teleport dead body to the player while knocked out
        new BukkitRunnable() {
            @Override
            public void run() {
                if (NpcManager.npcExists(p)) {
                    ClientboundTeleportEntityPacket teleportBodyPacket = new ClientboundTeleportEntityPacket(deadBody);
                    broadcastPacket(teleportBodyPacket);
                    if (p.isInsideVehicle()) {
                        deadBody.teleportTo(p.getLocation().getX(), p.getLocation().getY() + 0.5, p.getLocation().getZ());
                        armorStand.teleportTo(p.getLocation().getX(), p.getLocation().getY() + 0.5, p.getLocation().getZ());
                    } else {
                        deadBody.teleportTo(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                        armorStand.teleportTo(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(KnockoutPlugin.getPlugin(), 0, 3);
    }

    private static void setNoCollisions(Npc npc) {

        // Set no collisions for dead body
        PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.getPlayers().add(npc.getDeadBody().displayName);

        broadcastPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

    }

    private static void broadcastPacket(Packet<?> packet) {
        MinecraftServer server = MinecraftServer.getServer();
        List<ServerPlayer> onlinePlayers = server.getPlayerList().players;
        onlinePlayers.forEach(p -> p.connection.send(packet));
    }

    public static List<Npc> getNPCs() {
        return NPCs;
    }

    public static boolean npcExists(Player player) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.isPresent();
    }

    public static boolean npcExists(ArmorStand armorStand) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.isPresent();
    }

    public static boolean npcExists(ServerPlayer deadBody) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getDeadBody().equals(deadBody)).findFirst();
        return matchingNpc.isPresent();
    }

    public static Npc getNpc(Player player) {
        return NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst().get();
    }

    public static Npc getNpc(ArmorStand armorStand) {
        return NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst().get();
    }

    public static Npc getNpc(ServerPlayer deadBody) {
        return NPCs.stream().filter(npc -> npc.getDeadBody().equals(deadBody)).findFirst().get();
    }
}
