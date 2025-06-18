package org.mynion.knockoutplugin.utils;

import org.mynion.knockoutplugin.Knockout;

import static org.bukkit.Bukkit.getServer;

public class VersionControllerFactory {
    public static VersionController getNmsController(String version) {
        try {
            String className = "";
            switch (version){
                case "1.21-R0.1-SNAPSHOT":
                case "1.21.1-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_21_1";
                    break;
                case "1.21.2-R0.1-SNAPSHOT":
                case "1.21.3-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_21_3";
                    break;
                case "1.21.4-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_21_4";
                    break;
                case "1.21.5-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_21_5";
                    break;
                case "1.21.6-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_21_6";
                    break;
                case "1.20-R0.1-SNAPSHOT":
                case "1.20.1-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_20_1";
                    break;
                case "1.20.2-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_20_2";
                    break;
                case "1.20.3-R0.1-SNAPSHOT":
                case "1.20.4-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_20_4";
                    break;
                case "1.20.5-R0.1-SNAPSHOT":
                case "1.20.6-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_20_6";
                    break;
                case "1.19-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_19";
                    break;
                case "1.19.1-R0.1-SNAPSHOT":
                case "1.19.2-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_19_2";
                    break;
                case "1.19.3-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_19_3";
                    break;
                case "1.19.4-R0.1-SNAPSHOT":
                    className = "org.mynion.knockoutplugin.utils.VersionController_v1_19_4";
                    break;
                default:
                    getServer().getPluginManager().disablePlugin(Knockout.getPlugin());
                    System.out.println("Unsupported server version: " + version);
            }
            Class<?> clazz = Class.forName(className);
            return (VersionController) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            throw new IllegalArgumentException("Could not create VersionController for server version: " + version);
        }
    }
}
