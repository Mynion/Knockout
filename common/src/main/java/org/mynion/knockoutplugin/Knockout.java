package org.mynion.knockoutplugin;

import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.mynion.knockoutplugin.commands.DieCommand;
import org.mynion.knockoutplugin.commands.CarryCommand;
import org.mynion.knockoutplugin.commands.DropCommand;
import org.mynion.knockoutplugin.commands.KnockoutCommand;
import org.mynion.knockoutplugin.listeners.*;
import org.mynion.knockoutplugin.listeners.cancelled.*;
import org.mynion.knockoutplugin.utils.NpcManager;
import org.mynion.knockoutplugin.utils.VersionAdapter;
import org.mynion.knockoutplugin.utils.VersionAdapterFactory;

import java.lang.reflect.Field;
import java.util.List;

public final class Knockout extends JavaPlugin {
    private static Plugin plugin;
    private static VersionAdapter versionAdapter;

    @Override
    public void onEnable() {

        String version = Bukkit.getServer().getClass().getPackageName().split("\\.")[3];
        System.out.println("===================================================="+version);
        switch (version) {
            case "v1_21_R1":
                versionAdapter = VersionAdapterFactory.getVersionAdapter("v1_21_R1");
                break;
            case "v1_21_R2":
                versionAdapter = VersionAdapterFactory.getVersionAdapter("v1_21_R2");
                break;
            default:
                System.out.println("Unsupported server version" + version);
                getServer().getPluginManager().disablePlugin(this);
                return;
        }

        plugin = this;
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        loadAliases();
        getServer().getPluginManager().registerEvents(new PlayerDamageListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerSneakListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new GameModeListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractEntityListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
        getServer().getPluginManager().registerEvents(new RegainHealthListener(), this);
        getServer().getPluginManager().registerEvents(new EntityTargetListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
        getServer().getPluginManager().registerEvents(new DropItemListener(), this);
        getServer().getPluginManager().registerEvents(new PotionEffectListener(), this);
        getServer().getPluginManager().registerEvents(new PickupItemListener(), this);
        getServer().getPluginManager().registerEvents(new PickupArrowListener(), this);
        getServer().getPluginManager().registerEvents(new EditBookListener(), this);
        getServer().getPluginManager().registerEvents(new SwapHandItemsListener(), this);
        getServer().getPluginManager().registerEvents(new ExpChangeListener(), this);
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
        getCommand("carry").setExecutor(new CarryCommand());
        getCommand("drop").setExecutor(new DropCommand());
        getCommand("die").setExecutor(new DieCommand());
        getCommand("knockout").setExecutor(new KnockoutCommand());
    }

    @Override
    public void onDisable() {

        // Remove all NPCs
        NpcManager.getNPCs().forEach(npc -> {

            if (npc.getVehicle() != null) {
                npc.getVehicle().removePotionEffect(PotionEffectType.SLOWNESS);
            }

            ClientboundPlayerInfoRemovePacket removeNpcPacket = new ClientboundPlayerInfoRemovePacket(List.of(npc.getDeadBody().getGameProfile().getId()));
            ClientboundRemoveEntitiesPacket removeEntityPacket = new ClientboundRemoveEntitiesPacket(npc.getDeadBody().getId());
            NpcManager.broadcastPacket(removeNpcPacket);
            NpcManager.broadcastPacket(removeEntityPacket);

            npc.getArmorStand().remove();
            npc.getPlayer().setHealth(0);
            npc.getPlayer().setGameMode(npc.getPreviousGameMode());
            NpcManager.resetKnockoutEffects(npc.getPlayer());

        });
    }

    private void loadAliases() {
        try {
            final Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");

            commandMapField.setAccessible(true);
            CommandMap commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());

            getConfig().getStringList("aliases.carry").forEach(alias -> commandMap.register(alias, "knockoutplugin", getCommand("carry")));
            getConfig().getStringList("aliases.drop").forEach(alias -> commandMap.register(alias, "knockoutplugin", getCommand("drop")));
            getConfig().getStringList("aliases.die").forEach(alias -> commandMap.register(alias, "knockoutplugin", getCommand("die")));
            getConfig().getStringList("aliases.knockout").forEach(alias -> commandMap.register(alias, "knockoutplugin", getCommand("knockout")));

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Plugin getPlugin() {
        return plugin;
    }

    public static VersionAdapter getVersionAdapter() {
        return versionAdapter;
    }
}
