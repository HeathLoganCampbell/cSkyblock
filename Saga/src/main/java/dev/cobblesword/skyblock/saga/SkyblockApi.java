package dev.cobblesword.skyblock.saga;

import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.skyblock.protocol.models.IslandModel;
import dev.cobblesword.skyblock.saga.flows.island.create.CreateIslandFlow;
import dev.cobblesword.skyblock.saga.flows.island.load.LoadIslandFlow;
import dev.cobblesword.skyblock.saga.flows.island.teleport.TeleportToIslandFlow;
import dev.cobblesword.skyblock.saga.flows.spawn.teleport.TeleportToSpawnFlow;
import lombok.Getter;

import java.util.UUID;

public class SkyblockApi
{
    private IExchange exchange;

    @Getter
    private CreateIslandFlow createIslandFlow;

    @Getter
    private TeleportToIslandFlow teleportToIslandFlow;

    @Getter
    private TeleportToSpawnFlow teleportToSpawnFlow;

    @Getter
    private LoadIslandFlow loadIslandFlow;

    public SkyblockApi(IExchange exchange, String serverId, boolean isBungee)
    {
        this.exchange = exchange;

        this.teleportToIslandFlow = new TeleportToIslandFlow(this.exchange);
        this.createIslandFlow = new CreateIslandFlow(this.exchange, this.teleportToIslandFlow);
        this.loadIslandFlow = new LoadIslandFlow(this.exchange, this.teleportToIslandFlow);
        this.teleportToSpawnFlow = new TeleportToSpawnFlow(this.exchange);

        this.teleportToIslandFlow.onSaga(serverId, isBungee);
        this.createIslandFlow.onSaga(serverId, isBungee);
        this.loadIslandFlow.onSaga(serverId, isBungee);
        this.teleportToSpawnFlow.onSaga(serverId, isBungee);
    }

    public void createIsland(UUID requesterPlayerId)
    {
        this.createIslandFlow.sendRequestToCreateIsland(requesterPlayerId);
    }

    public void sendPlayerToIsland(UUID playerId, UUID islandId)
    {
        this.teleportToIslandFlow.sendRequestToGoToIsland(playerId, islandId);
    }

    public void sendPlayerToSpawn(UUID playerId)
    {
        this.teleportToSpawnFlow.sendRequestToGoToSpawn(playerId);
    }

    public IslandModel getIsland()
    {
        return null;
    }

}
