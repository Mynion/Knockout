package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

// Npc is a class that represents a knocked out player in the game
// It contains the player that is knocked out, the dead body of the player and the armor stand that displays text above the dead body

public class Npc extends NpcModel{
    private final ServerPlayer mannequin;

    public Npc(Player player, ServerPlayer mannequin, ArmorStand armorStand, GameMode previousGameMode, @Nullable Entity damager) {
        super(player, armorStand, previousGameMode, damager);
        this.mannequin = mannequin;
    }
    public ServerPlayer getMannequin() {
        return mannequin;
    }
}
