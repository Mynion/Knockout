package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mynion.knockoutplugin.enums.PacketType;
import org.mynion.knockoutplugin.enums.PotionType;

import java.util.UUID;

public interface VersionController {
    NpcModel createNpc(Player player, ArmorStand armorStand, GameMode previousGameMode, @Nullable Entity damager);

    void setMaxHealth(Player p);

    void setXpDelay(Player p, int delay);

    void setCollisions(Player p, boolean on);

    void addPotionEffect(LivingEntity p, @NotNull PotionType type, int duration, int amplifier, boolean ambient, boolean particles);

    void removePotionEffect(LivingEntity p, @NotNull PotionType type);

    int getPotionAmplifier(LivingEntity p, @NotNull PotionType type);

    void teleportMannequin(NpcModel npc, double x, double y, double z);

    void teleportHologram(NpcModel npc, double x, double y, double z);

    void broadcastPacket(NpcModel npc, PacketType packetType);

    void sendPacket(Player receiver, NpcModel npc, PacketType packetType);

    void setAbleToJump(Player p, boolean able);

    void removeParrotFromShoulder(Player p, boolean rightShoulder);

    UUID getMannequinUUID(NpcModel npc);
}
