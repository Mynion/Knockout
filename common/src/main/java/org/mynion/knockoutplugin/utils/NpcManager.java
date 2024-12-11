package org.mynion.knockoutplugin.utils;

import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.mynion.knockoutplugin.Knockout;

import java.util.*;

public interface NpcManager {
    //List<Npc> NPCs = new ArrayList<>();
    Plugin plugin = Knockout.getPlugin();

    void knockoutPlayer(Player p);

    //ServerPlayer createDeadBody(Player p);

    void resetKnockout(Player p);

    // Resets knockout and kills the player
    void forceKill(Player p);

    void applyKnockoutEffects(Player p);

    void startTimer(Player p);

    void resetKnockoutEffects(Player p);

    // Teleporting body and hologram to the player while knocked out
    //void teleportBody(Npc npc);

    // Set no collisions for dead body
    //void setNoCollisions(Npc npc);

    // Start carrying a knocked out player
    void startCarrying(Player knockedOutPlayer, Player vehicle);
    // Stop carrying a knocked out player
    void stopCarrying(Player knockedOutPlayer, Player vehicle);

    // Tracking a knocked out player vehicle to prevent dismount
    void trackVehicle(Player knockedOutPlayer);

    void startReviving(Player revivingPlayer, Player knockedOutPlayer);

    void reviveNow(Player revivingPlayer, Player knockedOutPlayer);

    boolean canBeRevivedBy(Player revivingPlayer, Player knockedOutPlayer, Location reviveLocation);

    //void broadcastPacket(Packet<?> packet);

    //List<Npc> getNPCs();

    boolean npcExists(Player player);

    boolean npcExists(ArmorStand armorStand);

    //boolean npcExists(ServerPlayer deadBody);

    //Npc getNpc(Player player);

    //Npc getNpc(ArmorStand armorStand);

    //Npc getNpc(ServerPlayer deadBody);

    void playerJoinActions(Player p);
    void hurtAnimation(Player p);
    void removeKOPlayers();
    void damage(ArmorStand koArmorStand, Entity attacker, double value);
}