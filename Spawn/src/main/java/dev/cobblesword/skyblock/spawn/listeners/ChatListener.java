package dev.cobblesword.skyblock.spawn.listeners;

import dev.cobblesword.skyblock.saga.SkyblockApi;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class ChatListener implements Listener
{
    private SkyblockApi skyblockApi;

    public ChatListener(SkyblockApi skyblockApi)
    {
        this.skyblockApi = skyblockApi;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        player.setPlayerListHeaderFooter("Skyblock", "Server: spawn-01");
    }
}
