package org.mynion.knockoutplugin.compatibility;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class TabAdapter {
    public static void setCollisionRule(Player p, boolean rule) {
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TAB");
        if (tabPlugin == null) return;
        if (!tabPlugin.isEnabled()) return;

        TabPlayer tp = TabAPI.getInstance().getPlayer(p.getName());
        try {
            TabAPI.getInstance().getNameTagManager().setCollisionRule(tp, rule);
        } catch (NullPointerException ignored) {
        }

    }
}
