package dev.cobblesword.skyblock.saga.flows.island.load;

import dev.cobblesword.skyblock.protocol.models.IslandModel;
import dev.cobblesword.skyblock.saga.flows.island.Consumer;

import java.util.UUID;

public interface ILoadIslandServerHandler
{
    void loadIsland(UUID requesterPlayerId, Consumer<IslandModel> callback);
}
