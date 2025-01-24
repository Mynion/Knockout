package org.mynion.knockoutplugin.utils;

import jline.internal.Nullable;
import org.bukkit.GameMode;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

public interface NmsController {
    void setMaxHealth(Player p);
    void setXpDelay(Player p, int delay);
    void teleportBody(NpcModel npc, double x, double y, double z);
    void teleportHologram(NpcModel npc, double x, double y, double z);
    NpcModel createNpc(Player player, ArmorStand armorStand, GameMode previousGameMode, @Nullable EntityDamageEvent.DamageCause damageCause, @Nullable Entity damager);
    void addPotionEffect(LivingEntity p, @NotNull PotionType type, int duration, int amplifier, boolean ambient, boolean particles);
    void removePotionEffect(LivingEntity p, @NotNull PotionType type);
    int getPotionAmplifier(LivingEntity p, @NotNull PotionType type);
    void sendPacket(Player receiver, NpcModel npc, PacketType packetType);
    void broadcastPacket(NpcModel npc, PacketType packetType);
}
