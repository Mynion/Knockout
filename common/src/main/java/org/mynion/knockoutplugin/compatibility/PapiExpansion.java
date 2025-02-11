package org.mynion.knockoutplugin.compatibility;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

public class PapiExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "knockout";
    }

    @Override
    public @NotNull String getAuthor() {
        return "mynion";
    }

    @Override
    public @NotNull String getVersion() {
        return Knockout.getPlugin().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        NpcManager npcManager = Knockout.getNpcManager();

        if (params.equals("knocked_out")) {
            if (player instanceof Player p) {
                return npcManager.npcExists(p) ? "true" : "false";
            }
            return "false";
        }

        if (params.equals("time_left")) {
            if (player instanceof Player p && npcManager.npcExists(p)) {
                return String.valueOf(npcManager.getNpc(p).getKnockoutCooldown());
            }
            return "";
        }

        if (params.equals("killer")) {
            if (player instanceof Player p && npcManager.npcExists(p)) {
                return npcManager.getNpc(p).getKiller().getName();
            }
            return "";
        }

        if (params.equals("is_being_revived")) {
            if (player instanceof Player p && npcManager.npcExists(p)) {
                return npcManager.getNpc(p).isBeingRevived() ? "true" : "false";
            }
            return "false";
        }

        if (params.equals("vehicle")) {
            if (player instanceof Player p && npcManager.npcExists(p)) {
                Player vehicle = npcManager.getNpc(p).getVehicle();
                return vehicle != null ? vehicle.getName() : "";
            }
            return "";
        }

        if (params.equals("knockouts")) {
            return String.valueOf(npcManager.getNPCs().size());
        }

        return null;
    }
}
