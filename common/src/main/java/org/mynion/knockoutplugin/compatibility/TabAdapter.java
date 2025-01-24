package org.mynion.knockoutplugin.compatibility;

import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.mynion.knockoutplugin.Knockout;

public class TabAdapter {
    public static void setCollisionRule(Player p, boolean rule) {
        Plugin tabPlugin = Knockout.getPlugin().getServer().getPluginManager().getPlugin("TAB");
        if (tabPlugin == null) return;
        if (!tabPlugin.isEnabled()) return;

        TabPlayer tp = TabAPI.getInstance().getPlayer(p.getName());
        try {
            TabAPI.getInstance().getNameTagManager().setCollisionRule(tp, rule);
        } catch (NullPointerException ignored) {
        }

    }
}
