package dev.cobblesword.skyblock.library.modules.scoreboard;

import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.common.messages.CC;
import dev.cobblesword.skyblock.library.common.task.Sync;
import dev.cobblesword.skyblock.library.modules.Module;
import dev.cobblesword.skyblock.library.modules.servertracker.ServerType;
import dev.cobblesword.skyblock.library.modules.skyislands.models.Island;
import fr.mrmicky.fastboard.FastBoard;
import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ScoreboardModule extends Module implements Listener
{
    private final Map<UUID, FastBoard> boards = new HashMap<>();

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
        }).interval(20).execute();
    }

    @Override
    protected void onDisable() {

    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();

        FastBoard board = new FastBoard(player);

        board.updateTitle(CC.aqua + "Skyopoly");

        this.boards.put(player.getUniqueId(), board);
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
        String placeLine = "No idea";

        Player player = board.getPlayer();
        Island islandPlayerIsOn = SkyblockLibrary.get().getSkyIslandsModule().getIslandPlayerIsOn(player);
        if(islandPlayerIsOn != null)
        {
            placeLine = "Island: " + islandPlayerIsOn.getDisplayName();
        }

        if(SkyblockLibrary.get().getServerType() == ServerType.SPAWN)
        {
            placeLine = "Spawn";
        }

        board.updateLines(
                placeLine,
                "",
                "Players: " + Bukkit.getServer().getOnlinePlayers().size(),
                "Server: " + SkyblockLibrary.get().getServerId()
        );
    }
}
