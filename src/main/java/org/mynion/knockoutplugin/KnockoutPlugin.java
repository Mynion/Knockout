package org.mynion.knockoutplugin;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.mynion.knockoutplugin.commands.DieCommand;
import org.mynion.knockoutplugin.commands.CarryCommand;
import org.mynion.knockoutplugin.commands.DropCommand;
import org.mynion.knockoutplugin.listeners.*;
import org.mynion.knockoutplugin.listeners.cancelled.*;
import org.mynion.knockoutplugin.utils.NpcManager;

public final class KnockoutPlugin extends JavaPlugin {
    private static Plugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
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
        getCommand("pick").setExecutor(new CarryCommand());
        getCommand("throw").setExecutor(new DropCommand());
        getCommand("die").setExecutor(new DieCommand());
    }

    @Override
    public void onDisable() {
        NpcManager.getNPCs().forEach(npc -> {
            if(npc.getVehicle() != null){
                npc.getVehicle().removePotionEffect(PotionEffectType.SLOWNESS);
            }
            NpcManager.forceKill(npc.getPlayer());
        });
    }

    public static Plugin getPlugin() {
        return plugin;
    }
}
