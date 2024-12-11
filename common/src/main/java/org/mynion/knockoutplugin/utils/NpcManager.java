package org.mynion.knockoutplugin.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.Knockout;

import java.util.*;

public class NpcManager {
    private static final List<Npc> NPCs = new ArrayList<>();
    private static final Plugin plugin = Knockout.getPlugin();

    public static void knockoutPlayer(Player p) {

        ServerPlayer sp = Knockout.getVersionAdapter().getServerPlayer(p);
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
        List<Pair<EquipmentSlot, ItemStack>> items = List.of(
                Pair.of(EquipmentSlot.HEAD, sp.getItemBySlot(EquipmentSlot.HEAD)),
                Pair.of(EquipmentSlot.CHEST, sp.getItemBySlot(EquipmentSlot.CHEST)),
                Pair.of(EquipmentSlot.LEGS, sp.getItemBySlot(EquipmentSlot.LEGS)),
                Pair.of(EquipmentSlot.FEET, sp.getItemBySlot(EquipmentSlot.FEET)),
                Pair.of(EquipmentSlot.MAINHAND, sp.getItemBySlot(EquipmentSlot.MAINHAND)),
                Pair.of(EquipmentSlot.OFFHAND, sp.getItemBySlot(EquipmentSlot.OFFHAND))
        );
        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(deadBodyPlayer.getId(), items);
        broadcastPacket(packet);

        // Create hologram
        ArmorStand armorStand = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
        armorStand.setSmall(true);
        String hologramName = plugin.getConfig().getString("knockout-hologram");
        if (hologramName != null) {
            armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', hologramName));
        }
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(false);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        // Has to be done before creating NPC
        applyKnockoutEffects(p);

        // Create npc
        Npc npc = new Npc(p, deadBodyPlayer, armorStand, playerGameMode);
        NPCs.add(npc);

        setNoCollisions(npc);
        teleportBody(npc);

        ChatUtils.sendMessage(p, "knockout-message");

    }

