package dev.cobblesword.skyblock.proxy;

import dev.cobblesword.exchange.impl.redis.RedisExchange;
import dev.cobblesword.exchange.impl.redis.RedisSettings;
import dev.cobblesword.skyblock.proxy.controller.ProxyControllerClient;
import dev.cobblesword.skyblock.proxy.islands.IslandManager;
import dev.cobblesword.skyblock.proxy.listeners.PlayerListener;
import dev.cobblesword.skyblock.proxy.remoteinv.PlayerJoinNetworkListener;
import dev.cobblesword.skyblock.proxy.servers.ServerManager;
import net.md_5.bungee.api.plugin.Plugin;

public class ProxyPlugin extends Plugin
{
    private RedisExchange exchange;
    private ProxyControllerClient controller;

    private static final String CONTROLLER_URL = "http://localhost:8080";

    @Override
    public void onEnable()
    {
        getLogger().info("Starting SkyblockProxy...");

        // Redis for pub/sub messaging
        RedisSettings redisSettings = new RedisSettings("localhost");
        exchange = new RedisExchange(redisSettings);

        // Controller client for HTTP API
        controller = new ProxyControllerClient(CONTROLLER_URL);

        // Server manager - handles dynamic server registration via KeepAlive
        new ServerManager(exchange, this);

        // Island manager (simplified - uses controller)
        new IslandManager(getProxy(), controller, exchange);

        // Event listeners
        getProxy().getPluginManager().registerListener(this, new PlayerJoinNetworkListener(exchange));
        getProxy().getPluginManager().registerListener(this, new PlayerListener());

        getLogger().info("✓ SkyblockProxy enabled");
    }

    @Override
    public void onDisable()
    {
        exchange.stop();
    }
}
