package org.mynion.knockoutplugin.utils;

public class VersionAdapterFactory {

    public static VersionAdapter getVersionAdapter(String version) {
        try {
            String className = "org.mynioon.v1_21_R1";
            if ("v1_21_R2".equals(version)) {
                className = "org.mynioon.v1_21_R2";
            }

            Class<?> clazz = Class.forName(className);
            return (VersionAdapter) clazz.getDeclaredConstructor().newInstance();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Unable to create VersionAdapter for version: " + version);
        }
    }
}