package dev.cobblesword.skyblock.saga.flows.island.create;

import dev.cobblesword.skyblock.protocol.responses.IslandCreated;

public interface ICreateIslandBungeeHandler {
    String getBestFitServerForNewIsland();

    void onIslandCreated(IslandCreated islandCreated);
}
