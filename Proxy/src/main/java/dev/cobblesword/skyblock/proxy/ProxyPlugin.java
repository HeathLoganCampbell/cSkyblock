package dev.cobblesword.skyblock.proxy;

import dev.cobblesword.exchange.impl.redis.RedisExchange;
import dev.cobblesword.exchange.impl.redis.RedisSettings;
import dev.cobblesword.skyblock.proxy.islands.IslandManager;
import dev.cobblesword.skyblock.proxy.listeners.PlayerListener;
import dev.cobblesword.skyblock.proxy.remoteinv.PlayerJoinNetworkListener;
import dev.cobblesword.skyblock.proxy.servers.ServerManager;
import dev.cobblesword.skyblock.saga.SkyblockApi;
import net.md_5.bungee.api.plugin.Plugin;

public class ProxyPlugin extends Plugin
{
    private RedisExchange exchange;

    @Override
    public void onEnable()
    {
        getLogger().info("Yay! It loads!");

        RedisSettings redisSettings = new RedisSettings("localhost");
        exchange = new RedisExchange(redisSettings);
        SkyblockApi skyblockApi = new SkyblockApi(exchange, "Proxy-01", true);

        ServerManager serverManager = new ServerManager(exchange, this);
        IslandManager islandManager = new IslandManager(serverManager, skyblockApi, getProxy());

        getProxy().getPluginManager().registerListener(this, new PlayerJoinNetworkListener(this.exchange));
        getProxy().getPluginManager().registerListener(this, new PlayerListener());
    }

    @Override
    public void onDisable()
    {
        exchange.stop();
    }
}
