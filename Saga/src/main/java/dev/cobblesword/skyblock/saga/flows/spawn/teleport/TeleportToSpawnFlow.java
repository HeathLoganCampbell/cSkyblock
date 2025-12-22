package dev.cobblesword.skyblock.saga.flows.spawn.teleport;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.requests.RequestPlayerGoToSpawn;
import lombok.Setter;

import java.util.UUID;

public class TeleportToSpawnFlow
{
    private IExchange exchange;

    @Setter
    private ITeleportToSpawnBungeeHandler bungeeHandler = null;


    public TeleportToSpawnFlow(IExchange exchange) {
        this.exchange = exchange;
    }

    // Create
    // Load
    //   Already loaded
    //   Not loaded

    // Server > Bungee         => Bungee > Server           => server > bungee
    // RequestPlayerGoToIsland => SendPlayerDataWithServer => PlayerDataReceived
    public void onSaga(String currentServerId, boolean isBungee)
    {
        exchange.subscribe(e -> {
            if(isBungee && e instanceof RequestPlayerGoToSpawn requestPlayerGoToSpawn)
            {
                String serverId = "Spawn-01";

                bungeeHandler.sendPlayerToServer(requestPlayerGoToSpawn.getPlayerId(), serverId);
            }
        });
    }

    public void sendRequestToGoToSpawn(UUID playerId)
    {
        RequestPlayerGoToSpawn requestPlayerGoToSpawn = new RequestPlayerGoToSpawn();
        requestPlayerGoToSpawn.setPlayerId(playerId);

        exchange.publishMessage(requestPlayerGoToSpawn);
    }
}
