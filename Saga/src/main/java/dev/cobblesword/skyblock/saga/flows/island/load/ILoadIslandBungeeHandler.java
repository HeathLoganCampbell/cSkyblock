package dev.cobblesword.skyblock.saga.flows.island.load;

import dev.cobblesword.skyblock.protocol.models.IslandModel;
import dev.cobblesword.skyblock.protocol.responses.IslandLoaded;

import java.util.UUID;

public interface ILoadIslandBungeeHandler
{
    IslandModel getActiveIsland(UUID islandId);

    String getBestFitServerForNewIsland();

    void onIslandLoaded(IslandLoaded islandLoaded);
}
