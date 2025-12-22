package dev.cobblesword.skyblock.proxy.remoteinv;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.requests.RePullInventoryRequest;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerJoinNetworkListener implements Listener
{
    private Set<UUID> connectedPlayers = ConcurrentHashMap.newKeySet();

    private IExchange exchange;

    public PlayerJoinNetworkListener(IExchange exchange) {
        this.exchange = exchange;
    }

    @EventHandler
    public void onPostLogin(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        if (isNewPlayer(playerUUID))
        {
            this.connectedPlayers.add(playerUUID);
            RePullInventoryRequest rePullInventoryRequest = new RePullInventoryRequest();
            rePullInventoryRequest.setPlayerId(playerUUID);
            rePullInventoryRequest.setServerId("Proxy-01");
            this.exchange.publishMessage(rePullInventoryRequest);
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event)
    {
        UUID playerUUID = event.getPlayer().getUniqueId();
        connectedPlayers.remove(playerUUID);
    }

    private boolean isNewPlayer(UUID playerUUID) {
        return connectedPlayers.add(playerUUID);
    }
}
