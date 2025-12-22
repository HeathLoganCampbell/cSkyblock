package dev.cobblesword.skyblock.saga.flows.island.create;

import dev.cobblesword.skyblock.protocol.models.IslandModel;
import dev.cobblesword.skyblock.saga.flows.island.Consumer;

import java.util.UUID;

public interface ICreateIslandServerHandler {
    void createIsland(UUID requesterPlayerId, Consumer<IslandModel> callback);
}
