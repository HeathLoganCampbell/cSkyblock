package dev.cobblesword.skyblock.library.modules.playersync.listeners;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.library.SkyblockLibrary;
import dev.cobblesword.skyblock.library.modules.playersync.InventoryShareUtil;
import dev.cobblesword.skyblock.library.modules.playersync.PlayerSyncModule;
import dev.cobblesword.skyblock.library.modules.playersync.database.PlayerSyncRepository;
import dev.cobblesword.skyblock.protocol.requests.RePullInventoryRequest;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerSyncListener implements Listener
{
    private PlayerSyncRepository repository;

    private IExchange exchange;

    private PlayerSyncModule playerSyncModule;

    public PlayerSyncListener(PlayerSyncRepository repository, IExchange exchange, PlayerSyncModule playerSyncModule) {
        this.repository = repository;
        this.exchange = exchange;
        this.playerSyncModule = playerSyncModule;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e)
    {
        Player player = e.getPlayer();
        System.out.println("PlayerSync] Join " + System.currentTimeMillis());;

        this.playerSyncModule.applyPlayerSync(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e)
    {
        Player player = e.getPlayer();
        System.out.println("PlayerSync] quit " + System.currentTimeMillis());

        playerSyncModule.storePlayerSync(player);
        playerSyncModule.triggerRePull(player.getUniqueId());
    }

    @EventHandler
    public void onKick(PlayerKickEvent e)
    {
        Player player = e.getPlayer();
        System.out.println("PlayerSync] kick " + System.currentTimeMillis());

        playerSyncModule.storePlayerSync(player);
        playerSyncModule.triggerRePull(player.getUniqueId());
    }
}
