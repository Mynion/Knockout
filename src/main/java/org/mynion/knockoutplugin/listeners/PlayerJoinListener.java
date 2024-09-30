package org.mynion.knockoutplugin.listeners;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.craftbukkit.v1_21_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.mynion.knockoutplugin.Knockout;
import org.mynion.knockoutplugin.utils.NpcManager;

import java.util.EnumSet;
import java.util.List;

public class PlayerJoinListener implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        NpcManager.resetKnockoutEffects(p);

        CraftPlayer cp = (CraftPlayer) p;
        ServerPlayer sp = cp.getHandle();
        ServerLevel level = sp.serverLevel();

        // Perform actions for a new player
        NpcManager.getNPCs().forEach(npc -> {

            ServerPlayer deadBodyPlayer = npc.getDeadBody();
            ServerEntity deadBodyEntity = new ServerEntity(level, deadBodyPlayer, 20, false, null, null);

            // Show dead bodies for a new player
            ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER), List.of(deadBodyPlayer));
            ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(deadBodyPlayer, deadBodyEntity);
            ClientboundSetEntityDataPacket setEntityDataPacket = new ClientboundSetEntityDataPacket(deadBodyPlayer.getId(), deadBodyPlayer.getEntityData().getNonDefaultValues());

            sp.connection.send(infoUpdatePacket);
            sp.connection.send(addEntityPacket);
            sp.connection.send(setEntityDataPacket);

            // Set no collisions for dead bodies and a new player
            PlayerTeam team = new PlayerTeam(new Scoreboard(), "deadBody");
            team.setCollisionRule(Team.CollisionRule.NEVER);
            team.getPlayers().add(npc.getDeadBody().displayName);

            sp.connection.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

            // Hide knocked out players for a new player
            p.hidePlayer(Knockout.getPlugin(), npc.getPlayer());

        });
    }
}
