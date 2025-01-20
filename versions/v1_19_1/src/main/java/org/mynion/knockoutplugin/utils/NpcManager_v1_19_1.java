package org.mynion.knockoutplugin.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.compatibility.TabAdapter;

import java.util.*;

public class NpcManager_v1_19_1 implements NpcManager {
    public NpcManager_v1_19_1() {
    }

    private static final List<Npc> NPCs = new ArrayList<>();
    private static final Plugin plugin = Knockout.getPlugin();

    public void knockoutPlayer(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();
        MinecraftServer server = sp.getServer();
        GameMode playerGameMode = p.getGameMode();

        // Create dead body
        ServerPlayer deadBodyPlayer = createDeadBody(p);

        // Create dead body server connection
        new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.CLIENTBOUND), deadBodyPlayer);

        // Set dead body model customization
        deadBodyPlayer.restoreFrom(sp, false);
        deadBodyPlayer.setGameMode(GameType.SURVIVAL);

        // Broadcast dead body info packets
        ClientboundPlayerInfoPacket infoUpdatePacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, List.of(deadBodyPlayer));
        ClientboundAddPlayerPacket addEntityPacket = new ClientboundAddPlayerPacket(deadBodyPlayer);
        ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(deadBodyPlayer.getId(), deadBodyPlayer.getEntityData(), true);
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
        startTimer(p);

        ChatUtils.sendMessage(p, "knockout-message");
        deadBodyPlayer.setInvisible(false);
    }

    private ServerPlayer createDeadBody(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();
        MinecraftServer server = sp.getServer();
        ServerLevel level = sp.getLevel();

        UUID deadBodyUUID = UUID.randomUUID();

        //Set different name for dead body to prevent other plugins conflicts
        String deadBodyName = p.getName();

        GameProfile deadBodyProfile = new GameProfile(deadBodyUUID, deadBodyName);

        ServerPlayer deadBodyPlayer = new ServerPlayer(server, level, deadBodyProfile, null);

        deadBodyPlayer.setPos(p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
        deadBodyPlayer.setXRot(sp.getXRot());
        deadBodyPlayer.setYRot(sp.getYRot());
        deadBodyPlayer.setYHeadRot(sp.getYHeadRot());
        deadBodyPlayer.setShoulderEntityLeft(sp.getShoulderEntityLeft());
        deadBodyPlayer.setPose(Pose.SWIMMING);
        deadBodyPlayer.setUUID(deadBodyUUID);
        deadBodyPlayer.setGameMode(GameType.SURVIVAL);

        // Set dead body skin
        try {
            Property skin = (Property) sp.getGameProfile().getProperties().get("textures").toArray()[0];
            String textures = skin.getValue();
            String signature = skin.getSignature();
            deadBodyPlayer.getGameProfile().getProperties().put("textures", new Property("textures", textures, signature));
        } catch (ArrayIndexOutOfBoundsException ignored) {
        }

        return deadBodyPlayer;
    }

    // Resets knockout but does not kill the player
    public void resetKnockout(Player p) {
        Npc npc = getNpc(p);
        GameMode previousGameMode = npc.getPreviousGameMode();

        // Reset knockout effects
        resetKnockoutEffects(p);

        // Remove dead body
        ClientboundPlayerInfoPacket removeNpcPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, getNpc(p).getDeadBody());
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
    public void forceKill(Player p) {
        resetKnockout(p);
        p.setHealth(0);
    }

    private void applyKnockoutEffects(Player p) {

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();

        // Remove all potion effects
        PotionEffectType[] potionEffects = PotionEffectType.values();
        Arrays.asList(potionEffects).forEach(p::removePotionEffect);

        // Add custom potion effects
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100, false, false);
        p.addPotionEffect(invisibility);
        PotionEffect noJump = new PotionEffect(PotionEffectType.JUMP, 999999999, 200, false, false);
        p.addPotionEffect(noJump);
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
        p.setSprinting(false);
        p.setFlying(false);
        p.setInvisible(true);
        p.setCollidable(false);
        p.setGameMode(GameMode.SURVIVAL);
        p.leaveVehicle();
        p.getPassengers().forEach(p::removePassenger);

        // Remove parrots from shoulders
        //TODO

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

    }

    private void startTimer(Player p) {
        int seconds = plugin.getConfig().getInt("knockout-time");
        if (seconds < 0) {
            seconds = 60;
        }
        Npc npc = getNpc(p);
        npc.setKnockoutCooldown(seconds);
        if(seconds > 0){
            String knockoutTitle = Knockout.getPlugin().getConfig().getString("knockout-title");
            if (knockoutTitle != null) {
                npc.getPlayer().sendTitle(ChatColor.translateAlternateColorCodes('&', knockoutTitle), Integer.toString(npc.getKnockoutCooldown()), 1, 20 * 3600, 1);
            }
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(p)) {
                    if (!p.isInsideVehicle() && !getNpc(p).isBeingRevived()) {
                        if (npc.getKnockoutCooldown() > 0) {
                            npc.setKnockoutCooldown(npc.getKnockoutCooldown() - 1);
                        } else {
                            this.cancel();
                        }
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Knockout.getPlugin(), 20, 20);

    }

    public void resetKnockoutEffects(Player p) {

        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        p.removePotionEffect(PotionEffectType.JUMP);

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.2f);
        p.setInvisible(false);
        p.setCollidable(true);

        // Reset collisions
        resetCollisions(p);

        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.showPlayer(Knockout.getPlugin(), p));

        p.resetTitle();

    }

    private void resetCollisions(Player p) {
        ServerPlayer sp = ((CraftPlayer) p).getHandle();

        PlayerTeam team = new PlayerTeam(new Scoreboard(), "player");
        if (sp.getTeam() != null) {
            team.setCollisionRule(sp.getTeam().getCollisionRule());
        } else {
            team.setCollisionRule(Team.CollisionRule.ALWAYS);
        }
        team.getPlayers().add(p.getName());

        broadcastPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

        TabAdapter.setCollisionRule(p, true);
    }

    // Teleporting body and hologram to the player while knocked out
    private void teleportBody(Npc npc) {

        CraftEntity craftArmorStand = (CraftEntity) npc.getArmorStand();
        Entity armorStand = craftArmorStand.getHandle();
        ServerPlayer deadBody = npc.getDeadBody();
        Player p = npc.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(p)) {
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
        }.runTaskTimer(Knockout.getPlugin(), 0, 1);


    }

    // Set no collisions for dead body
    private void setNoCollisions(Npc npc) {

        PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
        team.setCollisionRule(Team.CollisionRule.NEVER);
        team.getPlayers().add(npc.getPlayer().getName());

        broadcastPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

        TabAdapter.setCollisionRule(npc.getPlayer(), false);
    }

    // Start carrying a knocked out player
    public void startCarrying(Player knockedOutPlayer, Player vehicle) {
        getNpc(knockedOutPlayer).setVehicle(vehicle);
        trackVehicle(knockedOutPlayer);
    }

    // Stop carrying a knocked out player
    public void stopCarrying(Player knockedOutPlayer, Player vehicle) {
        getNpc(knockedOutPlayer).setVehicle(null);
        vehicle.removePassenger(knockedOutPlayer);

        // Remove slowness effect only if applied by the plugin
        int slowness_amplifier = vehicle.getPotionEffect(PotionEffectType.SLOW).getAmplifier();
        if (plugin.getConfig().getInt("slowness-amplifier") == slowness_amplifier) {
            vehicle.removePotionEffect(PotionEffectType.SLOW);
        }
    }

    // Tracking a knocked out player vehicle to prevent dismount
    private void trackVehicle(Player knockedOutPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(knockedOutPlayer)) {
                    Player currentVehicle = getNpc(knockedOutPlayer).getVehicle();
                    if (currentVehicle != null) {
                        if (currentVehicle.isOnline()) {
                            currentVehicle.addPassenger(knockedOutPlayer);
                            if (Knockout.getPlugin().getConfig().getBoolean(("slowness-for-carrier"))) {
                                currentVehicle.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 2, plugin.getConfig().getInt("slowness-amplifier"), false, false));
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

    public void startReviving(Player revivingPlayer, Player knockedOutPlayer) {

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
                        //revivingPlayer.setExpCooldown(-1);
                        ((CraftPlayer) revivingPlayer).getHandle().takeXpDelay = -1;

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
                        if (npcExists(knockedOutPlayer)) {
                            getNpc(knockedOutPlayer).setBeingRevived(false);
                        }
                        //revivingPlayer.setExpCooldown(0);
                        ((CraftPlayer) revivingPlayer).getHandle().takeXpDelay = 0;
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

    private void reviveNow(Player revivingPlayer, Player knockedOutPlayer) {
        getNpc(knockedOutPlayer).setBeingRevived(false);
        resetKnockout(knockedOutPlayer);
        ChatUtils.sendMessage(revivingPlayer, "rescuer-revived-message", new HashMap<>(Map.of("%player%", knockedOutPlayer.getDisplayName())));
        ChatUtils.sendMessage(knockedOutPlayer, "rescued-revived-message", new HashMap<>(Map.of("%player%", revivingPlayer.getDisplayName())));
        ((CraftPlayer) revivingPlayer).getHandle().takeXpDelay = 0;
        //revivingPlayer.setExpCooldown(0);
    }

    public void playerJoinActions(Player p) {
        ServerPlayer sp = ((CraftPlayer) p).getHandle();
        ServerLevel level = sp.getLevel();

        // Perform actions for a new player
        getNPCs().forEach(npc -> {

            ServerPlayer deadBodyPlayer = npc.getDeadBody();

            // Show dead bodies for a new player
            ClientboundPlayerInfoPacket infoUpdatePacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, List.of(deadBodyPlayer));
            ClientboundAddPlayerPacket addEntityPacket = new ClientboundAddPlayerPacket(deadBodyPlayer);
            ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(deadBodyPlayer.getId(), deadBodyPlayer.getEntityData(), true);

            sp.connection.send(infoUpdatePacket);
            sp.connection.send(addEntityPacket);
            sp.connection.send(setEntityDataPacket);

            // Set no collisions for dead bodies and a new player
            PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
            team.setCollisionRule(Team.CollisionRule.NEVER);
            team.getPlayers().add(npc.getDeadBody().displayName);

            sp.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

            // Hide knocked out players for a new player
            p.hidePlayer(Knockout.getPlugin(), npc.getPlayer());

        });
    }

    private void hurtAnimation(Player p) {

        // Damage attacked knocked out player
        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(getNpc(p).getDeadBody(), 1);
        broadcastPacket(packet);

    }

    public void removeKOPlayers() {
        // Remove all NPCs
        getNPCs().forEach(npc -> {

            if (npc.getVehicle() != null) {
                npc.getVehicle().removePotionEffect(PotionEffectType.SLOW);
            }

            ClientboundPlayerInfoPacket removeNpcPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc.getDeadBody());
            ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(npc.getDeadBody().getId());
            broadcastPacket(removeNpcPacket);
            broadcastPacket(removeEntityPacket);

            npc.getArmorStand().remove();
            npc.getPlayer().setHealth(0);
            npc.getPlayer().setGameMode(npc.getPreviousGameMode());
            resetKnockoutEffects(npc.getPlayer());

        });
    }

    public void damageKOPlayer(ArmorStand koArmorStand, org.bukkit.entity.Entity attacker, double value) {
        Npc npc = getNpc(koArmorStand);

        if (attacker instanceof Player p) {
            if (npc.getPlayer().equals(p)) return;
        }
        hurtAnimation(npc.getPlayer());

        if (Knockout.getPlugin().getConfig().getBoolean("damage-on-hit")) {
            npc.getPlayer().damage(value);

        } else {
            int timeDecrease = Knockout.getPlugin().getConfig().getInt("time-decrease-on-hit");
            npc.setKnockoutCooldown(npc.getKnockoutCooldown() - timeDecrease);

        }
    }

    private boolean canBeRevivedBy(Player revivingPlayer, Player knockedOutPlayer, Location reviveLocation) {
        return !knockedOutPlayer.isInsideVehicle() && revivingPlayer.isSneaking() && reviveLocation.getBlock().equals(revivingPlayer.getLocation().getBlock());
    }

    private void broadcastPacket(Packet<?> packet) {
        MinecraftServer server = MinecraftServer.getServer();
        List<ServerPlayer> onlinePlayers = server.getPlayerList().players;
        onlinePlayers.forEach(p -> p.connection.send(packet));
    }

    private List<Npc> getNPCs() {
        return NPCs;
    }

    public boolean npcExists(Player player) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.isPresent();
    }

    public boolean npcExists(ArmorStand armorStand) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.isPresent();
    }

    public boolean npcExists(ServerPlayer deadBody) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getDeadBody().equals(deadBody)).findFirst();
        return matchingNpc.isPresent();
    }

    private Npc getNpc(Player player) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.orElse(null);
    }

    private Npc getNpc(ArmorStand armorStand) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.orElse(null);
    }

    private Npc getNpc(ServerPlayer deadBody) {
        Optional<Npc> matchingNpc = NPCs.stream().filter(npc -> npc.getDeadBody().equals(deadBody)).findFirst();
        return matchingNpc.orElse(null);
    }
}