    private static ServerPlayer createDeadBody(Player p) {

        ServerPlayer sp = Knockout.getVersionAdapter().getServerPlayer(p);
        MinecraftServer server = sp.getServer();
        ServerLevel level = sp.serverLevel();

        UUID deadBodyUUID = UUID.randomUUID();
        String deadBodyName = p.getName();
        GameProfile deadBodyProfile = new GameProfile(deadBodyUUID, deadBodyName);

        ServerPlayer deadBodyPlayer = new ServerPlayer(server, level, deadBodyProfile, ClientInformation.createDefault());
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

        ServerPlayer sp = Knockout.getVersionAdapter().getServerPlayer(p);

        AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
        jumpAttribute.setBaseValue(0);

        // Remove all potion effects
        PotionEffectType[] potionEffects = PotionEffectType.values();
        Arrays.asList(potionEffects).forEach(p::removePotionEffect);

        // Add custom potion effects
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100, false, false);
        p.addPotionEffect(invisibility);
        if (plugin.getConfig().getBoolean("knockout-blindness")) {
            PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 999999999, 1, false, false);
            p.addPotionEffect(blindness);
        }

        // Set player health to max
        AttributeInstance maxHealthAttribute = sp.getAttribute(Attributes.MAX_HEALTH);
        double maxHealth = maxHealthAttribute.getValue();
        p.setHealth(maxHealth);

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

        // Remove parrots from shoulders


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
        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.hidePlayer(Knockout.getPlugin(), p));

        startTimer(p);

    }

    private static void startTimer(Player p) {
        int seconds = plugin.getConfig().getInt("knockout-time");
        if (seconds == 0) {
            seconds = 60;
        }
        final int[] knockoutCooldown = {seconds};
        new BukkitRunnable() {

            @Override
            public void run() {
                if (NpcManager.npcExists(p)) {
                    if (!p.isInsideVehicle() && !NpcManager.getNpc(p).isBeingRevived()) {
                        if (knockoutCooldown[0] > 0) {
                            String knockoutTitle = plugin.getConfig().getString("knockout-title");
                            if (knockoutTitle != null) {
                                p.sendTitle(ChatColor.translateAlternateColorCodes('&', knockoutTitle), Integer.toString(knockoutCooldown[0]), 1, 20 * 3600, 1);
                            }
                            knockoutCooldown[0]--;
                        } else {
                            if (Knockout.getPlugin().getConfig().getBoolean("death-on-end")) {
                                forceKill(p);
                            } else {
                                resetKnockout(getNpc(p));
                            }
                            this.cancel();
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Knockout.getPlugin(), 0, 20);

    }

    public static void resetKnockoutEffects(Player p) {

        ServerPlayer sp = Knockout.getVersionAdapter().getServerPlayer(p);

        AttributeInstance jumpAttribute = sp.getAttribute(Attributes.JUMP_STRENGTH);
        jumpAttribute.setBaseValue(0.42);

        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.2f);
        p.setInvisible(false);
        p.setCollidable(true);

        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.showPlayer(Knockout.getPlugin(), p));

        p.resetTitle();

    }

    // Teleporting body and hologram to the player while knocked out
    private static void teleportBody(Npc npc) {

        Entity armorStand = Knockout.getVersionAdapter().getArmorStand(npc.getArmorStand());
        ServerPlayer deadBody = npc.getDeadBody();
        ServerPlayer sp = Knockout.getVersionAdapter().getServerPlayer(npc.getPlayer());
        Player p = npc.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (NpcManager.npcExists(p)) {
                    double yDiff;
                    if (p.isInsideVehicle()) {
                        yDiff = 0.6;
                    } else {
                        yDiff = -0.2;
                    }
                    //ClientboundTeleportEntityPacket teleportBodyPacket = Knockout.getVersionAdapter().getTeleportPacket(deadBody, sp, yDiff);
                    //broadcastPacket(teleportBodyPacket);
                    deadBody.teleportTo(p.getLocation().getX(), p.getLocation().getY() + yDiff, p.getLocation().getZ());
                    armorStand.teleportTo(p.getLocation().getX(), p.getLocation().getY() + yDiff, p.getLocation().getZ());
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Knockout.getPlugin(), 0, 1);
    }

    // Set no collisions for dead body
    private static void setNoCollisions(Npc npc) {

        PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.getPlayers().add(npc.getDeadBody().displayName);

        broadcastPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

    }

    // Start carrying a knocked out player
    public static void startCarrying(Player knockedOutPlayer, Player vehicle) {
        getNpc(knockedOutPlayer).setVehicle(vehicle);
        trackVehicle(knockedOutPlayer);
    }

    // Stop carrying a knocked out player
    public static void stopCarrying(Player knockedOutPlayer, Player vehicle) {
        getNpc(knockedOutPlayer).setVehicle(null);
        vehicle.removePassenger(knockedOutPlayer);

        // Remove slowness effect only if applied by the plugin
        int slowness_amplifier = vehicle.getPotionEffect(PotionEffectType.SLOWNESS).getAmplifier();
        if (plugin.getConfig().getInt("slowness-amplifier") == slowness_amplifier) {
            vehicle.removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }

    // Tracking a knocked out player vehicle to prevent dismount
    private static void trackVehicle(Player knockedOutPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(knockedOutPlayer)) {
                    Player currentVehicle = getNpc(knockedOutPlayer).getVehicle();
                    if (currentVehicle != null) {
                        if (currentVehicle.isOnline()) {
                            currentVehicle.addPassenger(knockedOutPlayer);
                            if (Knockout.getPlugin().getConfig().getBoolean(("slowness-for-carrier"))) {
                                currentVehicle.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20 * 2, plugin.getConfig().getInt("slowness-amplifier"), false, false));
                            }
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
        }.runTaskTimer(Knockout.getPlugin(), 0, 2);
    }

    public static void startReviving(Player revivingPlayer, Player knockedOutPlayer) {

        if (!revivingPlayer.hasPermission("knockout.revive")) {
            ChatUtils.sendMessage(revivingPlayer, "no-permission-message");
            return;
        }

        int reviveTime = Knockout.getPlugin().getConfig().getInt("revive-time");
        if (reviveTime <= 0) {
            reviveNow(revivingPlayer, knockedOutPlayer);
            return;
        }

        int timeInTicks = reviveTime * 20;
        int requiredLevels = plugin.getConfig().getInt("revive-levels");
        int period = 1;
        float expDecrement = (float) requiredLevels / ((float) timeInTicks / period);
        Location reviveLocation = revivingPlayer.getLocation();

        if (revivingPlayer.getLevel() >= requiredLevels) {
            getNpc(knockedOutPlayer).setBeingRevived(true);
            new BukkitRunnable() {
                int timer = 0;
                int percent = 0;

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
                        }

                        // Update timer and send titles
                        timer += period;
                        percent = (int) (((float) timer / (float) timeInTicks) * 100);

                        String revivingTitle = plugin.getConfig().getString("rescuer-reviving-title");
                        if (revivingTitle != null) {
                            revivingPlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', revivingTitle), percent + "%", 1, period, 1);
                        }

                        String revivedTitle = plugin.getConfig().getString("rescued-reviving-title");
                        if (revivedTitle != null) {
                            knockedOutPlayer.sendTitle(ChatColor.translateAlternateColorCodes('&', revivedTitle), percent + "%", 1, period, 1);
                        }

                        // Check if the revive process is complete
                        if (percent == 100) {

                            // Revive a KO player
                            reviveNow(revivingPlayer, knockedOutPlayer);
                            this.cancel();
                        }
                    } else {
                        if (NpcManager.npcExists(knockedOutPlayer)) {
                            getNpc(knockedOutPlayer).setBeingRevived(false);
                        }
                        revivingPlayer.setExpCooldown(0);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Knockout.getPlugin(), 0, period);
        } else {
            if (!revivingPlayer.isSneaking() && !knockedOutPlayer.isInsideVehicle()) {
                ChatUtils.sendMessage(revivingPlayer, "no-levels-message", new HashMap<>(Map.of("%levels%", String.valueOf(requiredLevels))));
            }
        }
    }

    private static void reviveNow(Player revivingPlayer, Player knockedOutPlayer) {
        getNpc(knockedOutPlayer).setBeingRevived(false);
        resetKnockout(getNpc(knockedOutPlayer));
        ChatUtils.sendMessage(revivingPlayer, "rescuer-revived-message", new HashMap<>(Map.of("%player%", knockedOutPlayer.getDisplayName())));
        ChatUtils.sendMessage(knockedOutPlayer, "rescued-revived-message", new HashMap<>(Map.of("%player%", revivingPlayer.getDisplayName())));
        revivingPlayer.setExpCooldown(0);
    }

    private static boolean canBeRevivedBy(Player revivingPlayer, Player knockedOutPlayer, Location reviveLocation) {
        return !knockedOutPlayer.isInsideVehicle() && revivingPlayer.isSneaking() && reviveLocation.getBlock().equals(revivingPlayer.getLocation().getBlock());
    }

    public static void broadcastPacket(Packet<?> packet) {
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
