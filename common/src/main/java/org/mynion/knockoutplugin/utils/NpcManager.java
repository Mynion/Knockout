package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
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

    // Knockout a player
    public void knockoutPlayer(Player p, @Nullable Entity damager, int time) {

        // Drop all carried knocked out players
        p.getPassengers().stream()
                .filter(passenger -> passenger instanceof Player)
                .map(passenger -> (Player) passenger)
                .filter(this::npcExists)
                .forEach(knockedOutPlayer -> dropPlayer(knockedOutPlayer, p));

        GameMode playerGameMode = p.getGameMode();

        // Has to be done before creating NPC
        applyKnockoutEffects(p);

        // Create hologram
        ArmorStand armorStand = (ArmorStand) p.getWorld().spawnEntity(p.getLocation(), EntityType.ARMOR_STAND);
        armorStand.setSmall(true);
        String hologramName = plugin.getConfig().getString("knockout-hologram");
        if (hologramName != null) {
            armorStand.setCustomName(MessageUtils.translateColorCodes(hologramName));
        }
        armorStand.setCustomNameVisible(true);
        armorStand.setInvulnerable(false);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);

        // Create npc
        NpcModel npc = versionController.createNpc(p, armorStand, playerGameMode, damager);
        NPCs.add(npc);

        // Broadcast mannequin info packets
        versionController.broadcastPacket(npc, PacketType.INFO_UPDATE);
        versionController.broadcastPacket(npc, PacketType.ADD_ENTITY);
        versionController.broadcastPacket(npc, PacketType.SET_ENTITY_DATA);
        versionController.broadcastPacket(npc, PacketType.SET_EQUIPMENT);

        versionController.setCollisions(p, false);
        TabAdapter.setCollisionRule(npc.getPlayer(), false);

        teleportMannequin(npc);
        startTimer(npc, time);

        MessageUtils.sendMessage(p, "knockout-message");
        if (damager instanceof Player damagerPlayer) {
            MessageUtils.sendMessage(damagerPlayer, "knockout-attacker-message", new HashMap<>(Map.of("%player%", p.getName())));
            MessageUtils.sendTitle(damagerPlayer, "knockout-attacker-title", "knockout-attacker-subtitle", new HashMap<>(Map.of("%player%", p.getName())), new HashMap<>(Map.of("%player%", p.getName())), 10, 20, 10);
        }

        runConfigCommands("console-knockout-commands", p, false);
        runConfigCommands("console-knockout-loop-commands", p, true);

    }

    // Run commands from the config
    public void runConfigCommands(String configPath, Player knockedOutPlayer, boolean isLooped) {
        if (!plugin.getConfig().getBoolean("enable-console-commands")) {
            return;
        }

        List<?> commands = plugin.getConfig().getList(configPath);

        if (commands == null) {
            return;
        }

        int delay = 0;
        for (Object e : commands) {
            if (e instanceof String task) {
                if (isLooped && !npcExists(knockedOutPlayer)) return;
                if (task.startsWith("DELAY")) {
                    String[] split = task.split(" ");
                    delay += Integer.parseInt(split[1]);
                } else {
                    String command = MessageUtils.translateColorCodes(task.replace("%player%", knockedOutPlayer.getName()));
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (isLooped && !npcExists(knockedOutPlayer)) return;
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                        }
                    }.runTaskLater(plugin, delay);
                }
                if (isLooped && commands.get(commands.size() - 1).equals(e)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            runConfigCommands(configPath, knockedOutPlayer, true);
                        }
                    }.runTaskLater(plugin, delay);
                }
            }
        }
    }


    // End knockout for a player
    public void endKnockout(Player p, boolean killPlayer) {
        NpcModel npc = getNpc(p);

        endKO(p, killPlayer);

        // Remove npc from npc list
        NPCs.remove(npc);

    }

    private void endKO(Player p, boolean killPlayer) {
        NpcModel npc = getNpc(p);
        GameMode previousGameMode = npc.getPreviousGameMode();

        // Leave vehicle
        if (p.isInsideVehicle()) {
            p.leaveVehicle();
        }

        // Kill player if needed
        if (killPlayer) {
            // Sets a player killer
            if (getKiller(p) != null) {
                getNpc(p).setVulnerableByPlayerWhenCarried(true);
                p.damage(p.getHealth(), getKiller(p));
            }
            if (p.getHealth() > 0) p.setHealth(0);
        }

        // Reset knockout effects
        endKnockoutEffects(p);

        // Remove mannequin
        versionController.broadcastPacket(npc, PacketType.INFO_REMOVE);
        versionController.broadcastPacket(npc, PacketType.REMOVE_ENTITY);

        // Remove hologram
        npc.getArmorStand().remove();

        // Set previous gamemode
        p.setGameMode(previousGameMode);

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

        // Prevent xp gain
        versionController.setXpDelay(p, -1);

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
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Knockout.getNpcManager().npcExists(p)) this.cancel();

                versionController.removeParrotFromShoulder(p, true);
                versionController.removeParrotFromShoulder(p, false);
            }
        }.runTaskTimer(Knockout.getPlugin(), 0, 2);


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

    private void startTimer(NpcModel npc, int seconds) {
        Player p = npc.getPlayer();

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

                if (!npcExists(p)) {
                    this.cancel();
                    return;
                }

                if (!p.isInsideVehicle() && !getNpc(p).isBeingRevived()) {

                    if (npc.getKnockoutCooldown() <= 0) {
                        this.cancel();
                        return;
                    }

                    npc.setKnockoutCooldown(npc.getKnockoutCooldown() - 1);
                }
            }
        }.runTaskTimer(Knockout.getPlugin(), 20, 20);

    }

    // End knockout effects given to a player
    public void endKnockoutEffects(Player p) {

        p.removePotionEffect(PotionEffectType.BLINDNESS);
        p.removePotionEffect(PotionEffectType.INVISIBILITY);
        versionController.setAbleToJump(p, true);
        versionController.setXpDelay(p, 0);

        p.setWalkSpeed(0.2f);
        p.setFlySpeed(0.2f);
        p.setInvisible(false);
        p.setCollidable(true);

        // Reset collisions
        versionController.setCollisions(p, true);
        TabAdapter.setCollisionRule(p, true);

        Bukkit.getServer().getOnlinePlayers().forEach(player -> player.showPlayer(Knockout.getPlugin(), p));

        p.resetTitle();

    }

    // Teleporting mannequin and hologram to the player while knocked out
    private void teleportMannequin(NpcModel npc) {
        Player p = npc.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!npcExists(p)) {
                    this.cancel();
                    return;
                }
                versionController.broadcastPacket(npc, PacketType.TELEPORT);
                if (p.isInsideVehicle()) {
                    versionController.teleportMannequin(npc, p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                    versionController.teleportHologram(npc, p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                } else {
                    versionController.teleportMannequin(npc, p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                    versionController.teleportHologram(npc, p.getLocation().getX(), p.getLocation().getY() - 0.2, p.getLocation().getZ());
                }
            }
        }.runTaskTimer(Knockout.getPlugin(), 0, 1);

    }

    // Start carrying a knocked out player
    public void carryPlayer(Player knockedOutPlayer, Player vehicle) {
        getNpc(knockedOutPlayer).setVehicle(vehicle);

        // Checking if knocked out player is riding a vehicle
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!npcExists(knockedOutPlayer)) {
                    this.cancel();
                    return;
                }

                Player currentVehicle = getNpc(knockedOutPlayer).getVehicle();

                if (currentVehicle == null) {
                    this.cancel();
                    return;
                }

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

            }
        }.runTaskTimer(Knockout.getPlugin(), 0, 2);

    }

    // Stop carrying a knocked out player
    public void dropPlayer(Player knockedOutPlayer, Player vehicle) {
        getNpc(knockedOutPlayer).setVehicle(null);
        vehicle.removePassenger(knockedOutPlayer);

        // Remove slowness effect only if applied by the plugin
        int slowness_amplifier = versionController.getPotionAmplifier(vehicle, PotionType.SLOWNESS);
        if (plugin.getConfig().getInt("slowness-amplifier") == slowness_amplifier) {
            versionController.removePotionEffect(vehicle, PotionType.SLOWNESS);
        }
    }

    // Start reviving a knocked out player by another player
    public void startReviving(Player revivingPlayer, Player knockedOutPlayer) {

        if (!revivingPlayer.hasPermission("knockout.revive")) {
            MessageUtils.sendMessage(revivingPlayer, "no-permission-message");
            return;
        }

        // Revive item check variable
        Material reviveItemMaterial = Material.getMaterial(Knockout.getPlugin().getConfig().getString("revive-item"));

        // Check item conditions
        if (reviveItemMaterial != null) {
            if (reviveItemMaterial != revivingPlayer.getInventory().getItemInMainHand().getType()) {
                // Prevent sending a message twice
                if (!revivingPlayer.isSneaking() && !knockedOutPlayer.isInsideVehicle()) {
                    MessageUtils.sendMessage(revivingPlayer, "revive-item-missing-message");
                }
                return;
            }
        }

        int reviveTime = Knockout.getPlugin().getConfig().getInt("revive-time");
        if (reviveTime <= 0) {
            revivePlayer(knockedOutPlayer, revivingPlayer);
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

                    // Maybe optimize later
                    if(reviveItemMaterial != null) {
                        // We have to check item conditions all the time because player might switch item in main hand
                        if (reviveItemMaterial != revivingPlayer.getInventory().getItemInMainHand().getType()) {
                            if (npcExists(knockedOutPlayer)) {
                                getNpc(knockedOutPlayer).setBeingRevived(false);
                            }
                            versionController.setXpDelay(revivingPlayer, 0);

                            this.cancel();
                        }
                    }

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
                            revivePlayer(knockedOutPlayer, revivingPlayer);
                            // Decrease item amount in player's hand
                            revivingPlayer.getInventory().getItemInMainHand().setAmount(revivingPlayer.getInventory().getItemInMainHand().getAmount() - 1);
                            versionController.setXpDelay(revivingPlayer, 0);
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

    // Revive a knocked out player by another player
    public void revivePlayer(Player knockedOutPlayer, Player revivingPlayer) {
        getNpc(knockedOutPlayer).setBeingRevived(false);
        endKnockout(knockedOutPlayer, false);
        double health = Knockout.getPlugin().getConfig().getDouble("revived-health");
        if(health > 0) knockedOutPlayer.setHealth(health);
        if(health == -1) knockedOutPlayer.setHealth(knockedOutPlayer.getMaxHealth());
        MessageUtils.sendMessage(revivingPlayer, "rescuer-revived-message", new HashMap<>(Map.of("%player%", knockedOutPlayer.getDisplayName())));
        MessageUtils.sendMessage(knockedOutPlayer, "rescued-revived-by-message", new HashMap<>(Map.of("%player%", revivingPlayer.getDisplayName())));
        MessageUtils.sendTitle(revivingPlayer, "rescuer-revived-title", "rescuer-revived-subtitle", new HashMap<>(Map.of("%player%", knockedOutPlayer.getName())), new HashMap<>(Map.of("%player%", knockedOutPlayer.getName())), 10, 20 * 3, 10);
        MessageUtils.sendTitle(knockedOutPlayer, "rescued-revived-by-title", "rescued-revived-by-subtitle", new HashMap<>(Map.of("%player%", revivingPlayer.getName())), new HashMap<>(Map.of("%player%", revivingPlayer.getName())), 10, 20 * 3, 10);
        runConfigCommands("console-after-revive-commands", knockedOutPlayer, false);
    }

    // Revive a knocked out player
    public void revivePlayer(Player knockedOutPlayer) {
        getNpc(knockedOutPlayer).setBeingRevived(false);
        endKnockout(knockedOutPlayer, false);
        double health = Knockout.getPlugin().getConfig().getDouble("revived-health");
        if(health > 0) knockedOutPlayer.setHealth(health);
        if(health == -1) knockedOutPlayer.setHealth(knockedOutPlayer.getMaxHealth());
        MessageUtils.sendMessage(knockedOutPlayer, "rescued-revived-message");
        MessageUtils.sendTitle(knockedOutPlayer, "rescued-revived-title", "rescued-revived-subtitle", new HashMap<>(), new HashMap<>(), 10, 20 * 3, 10);
        runConfigCommands("console-after-revive-commands", knockedOutPlayer, false);
    }


    public Optional<Player> findNearbyKnockedOutPlayer(Player p) {
        return p.getNearbyEntities(1, 1, 1).stream()
                .filter(entity -> entity instanceof Player)
                .map(entity -> (Player) entity)
                .filter(this::npcExists)
                .findFirst();
    }

    public PlayerInventory getDownedPlayerInventory(Player downedPlayer) {
        return downedPlayer.getInventory();
    }

    // Refresh all NPCs for a player
    public void refreshNPCsForPlayer(Player p) {

        // Perform actions for a new player
        getNPCs().forEach(npc -> {

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

    // Remove all NPCs
    public void endAllKnockouts() {

        getNPCs().forEach(npc -> {
            Player p = npc.getPlayer();

            endKO(p, plugin.getConfig().getBoolean("death-on-end"));

        });

    }

    public void playDamageAnimation(NpcModel npc) {
        versionController.broadcastPacket(npc, PacketType.ANIMATE);
    }

    // Damage a knocked out player
    public void damagePlayerByEntity(NpcModel npc, Entity attacker, double value) {
        Player ko = npc.getPlayer();
        if (attacker instanceof Player p) {
            if (ko.equals(p)) return;
        }

        // Play damage animation attacked knocked out player
        versionController.broadcastPacket(npc, PacketType.ANIMATE);
        // Sound added
        ko.getWorld().playSound(ko.getLocation(), Sound.ENTITY_PLAYER_HURT, 1, 1);

        if (Knockout.getPlugin().getConfig().getBoolean("damage-on-hit")) {
            ko.damage(value);

        } else {
            int timeDecrease = Knockout.getPlugin().getConfig().getInt("time-decrease-on-hit");
            npc.setKnockoutCooldown(npc.getKnockoutCooldown() - timeDecrease);

        }
    }

    private boolean canBeRevivedBy(Player revivingPlayer, Player knockedOutPlayer, Location reviveLocation) {
        return !knockedOutPlayer.isInsideVehicle() && revivingPlayer.isSneaking() && reviveLocation.getBlock().equals(revivingPlayer.getLocation().getBlock());
    }

    public List<NpcModel> getNPCs() {
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

    public NpcModel getNpc(ArmorStand armorStand) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.orElse(null);
    }

    public Entity getKiller(Player p) {
        return getNpc(p).getKiller();
    }
}
