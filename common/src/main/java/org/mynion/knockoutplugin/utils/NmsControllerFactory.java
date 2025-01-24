package org.mynion.knockoutplugin.utils;

import org.mynion.knockoutplugin.Knockout;

import static org.bukkit.Bukkit.getServer;

public class NmsControllerFactory {
    public static NmsController getNmsController(String version) {
        try {
            String className = "";
            switch (version){
                case "1.21-R0.1-SNAPSHOT":
                case "1.21.1-R0.1-SNAPSHOT":
                case "1.21.2-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R1";
                    break;
                case "1.21.3-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R2";
                    break;
                case "1.21.4-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R3";
                    break;
                case "1.20.6-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_20_R4";
                    break;
                case "1.19-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NmsController_v1_19";
                    break;
                case "1.19.1-R0.1-SNAPSHOT":
                case "1.19.2-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_19_1";
                    break;
                case "1.19.3-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_19_3";
                    break;
                case "1.19.4-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.NpcManager_v1_19_4";
                    break;
                default:
                    getServer().getPluginManager().disablePlugin(Knockout.getPlugin());
                    System.out.println("Unsupported server version: " + version);
            }
            Class<?> clazz = Class.forName(className);
            return (NmsController) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create NmcController for server version: " + version);
        }
    }
}
