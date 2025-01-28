package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.compatibility.TabAdapter;
import org.mynion.knockoutplugin.enums.PacketType;
import org.mynion.knockoutplugin.enums.PotionType;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NpcManager {
    private final VersionController versionController;

    public NpcManager(VersionController versionController) {
        this.versionController = versionController;
    }

    private static final List<NpcModel> NPCs = new ArrayList<>();
    private static final Plugin plugin = Knockout.getPlugin();

    public void knockoutPlayer(Player p, @Nullable Entity damager) {

        // Drop all carried knocked out players
        p.getPassengers().stream()
                .filter(passenger -> passenger instanceof Player)
                .map(passenger -> (Player) passenger)
                .filter(this::npcExists)
                .forEach(knockedOutPlayer -> stopCarrying(knockedOutPlayer, p));

        GameMode playerGameMode = p.getGameMode();

        // Has to be done before creating NPC
        applyKnockoutEffects(p);

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

        // Create npc
        NpcModel npc = versionController.createNpc(p, armorStand, playerGameMode, damager);
        NPCs.add(npc);

        // Broadcast body info packets
        versionController.broadcastPacket(npc, PacketType.INFO_UPDATE);
        versionController.broadcastPacket(npc, PacketType.ADD_ENTITY);
        versionController.broadcastPacket(npc, PacketType.SET_ENTITY_DATA);
        versionController.broadcastPacket(npc, PacketType.SET_EQUIPMENT);

        versionController.setCollisions(p, false);
        TabAdapter.setCollisionRule(npc.getPlayer(), false);

        teleportBody(npc);
        startTimer(npc);

        MessageUtils.sendMessage(p, "knockout-message");
        if (damager instanceof Player) {
            MessageUtils.sendMessage(damager, "knockout-attacker-message", new HashMap<>(Map.of("%player%", p.getName())));
        }
    }


    // Resets knockout but does not kill the player
    public void resetKnockout(Player p) {
        NpcModel npc = getNpc(p);
        GameMode previousGameMode = npc.getPreviousGameMode();

        if (p.isInsideVehicle()) {
            p.leaveVehicle();
        }

        // Reset knockout effects
        resetKnockoutEffects(p);

        // Remove body
        versionController.broadcastPacket(npc, PacketType.INFO_REMOVE);
        versionController.broadcastPacket(npc, PacketType.REMOVE_ENTITY);

        // Remove hologram
        npc.getArmorStand().remove();

        // Remove npc from npc list
        NPCs.remove(npc);

        // Set previous gamemode
        p.setGameMode(previousGameMode);

    }

    // Ends knockout and kills the player
    public void forceKill(Player p) {
        if (getDamager(p) != null) {
            p.damage(1, getDamager(p));
        }
        resetKnockout(p);
        p.setHealth(0);
    }

    private void applyKnockoutEffects(Player p) {

        // Remove all potion effects
        PotionEffectType[] potionEffects = PotionEffectType.values();
        Arrays.asList(potionEffects).forEach(p::removePotionEffect);

        // Add custom potion effects
        PotionEffect invisibility = new PotionEffect(PotionEffectType.INVISIBILITY, 999999999, 100, false, false);
        p.addPotionEffect(invisibility);
        if (!plugin.getConfig().getBoolean("jump-when-knocked-out")) {
            versionController.setAbleToJump(p, false);
        }
        if (plugin.getConfig().getBoolean("knockout-blindness")) {
            PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 999999999, plugin.getConfig().getInt("blindness-amplifier"), false, false);
            p.addPotionEffect(blindness);
        }

        // Set player health to max
        versionController.setMaxHealth(p);

        p.setWalkSpeed(0);
        if (plugin.getConfig().getBoolean(("move-when-knocked-out"))) {
            p.setWalkSpeed(0.05f);
        }
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
        versionController.removeParrotsFromShoulders(p);

        // Reset nearby mobs focus on a KO player
        List<Entity> nearbyMobs = p.getNearbyEntities(50, 50, 50);
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

    private void startTimer(NpcModel npc) {
        Player p = npc.getPlayer();
        int seconds = plugin.getConfig().getInt("knockout-time");
        if (seconds < 0) {
            seconds = 60;
        }

        LocalTime time = LocalTime.ofSecondOfDay(seconds);
        String formattedTime;
        if (seconds < 60) {
            formattedTime = time.format(DateTimeFormatter.ofPattern("s"));
        } else if (seconds < 3600) {
            formattedTime = time.format(DateTimeFormatter.ofPattern("m:ss"));
        } else {
            formattedTime = time.format(DateTimeFormatter.ofPattern("H:mm:ss"));
        }

        npc.setKnockoutCooldown(seconds);
        if (seconds > 0) {
            MessageUtils.sendTitle(p, "knockout-title", "knockout-subtitle", new HashMap<>(Map.of("%timer%", formattedTime)), new HashMap<>(Map.of("%timer%", formattedTime)), 1, 20 * 3600, 1);
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
        versionController.setAbleToJump(p, true);

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
        versionController.setCollisions(p, true);
        TabAdapter.setCollisionRule(p, true);
    }

    // Teleporting body and hologram to the player while knocked out
    private void teleportBody(NpcModel npc) {
        Player p = npc.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(p)) {
                    versionController.broadcastPacket(npc, PacketType.TELEPORT);
                    if (p.isInsideVehicle()) {
                        versionController.teleportMannequin(npc, p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                        versionController.teleportHologram(npc, p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                    } else {
                        versionController.teleportMannequin(npc, p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                        versionController.teleportHologram(npc, p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                    }
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Knockout.getPlugin(), 0, 1);


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
        int slowness_amplifier = versionController.getPotionAmplifier(vehicle, PotionType.SLOWNESS);
        if (plugin.getConfig().getInt("slowness-amplifier") == slowness_amplifier) {
            versionController.removePotionEffect(vehicle, PotionType.SLOWNESS);
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
                                versionController.addPotionEffect(currentVehicle, PotionType.SLOWNESS, 20 * 2, plugin.getConfig().getInt("slowness-amplifier"), false, false);
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
            MessageUtils.sendMessage(revivingPlayer, "no-permission-message");
            return;
        }

        int reviveTime = Knockout.getPlugin().getConfig().getInt("revive-time");
        if (reviveTime <= 0) {
            reviveNow(revivingPlayer, knockedOutPlayer);
            return;
        }

        int timeInTicks = reviveTime * 20;
        int requiredLevels = plugin.getConfig().getInt("revive-levels");
        int period = 2;
        float expDecrement = (float) requiredLevels / ((float) timeInTicks / period);
        Location reviveLocation = revivingPlayer.getLocation();

        if (revivingPlayer.getLevel() >= requiredLevels) {
            getNpc(knockedOutPlayer).setBeingRevived(true);
            new BukkitRunnable() {
                int timer = 0;
                int percent = 0;
                String loadingIcon = "\\";

                @Override
                public void run() {
                    if (canBeRevivedBy(revivingPlayer, knockedOutPlayer, reviveLocation)) {
                        versionController.setXpDelay(revivingPlayer, -1);

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

                        HashMap<String, String> rescuerReplacements = new HashMap<>(Map.of("%percent%", String.valueOf(percent), "%loading-icon%", loadingIcon, "%player%", knockedOutPlayer.getName()));
                        HashMap<String, String> rescuedReplacements = new HashMap<>(Map.of("%percent%", String.valueOf(percent), "%loading-icon%", loadingIcon, "%player%", revivingPlayer.getName()));
                        MessageUtils.sendTitle(revivingPlayer, "rescuer-reviving-title", "rescuer-reviving-subtitle", rescuerReplacements, rescuerReplacements, 1, period, 1);
                        MessageUtils.sendTitle(knockedOutPlayer, "rescued-reviving-title", "rescued-reviving-subtitle", rescuedReplacements, rescuedReplacements, 1, period, 1);

                        switch (loadingIcon) {
                            case "\\" -> loadingIcon = "|";
                            case "|" -> loadingIcon = "/";
                            case "/" -> loadingIcon = "-";
                            default -> loadingIcon = "\\";
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
                        versionController.setXpDelay(revivingPlayer, 0);
                        this.cancel();
                    }
                }
            }.runTaskTimer(Knockout.getPlugin(), 0, period);
        } else {
            if (!revivingPlayer.isSneaking() && !knockedOutPlayer.isInsideVehicle()) {
                MessageUtils.sendMessage(revivingPlayer, "no-levels-message", new HashMap<>(Map.of("%levels%", String.valueOf(requiredLevels))));
            }
        }
    }

    private void reviveNow(Player revivingPlayer, Player knockedOutPlayer) {
        getNpc(knockedOutPlayer).setBeingRevived(false);
        resetKnockout(knockedOutPlayer);
        MessageUtils.sendMessage(revivingPlayer, "rescuer-revived-message", new HashMap<>(Map.of("%player%", knockedOutPlayer.getDisplayName())));
        MessageUtils.sendMessage(knockedOutPlayer, "rescued-revived-message", new HashMap<>(Map.of("%player%", revivingPlayer.getDisplayName())));
        MessageUtils.sendTitle(revivingPlayer, "rescuer-revived-title", "rescuer-revived-subtitle", new HashMap<>(Map.of("%player%", knockedOutPlayer.getName())), new HashMap<>(Map.of("%player%", knockedOutPlayer.getName())), 10, 20 * 3, 10);
        MessageUtils.sendTitle(knockedOutPlayer, "rescued-revived-title", "rescued-revived-subtitle", new HashMap<>(Map.of("%player%", revivingPlayer.getName())), new HashMap<>(Map.of("%player%", revivingPlayer.getName())), 10, 20 * 3, 10);
        versionController.setXpDelay(revivingPlayer, 0);
    }

    public void playerJoinActions(Player p) {
        //ServerPlayer sp = ((CraftPlayer) p).getHandle();

        // Perform actions for a new player
        getNPCs().forEach(npc -> {

            //ServerPlayer deadBodyPlayer = npc.getDeadBody();

            // Show bodies for a new player
            versionController.sendPacket(p, npc, PacketType.INFO_UPDATE);
            versionController.sendPacket(p, npc, PacketType.ADD_ENTITY);
            versionController.sendPacket(p, npc, PacketType.SET_ENTITY_DATA);

            // Set no collisions for bodies and a new player
            versionController.setCollisions(p, false);

            // Hide knocked out players for a new player
            p.hidePlayer(Knockout.getPlugin(), npc.getPlayer());

        });
    }

    private void hurtAnimation(Player p) {

        // Play damage animation attacked knocked out player
        versionController.broadcastPacket(getNpc(p), PacketType.ANIMATE);

    }

    public void removeKOPlayers() {
        // Remove all NPCs
        getNPCs().forEach(npc -> {

            if (npc.getVehicle() != null) {
                versionController.removePotionEffect(npc.getVehicle(), PotionType.SLOWNESS);
            }

            versionController.broadcastPacket(npc, PacketType.INFO_REMOVE);
            versionController.broadcastPacket(npc, PacketType.REMOVE_ENTITY);

            npc.getArmorStand().remove();
            npc.getPlayer().setHealth(0);
            npc.getPlayer().setGameMode(npc.getPreviousGameMode());
            resetKnockoutEffects(npc.getPlayer());

        });
    }

    public void damageKOPlayer(ArmorStand koArmorStand, Entity attacker, double value) {
        NpcModel npc = getNpc(koArmorStand);

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

    private List<NpcModel> getNPCs() {
        return NPCs;
    }

    public boolean npcExists(Player player) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.isPresent();
    }

    public boolean npcExists(ArmorStand armorStand) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.isPresent();
    }

    public NpcModel getNpc(Player player) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.orElse(null);
    }

    private NpcModel getNpc(ArmorStand armorStand) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.orElse(null);
    }

    public Entity getDamager(Player p) {
        return getNpc(p).getDamager();
    }
}
