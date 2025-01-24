package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.compatibility.TabAdapter;

import java.util.*;

public class KnockoutLogic implements NpcManager {
    private NmsController nmsController;
    public KnockoutLogic() {
        nmsController = Knockout.getNmsController();
    }

    private static final List<NpcModel> NPCs = new ArrayList<>();
    private static final Plugin plugin = Knockout.getPlugin();

    public void knockoutPlayer(Player p, @Nullable EntityDamageEvent.DamageCause koCause, @Nullable Entity damager) {

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
        NpcModel npc = nmsController.createNpc(p, armorStand, playerGameMode, koCause, damager);
        NPCs.add(npc);

        // Broadcast dead body info packets
//        ClientboundPlayerInfoPacket infoUpdatePacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, List.of(deadBodyPlayer));
//        ClientboundAddPlayerPacket addEntityPacket = new ClientboundAddPlayerPacket(deadBodyPlayer);
//        ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(deadBodyPlayer.getId(), deadBodyPlayer.getEntityData(), true);
//        broadcastPacket(infoUpdatePacket);
//        broadcastPacket(addEntityPacket);
//        broadcastPacket(setEntityDataPacket);
        nmsController.broadcastPacket(npc, PacketType.INFO_UPDATE);
        nmsController.broadcastPacket(npc, PacketType.ADD_ENTITY);
        nmsController.broadcastPacket(npc, PacketType.SET_ENTITY_DATA);


//        List<Pair<EquipmentSlot, ItemStack>> items = List.of(
//                Pair.of(EquipmentSlot.HEAD, sp.getItemBySlot(EquipmentSlot.HEAD)),
//                Pair.of(EquipmentSlot.CHEST, sp.getItemBySlot(EquipmentSlot.CHEST)),
//                Pair.of(EquipmentSlot.LEGS, sp.getItemBySlot(EquipmentSlot.LEGS)),
//                Pair.of(EquipmentSlot.FEET, sp.getItemBySlot(EquipmentSlot.FEET)),
//                Pair.of(EquipmentSlot.MAINHAND, sp.getItemBySlot(EquipmentSlot.MAINHAND)),
//                Pair.of(EquipmentSlot.OFFHAND, sp.getItemBySlot(EquipmentSlot.OFFHAND))
//        );
//        ClientboundSetEquipmentPacket packet = new ClientboundSetEquipmentPacket(deadBodyPlayer.getId(), items);
//        broadcastPacket(packet);
        nmsController.broadcastPacket(npc, PacketType.SET_EQUIPMENT);

//        nmsController.setNoCollisions(npc);
        nmsController.broadcastPacket(npc, PacketType.COLLISIONS_OFF);


        TabAdapter.setCollisionRule(npc.getPlayer(), false);
        teleportBody(npc);
        startTimer(p);

        ChatUtils.sendMessage(p, "knockout-message");
    }


    // Resets knockout but does not kill the player
    public void resetKnockout(Player p) {
        NpcModel npc = getNpc(p);
        GameMode previousGameMode = npc.getPreviousGameMode();

        // Reset knockout effects
        resetKnockoutEffects(p);

        // Remove dead body
//        ClientboundPlayerInfoPacket removeNpcPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, getNpc(p).getDeadBody());
//        ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(npc.getDeadBody().getId());
//        broadcastPacket(removeNpcPacket);
//        broadcastPacket(removeEntityPacket);
        nmsController.broadcastPacket(npc, PacketType.INFO_REMOVE);
        nmsController.broadcastPacket(npc, PacketType.REMOVE_ENTITY);


        // Remove hologram
        npc.getArmorStand().remove();

        // Remove npc from npc list
        NPCs.remove(npc);

        // Set previous gamemode
        p.setGameMode(previousGameMode);

    }

