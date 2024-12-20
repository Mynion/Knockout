package org.mynion.knockoutplugin.utils;

import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.mynion.knockoutplugin.Knockout;

public interface NpcManager {
    Plugin plugin = Knockout.getPlugin();

    void knockoutPlayer(Player p);

    void resetKnockout(Player p);

    void forceKill(Player p);

    void resetKnockoutEffects(Player p);

    void startCarrying(Player knockedOutPlayer, Player vehicle);

    void stopCarrying(Player knockedOutPlayer, Player vehicle);

    void startReviving(Player revivingPlayer, Player knockedOutPlayer);

    boolean npcExists(Player player);

    boolean npcExists(ArmorStand armorStand);

    void playerJoinActions(Player p);

    void removeKOPlayers();

    void damageKOPlayer(ArmorStand koArmorStand, Entity attacker, double value);
}