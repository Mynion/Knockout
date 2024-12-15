package org.mynion.knockoutplugin.utils;

import org.mynion.knockoutplugin.Knockout;

import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class NpcManagerFactory {
    public static NpcManager getNpcManager(String version) {
        try {
            String className = "";
            if (List.of("1.21-R0.1-SNAPSHOT", "1.21.1-R0.1-SNAPSHOT", "1.21.2-R0.1-SNAPSHOT").contains(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R1";
            } else if ("1.21.3-R0.1-SNAPSHOT".equals(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R2";
            } else if ("1.21.4-R0.1-SNAPSHOT".equals(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R3";
            } else if (List.of("1.19-R0.1-SNAPSHOT", "1.19.1-R0.1-SNAPSHOT", "1.19.2-R0.1-SNAPSHOT").contains(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_19_R1";
            } else {
                getServer().getPluginManager().disablePlugin(Knockout.getPlugin());
                System.out.println("Unsupported server version: " + version);
            }

            Class<?> clazz = Class.forName(className);
            return (NpcManager) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create NpcManager for server version: " + version);
        }
    }
}
