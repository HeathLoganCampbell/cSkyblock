package dev.cobblesword.skyblock.proxy.islands;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToSpawn;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerRoute;
import dev.cobblesword.skyblock.proxy.controller.ProxyControllerClient;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

/**
 * Simplified Island Manager using Controller service.
 * Only handles spawn teleport via Redis - island routing uses Controller API.
 */
public class IslandManager {

    private final ProxyServer proxy;
    private final ProxyControllerClient controller;
    private final IExchange exchange;
    private final String spawnServerId = "Spawn-01";

    public IslandManager(ProxyServer proxy, ProxyControllerClient controller, IExchange exchange) {
        this.proxy = proxy;
        this.controller = controller;
        this.exchange = exchange;

        setupListeners();
    }

    private void setupListeners() {
        exchange.subscribe(e -> {
            // Route to spawn
            if (e instanceof RequestPlayerGoToSpawn request) {
                handleSpawnRequest(request);
            }
            // Route to any server (for island teleports)
            if (e instanceof RequestPlayerRoute request) {
                handleRouteRequest(request);
            }
        });
    }

    private void handleRouteRequest(RequestPlayerRoute request) {
        UUID playerId = request.getPlayerId();
        String targetServer = request.getTargetServer();

        ProxiedPlayer player = proxy.getPlayer(playerId);
        if (player == null) {
            System.out.println("Player " + playerId + " not online for route to " + targetServer);
            return;
        }

        sendToServer(player, targetServer);
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

    /**
     * Route player to their island via Controller API
     */
    public void sendPlayerToIsland(ProxiedPlayer player, UUID islandId) {
        controller.getRoute(player.getUniqueId()).thenAccept(route -> {
            if (route == null || route.serverId() == null) {
                player.sendMessage("§c✖ §7Could not find your island. Try again.");
                return;
            }

            sendToServer(player, route.serverId());
        }).exceptionally(e -> {
            player.sendMessage("§c✖ §7Failed to connect to controller.");
            e.printStackTrace();
            return null;
        });
    }

    public void sendToServer(ProxiedPlayer player, String serverId) {
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
