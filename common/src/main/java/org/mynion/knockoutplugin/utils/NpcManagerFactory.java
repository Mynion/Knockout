package org.mynion.knockoutplugin.utils;

import org.mynion.knockoutplugin.Knockout;

import static org.bukkit.Bukkit.getServer;

public class NpcManagerFactory {
    public static NpcManager getNpcManager(String version) {
        try {
            String className = "";
            if ("v1_21_R1".equals(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R1";
            } else if ("v1_21_R2".equals(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R2";
            } else if ("v1_21_R3".equals(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R3";
            } else {
                getServer().getPluginManager().disablePlugin(Knockout.getPlugin());
                System.out.println("Unsupported server version: " + version);
            }

            Class<?> clazz = Class.forName(className);
            return (NpcManager) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Could not create NpcManager for server version: " + version);
        }
    }
}
