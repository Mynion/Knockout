package org.mynion.knockoutplugin.compatibility;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import com.sk89q.worldguard.session.SessionManager;
import org.bukkit.entity.Player;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;
import org.mynion.knockoutplugin.utils.NpcModel;

import java.util.UUID;

public class WorldGuardAdapter {
    public static boolean canEnterRegion(Player p) {
        NpcManager npcManager = Knockout.getNpcManager();
        if (!npcManager.npcExists(p)) return true;

        NpcModel npc = npcManager.getNpc(p);

        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(p);
        Location loc = new Location(localPlayer.getWorld(), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        UUID mannequinUUID = npcManager.getVersionController().getMannequinUUID(npc);

        query.getApplicableRegions(loc).getRegions().stream()
                .filter(r -> !r.getMembers().contains(mannequinUUID))
                .forEach(r -> r.getMembers().addPlayer(mannequinUUID));

        SessionManager sessionManager = WorldGuard.getInstance().getPlatform().getSessionManager();

        LocalPlayer localVehicle = null;
        if(npc.getVehicle() != null){
            localVehicle = WorldGuardPlugin.inst().wrapPlayer(npc.getVehicle());
        }

        if ((!query.testState(loc, localPlayer, Flags.ENTRY) && !sessionManager.hasBypass(localPlayer, localPlayer.getWorld())) | (localVehicle != null && !query.testState(loc, localVehicle, Flags.ENTRY) && !sessionManager.hasBypass(localVehicle, localVehicle.getWorld()))) {
            // Can't entry
            npcManager.dropPlayer(p, npc.getVehicle());
            p.teleport(npc.getLastLocation());
            return false;
        }

        return true;
    }

    public static void removeMannequinFromRegions(NpcModel npc) {
        NpcManager npcManager = Knockout.getNpcManager();
        Player p = npc.getPlayer();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();

        UUID mannequinUUID = npcManager.getVersionController().getMannequinUUID(npc);
        RegionManager manager = container.get(BukkitAdapter.adapt(p.getWorld()));
        manager.getRegions().forEach((s, r) -> r.getMembers().removePlayer(mannequinUUID));

    }
}
