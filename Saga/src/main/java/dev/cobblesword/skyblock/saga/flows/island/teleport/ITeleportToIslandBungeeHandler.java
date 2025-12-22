package dev.cobblesword.skyblock.saga.flows.island.teleport;

import java.util.UUID;

public interface ITeleportToIslandBungeeHandler
{
    void sendPlayerToServer(UUID playerId, String serverId);

    String getServerOfIsland(UUID islandId);
}
