package org.mynion.knockoutplugin.utils;

public class NpcManagerFactory {
    public static NpcManager getNpcManager(String version) {
        try {
            String className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R1";
            if ("v1_21_R2".equals(version)) {
                className = "org.mynion.knockoutplugin.utils.NpcManager_v1_21_R2";
            }

            Class<?> clazz = Class.forName(className);
            return (NpcManager) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to create NpcManager for version: " + version);
        }
    }
}
