package dev.cobblesword.skyblock.library.modules.scoreboard;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.CC;
import dev.cobblesword.skyblock.library.common.messages.Messages;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.models.Island;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardModule extends Module implements Listener
{
    private final Map<UUID, FastBoard> boards = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
    
    // Scoreboard styling
    private static final String TITLE = CC.bAqua + "★ " + CC.bWhite + "SKYOPOLY" + CC.bAqua + " ★";
    private static final String LINE = CC.darkGray + "┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄┄";
    private static final String BULLET = CC.aqua + "▸ ";

    public ScoreboardModule() {
        super("Scoreboard");
    }

    @Override
    protected void onEnable() {
        this.registerListener(this);

        Sync.get().run(() -> {
            for (FastBoard board : this.boards.values()) {
                updateBoard(board);
            }
        }).interval(10).execute(); // Update every 0.5s for smoother feel
    }

    @Override
    protected void onDisable() {

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        FastBoard board = new FastBoard(player);
        board.updateTitle(TITLE);

        this.boards.put(player.getUniqueId(), board);
        
        // Show welcome title
        Bukkit.getScheduler().runTaskLater(SkyblockLibrary.get().getPlugin(), () -> {
            Messages.welcome(player);
        }, 10L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player player = e.getPlayer();

        FastBoard board = this.boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    @EventHandler
    public void onKick(PlayerKickEvent e) {
        Player player = e.getPlayer();

        FastBoard board = this.boards.remove(player.getUniqueId());

        if (board != null) {
            board.delete();
        }
    }

    public FastBoard getScoreboard(UUID playerId)
    {
        return this.boards.get(playerId);
    }

    public FastBoard getScoreboard(Player player)
    {
        return this.boards.get(player.getUniqueId());
    }

    private void updateBoard(FastBoard board)
    {
        Player player = board.getPlayer();
        if (player == null || !player.isOnline()) return;
        
        String locationLine;
        String locationIcon;
        
        Island islandPlayerIsOn = SkyblockLibrary.get().getSkyIslandsModule().getIslandPlayerIsOn(player);
        if(islandPlayerIsOn != null)
        {
            locationIcon = CC.green + "⛰";
            locationLine = CC.white + islandPlayerIsOn.getDisplayName();
        }
        else if(SkyblockLibrary.get().getServerType() == ServerType.SPAWN)
        {
            locationIcon = CC.gold + "⚑";
            locationLine = CC.white + "Spawn Lobby";
        }
        else
        {
            locationIcon = CC.gray + "◆";
            locationLine = CC.gray + "Unknown";
        }

        int totalPlayers = Bukkit.getServer().getOnlinePlayers().size();
        String date = dateFormat.format(new Date());
        
        board.updateLines(
            CC.gray + date,
            "",
            BULLET + CC.white + "Location",
            "  " + locationIcon + " " + locationLine,
            "",
            BULLET + CC.white + "Players",
            "  " + CC.aqua + totalPlayers + CC.gray + " online",
            "",
            LINE,
            CC.gold + "play.skyopoly.net"
        );
    }
}
