package org.mynion.knockoutplugin.listeners;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PlayerDeathListener implements Listener {

    // Reset knockout on player death caused for any reason, for example by different plugin
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        NpcManager NpcManager = Knockout.getNpcManager();
        Player p = e.getEntity();

        // Check if the player is knocked out
        if (NpcManager.npcExists(p)) {

            // End knockout
            NpcManager.endKnockout(p, false);

            // Custom spawn location for 1.20.6+ versions
        } else if (Knockout.getVersion().startsWith("1.2")) {
            FileConfiguration config = Knockout.getPlugin().getConfig();
            double x = config.getDouble("custom-location.x");
            double y = config.getDouble("custom-location.y");
            double z = config.getDouble("custom-location.z");
            World world = Knockout.getPlugin().getServer().getWorld(config.getString("custom-location.world-name"));
            if (world == null) {
                world = p.getWorld();
                if (config.getString("custom-location.world-name") != null) {
                    Knockout.getPlugin().getLogger().warning("World " + config.getString("custom-location.world-name") + " not found. Using player's world instead.");
                }
            }
            Location respawnLocation = new Location(world, x, y, z);
            if (config.getBoolean("respawn-in-custom-location")) {
                p.setRespawnLocation(respawnLocation, true);
            } else if (respawnLocation.add(new Vector(0.5, 0.1, 0.5)).equals(p.getRespawnLocation())) {
                p.setRespawnLocation(null);
            }
        }

        NpcManager.runConfigCommands("ConsoleAfterCommands", p, false);
    }
}
