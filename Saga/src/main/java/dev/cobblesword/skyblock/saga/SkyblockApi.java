package dev.cobblesword.skyblock.saga;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.saga.flows.spawn.teleport.TeleportToSpawnFlow;
import lombok.Getter;

import java.util.UUID;

/**
 * Simplified API - only handles spawn teleportation via Redis.
 * Island operations are now handled by the Controller HTTP API.
 */
public class SkyblockApi
{
    private IExchange exchange;

    @Getter
    private TeleportToSpawnFlow teleportToSpawnFlow;

    public SkyblockApi(IExchange exchange, String serverId, boolean isBungee)
    {
        this.exchange = exchange;
        this.teleportToSpawnFlow = new TeleportToSpawnFlow(this.exchange);
        this.teleportToSpawnFlow.onSaga(serverId, isBungee);
    }

    public void sendPlayerToSpawn(UUID playerId)
    {
        this.teleportToSpawnFlow.sendRequestToGoToSpawn(playerId);
    }
}