    // Resets knockout and kills the player
    public void forceKill(Player p) {
        if(getDamager(p) != null){
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
        nmsController.addPotionEffect(p, PotionType.JUMP, 999999999, 200, false, false);
        if (plugin.getConfig().getBoolean("knockout-blindness")) {
            PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, 999999999, 1, false, false);
            p.addPotionEffect(blindness);
        }

        // Set player health to max
        nmsController.setMaxHealth(p);

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

    private void startTimer(Player p) {
        int seconds = plugin.getConfig().getInt("knockout-time");
        if (seconds < 0) {
            seconds = 60;
        }
        NpcModel npc = getNpc(p);
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
        nmsController.removePotionEffect(p, PotionType.JUMP);

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
//        ServerPlayer sp = ((CraftPlayer) p).getHandle();
//
//        PlayerTeam team = new PlayerTeam(new Scoreboard(), "player");
//        if (sp.getTeam() != null) {
//            team.setCollisionRule(sp.getTeam().getCollisionRule());
//        } else {
//            team.setCollisionRule(Team.CollisionRule.ALWAYS);
//        }
//        team.getPlayers().add(p.getName());
//
//        broadcastPacket(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
        nmsController.broadcastPacket(getNpc(p), PacketType.COLLISIONS_ON);

        TabAdapter.setCollisionRule(p, true);
    }

    // Teleporting body and hologram to the player while knocked out
    private void teleportBody(NpcModel npc) {
        Player p = npc.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (npcExists(p)) {
//                    ClientboundTeleportEntityPacket teleportBodyPacket = new ClientboundTeleportEntityPacket(deadBody);
//                    broadcastPacket(teleportBodyPacket);
                    nmsController.broadcastPacket(npc, PacketType.TELEPORT);
                    if (p.isInsideVehicle()) {
                        nmsController.teleportBody(npc, p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                        nmsController.teleportHologram(npc, p.getLocation().getX(), p.getLocation().getY() + 0.6, p.getLocation().getZ());
                    } else {
                        nmsController.teleportBody(npc, p.getLocation().getX(), p.getLocation().getY() + -0.2, p.getLocation().getZ());
                        nmsController.teleportHologram(npc, p.getLocation().getX(), p.getLocation().getY() + -0.2, p.getLocation().getZ());
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
        int slowness_amplifier = nmsController.getPotionAmplifier(vehicle, PotionType.SLOW);
        if (plugin.getConfig().getInt("slowness-amplifier") == slowness_amplifier) {
            nmsController.removePotionEffect(vehicle, PotionType.SLOW);
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
                                nmsController.addPotionEffect(currentVehicle, PotionType.SLOW, 20 * 2, plugin.getConfig().getInt("slowness-amplifier"), false, false);
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
                        nmsController.setXpDelay(revivingPlayer, -1);

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
                        nmsController.setXpDelay(revivingPlayer, 0);
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
        nmsController.setXpDelay(revivingPlayer, 0);
    }

    public void playerJoinActions(Player p) {
        //ServerPlayer sp = ((CraftPlayer) p).getHandle();

        // Perform actions for a new player
        getNPCs().forEach(npc -> {

            //ServerPlayer deadBodyPlayer = npc.getDeadBody();

            // Show dead bodies for a new player
//            ClientboundPlayerInfoPacket infoUpdatePacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, List.of(deadBodyPlayer));
//            ClientboundAddPlayerPacket addEntityPacket = new ClientboundAddPlayerPacket(deadBodyPlayer);
//            ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(deadBodyPlayer.getId(), deadBodyPlayer.getEntityData(), true);
//
//            sp.connection.send(infoUpdatePacket);
//            sp.connection.send(addEntityPacket);
//            sp.connection.send(setEntityDataPacket);

            nmsController.sendPacket(p, npc, PacketType.INFO_UPDATE);
            nmsController.sendPacket(p, npc, PacketType.ADD_ENTITY);
            nmsController.sendPacket(p, npc, PacketType.SET_ENTITY_DATA);

            // Set no collisions for dead bodies and a new player
//            PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
//            team.setCollisionRule(Team.CollisionRule.NEVER);
//            team.getPlayers().add(npc.getDeadBody().displayName);
//
//            sp.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));
            nmsController.sendPacket(p, npc, PacketType.COLLISIONS_OFF);

            // Hide knocked out players for a new player
            p.hidePlayer(Knockout.getPlugin(), npc.getPlayer());

        });
    }

    private void hurtAnimation(Player p) {

        // Damage attacked knocked out player
//        ClientboundAnimatePacket packet = new ClientboundAnimatePacket(getNpc(p).getDeadBody(), 1);
//        broadcastPacket(packet);
        nmsController.broadcastPacket(getNpc(p), PacketType.ANIMATE);

    }

    public void removeKOPlayers() {
        // Remove all NPCs
        getNPCs().forEach(npc -> {

            if (npc.getVehicle() != null) {
                nmsController.removePotionEffect(npc.getVehicle(), PotionType.SLOW);
            }

//            ClientboundPlayerInfoPacket removeNpcPacket = new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc.getDeadBody());
//            ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(npc.getDeadBody().getId());
//            broadcastPacket(removeNpcPacket);
//            broadcastPacket(removeEntityPacket);
            nmsController.broadcastPacket(npc, PacketType.INFO_REMOVE);
            nmsController.broadcastPacket(npc, PacketType.REMOVE_ENTITY);

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

    private NpcModel getNpc(Player player) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getPlayer().equals(player)).findFirst();
        return matchingNpc.orElse(null);
    }

    private NpcModel getNpc(ArmorStand armorStand) {
        Optional<NpcModel> matchingNpc = NPCs.stream().filter(npc -> npc.getArmorStand().equals(armorStand)).findFirst();
        return matchingNpc.orElse(null);
    }

    @Override
    public EntityDamageEvent.DamageCause getKOCause(Player p) {
        return getNpc(p).getKoCause();
    }

    @Override
    public Entity getDamager(Player p) {
        return getNpc(p).getDamager();
    }
}
