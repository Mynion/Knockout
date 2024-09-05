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
import org.bukkit.Location;
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
        GameMode playerGameMode = p.getGameMode();

        // Create dead body
        ServerPlayer deadBodyPlayer = createDeadBody(p);

        // Create dead body server connection
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.CLIENTBOUND), deadBodyPlayer, CommonListenerCookie.createInitial(deadBodyPlayer.getGameProfile(), false));

        // Broadcast dead body info packets
        ServerEntity deadBodyEntity = new ServerEntity(level, deadBodyPlayer, 20, false, null, null);
        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), List.of(deadBodyPlayer));
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(deadBodyPlayer, deadBodyEntity);
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

        // Has to be done before creating NPC
        applyKnockoutEffects(p);

        // Create npc
        Npc npc = new Npc(p, deadBodyPlayer, armorStand, playerGameMode);
        NPCs.add(npc);

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

    // Resets knockout but does not kill the player
    public static void resetKnockout(Npc npc) {
        Player p = npc.getPlayer();
        GameMode previousGameMode = npc.getPreviousGameMode();

        // Reset knockout effects
        resetKnockoutEffects(p);

        // Remove dead body
        ClientboundPlayerInfoRemovePacket removeNpcPacket = new ClientboundPlayerInfoRemovePacket(List.of(npc.getDeadBody().getGameProfile().getId()));
        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(npc.getDeadBody().getId());
        broadcastPacket(removeNpcPacket);
        broadcastPacket(removeEntityPacket);

        // Remove hologram
        npc.getArmorStand().remove();

        // Remove npc from npc list
        NPCs.remove(npc);

        // Set previous gamemode
        p.setGameMode(previousGameMode);

    }

    // Resets knockout and kills the player
    public static void forceKill(Player p) {
        resetKnockout(getNpc(p));
        p.setHealth(0);
    }

    private static void applyKnockoutEffects(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();

        AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
        jumpAttribute.setBaseValue(0);

        // Remove all potion effects
        PotionEffectType[] potionEffects = PotionEffectType.values();
        Arrays.asList(potionEffects).forEach(p::removePotionEffect);

        // Add custom potion effects
        PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 999999999, 1, false, false);
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100, false, false);
        p.addPotionEffect(blindness);
        p.addPotionEffect(invisibility);

        p.setHealth(2);
        p.setWalkSpeed(0);
        p.setFlySpeed(0);
        p.setFireTicks(0);
        p.setNoDamageTicks(11);
        p.setFlying(false);
        p.setInvisible(true);
        p.setCollidable(false);
        p.setGameMode(GameMode.SURVIVAL);
        p.leaveVehicle();
        p.getPassengers().forEach(p::removePassenger);

        // Reset nearby mobs focus on a KO player
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

        // Hide KO player
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.hidePlayer(KnockoutPlugin.getPlugin(), p));

        startTimer(p);

    }

    private static void startTimer(Player p) {

        final int[] knockoutCooldown = {60};
        new BukkitRunnable() {
            @Override
            public void run() {
                if (NpcManager.npcExists(p) && !p.isInsideVehicle() && !NpcManager.getNpc(p).isBeingRevived()) {
                    if (knockoutCooldown[0] > 0) {
                        p.sendTitle(ChatColor.RED + "Knockout", Integer.toString(knockoutCooldown[0]), 1, 20 * 3600, 1);
                        knockoutCooldown[0]--;
                    } else {
                        forceKill(p);
                        this.cancel();
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

    // Teleporting body and hologram to the player while knocked out
    private static void teleportBody(Npc npc) {

        CraftEntity craftArmorStand = (CraftEntity) npc.getArmorStand();
        Entity armorStand = craftArmorStand.getHandle();
        ServerPlayer deadBody = npc.getDeadBody();
        Player p = npc.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (NpcManager.npcExists(p)) {
                    ClientboundTeleportEntityPacket teleportBodyPacket = new ClientboundTeleportEntityPacket(deadBody);
                    broadcastPacket(teleportBodyPacket);
                    if (p.isInsideVehicle()) {
                        deadBody.teleportTo(p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                        armorStand.teleportTo(p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                    } else {
                        deadBody.teleportTo(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                        armorStand.teleportTo(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(KnockoutPlugin.getPlugin(), 0, 1);
    }

    // Set no collisions for dead body
    private static void setNoCollisions(Npc npc) {

        PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.getPlayers().add(npc.getDeadBody().displayName);

        broadcastPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

    }

    // Tracking a knocked out player vehicle to prevent dismount
    public static void trackVehicle(Player knockedOutPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(knockedOutPlayer)) {
                    Player currentVehicle = getNpc(knockedOutPlayer).getVehicle();
                    if (currentVehicle != null) {
                        if (currentVehicle.isOnline()) {
                            currentVehicle.addPassenger(knockedOutPlayer);
                        } else {
                            knockedOutPlayer.leaveVehicle();
                            getNpc(knockedOutPlayer).setVehicle(null);
                            this.cancel();
                        }
                    } else {
                        this.cancel();
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(KnockoutPlugin.getPlugin(), 0, 2);
    }

    public static void startReviving(Player revivingPlayer, Player knockedOutPlayer) {

        int requiredLevels = 5;
        int playerStartingLevel = revivingPlayer.getLevel();
        float playerStartingExp = revivingPlayer.getExp();
        int timeInTicks = 10 * 20;
        int period = 1;
        float expDecrement = (float) requiredLevels / ((float) timeInTicks / period);
        Location reviveLocation = revivingPlayer.getLocation();

        if (playerStartingLevel >= requiredLevels) {
            getNpc(knockedOutPlayer).setBeingRevived(true);
            new BukkitRunnable() {
                int timer = 0;
                float usedLevels = 0;

                @Override
                public void run() {
                    if (canBeRevivedBy(revivingPlayer, knockedOutPlayer, reviveLocation)) {
                        revivingPlayer.setExpCooldown(-1);

                        // Checks if the player has enough xp to be decremented
                        if (revivingPlayer.getExp() >= expDecrement) {
                            // Decrement player xp
                            revivingPlayer.setExp(revivingPlayer.getExp() - expDecrement);
                        } else {
                            // Reduce player level by 1 and set xp bar to maximum
                            revivingPlayer.setLevel(revivingPlayer.getLevel() - 1);
                            revivingPlayer.setExp(0.9999f);
                            usedLevels = playerStartingLevel - revivingPlayer.getLevel();
                        }

                        // Update timer and send titles
                        timer += period;
                        revivingPlayer.sendTitle(ChatColor.GREEN + "Reviving...", (int) (((float) timer / (float) timeInTicks) * 100) + "%", 1, period, 1);
                        knockedOutPlayer.sendTitle(ChatColor.GREEN + "You are being revived... ", (int) (((float) timer / (float) timeInTicks) * 100) + "%", 1, period, 1);

                        // Check if the player has used required levels
                        if (usedLevels >= requiredLevels) {
                            if (revivingPlayer.getExp() < expDecrement) {
                                revivingPlayer.setExp(0);
                            }

                            // Check if the player has used required xp on xp bar
                            if (revivingPlayer.getExp() <= playerStartingExp) {

                                // Revive a KO player
                                NpcManager.resetKnockout(NpcManager.getNpc(knockedOutPlayer));
                                revivingPlayer.sendMessage("You revived " + knockedOutPlayer.getDisplayName());
                                knockedOutPlayer.sendMessage("You have been revived by " + revivingPlayer.getDisplayName());
                                revivingPlayer.setExpCooldown(0);
                                getNpc(knockedOutPlayer).setBeingRevived(false);
                                this.cancel();

                            }
                        }
                    } else {
                        getNpc(knockedOutPlayer).setBeingRevived(false);
                        revivingPlayer.setExpCooldown(0);
                        this.cancel();
                    }
                }
            }.runTaskTimer(KnockoutPlugin.getPlugin(), 0, period);
        } else {
            if (!revivingPlayer.isSneaking() && !knockedOutPlayer.isInsideVehicle()) {
                revivingPlayer.sendMessage(ChatColor.RED + "You don't have enough levels ( " + ChatColor.RED + requiredLevels + ChatColor.RED + " ) to revive that player");
            }
        }
    }

    private static boolean canBeRevivedBy(Player revivingPlayer, Player knockedOutPlayer, Location reviveLocation) {
        return !knockedOutPlayer.isInsideVehicle() && revivingPlayer.isSneaking() && reviveLocation.getBlock().equals(revivingPlayer.getLocation().getBlock());
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
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.orElse(null);
    }

    public static Npc getNpc(ArmorStand armorStand) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.orElse(null);
    }

    public static Npc getNpc(ServerPlayer deadBody) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getDeadBody().equals(deadBody)).findFirst();
        return matchingNpc.orElse(null);
    }
}
