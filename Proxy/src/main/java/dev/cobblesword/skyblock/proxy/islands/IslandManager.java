package dev.cobblesword.skyblock.proxy.islands;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToIsland;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToSpawn;
import dev.cobblesword.skyblock.proxy.controller.ProxyControllerClient;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Simplified Island Manager using Controller service
 */
public class IslandManager {

    private final ProxyServer proxy;
    private final ProxyControllerClient controller;
    private final IExchange exchange;
    private final String spawnServerId = "spawn-01";

    public IslandManager(ProxyServer proxy, ProxyControllerClient controller, IExchange exchange) {
        this.proxy = proxy;
        this.controller = controller;
        this.exchange = exchange;

        setupListeners();
    }

    private void setupListeners() {
        // Listen for island teleport requests
        exchange.subscribe(e -> {
            if (e instanceof RequestPlayerGoToIsland request) {
                handleIslandRequest(request);
            }
            
            if (e instanceof RequestPlayerGoToSpawn request) {
                handleSpawnRequest(request);
            }
        });
    }

    private void handleIslandRequest(RequestPlayerGoToIsland request) {
        UUID playerId = request.getPlayerId();
        String targetServerId = request.getTargetServerId();

        ProxiedPlayer player = proxy.getPlayer(playerId);
        if (player == null) {
            System.out.println("Player " + playerId + " not online for island teleport");
            return;
        }

        sendToServer(player, targetServerId);
    }

    private void handleSpawnRequest(RequestPlayerGoToSpawn request) {
        UUID playerId = request.getPlayerId();

        ProxiedPlayer player = proxy.getPlayer(playerId);
        if (player == null) {
            System.out.println("Player " + playerId + " not online for spawn teleport");
            return;
        }

        sendToServer(player, spawnServerId);
    }

    private void sendToServer(ProxiedPlayer player, String serverId) {
        ServerInfo targetServer = proxy.getServerInfo(serverId);
        
        if (targetServer != null) {
            System.out.println("Sending " + player.getName() + " to " + serverId);
            player.connect(targetServer);
        } else {
            player.sendMessage("§c✖ §7Server unavailable. Please try again.");
            System.err.println("Server not found: " + serverId);
        }
    }
}
