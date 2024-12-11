package org.mynion.knockoutplugin.listeners;

//import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
//import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
//import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
//import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
//import net.minecraft.server.level.ServerEntity;
//import net.minecraft.server.level.ServerLevel;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.scores.PlayerTeam;
//import net.minecraft.world.scores.Scoreboard;
//import net.minecraft.world.scores.Team;
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

        NpcManager NpcManager = Knockout.getNpcManager();
        NpcManager.resetKnockoutEffects(p);

        NpcManager.playerJoinActions(p);
    }
}
