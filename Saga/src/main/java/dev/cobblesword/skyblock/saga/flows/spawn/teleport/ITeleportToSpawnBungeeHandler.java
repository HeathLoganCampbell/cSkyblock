package dev.cobblesword.skyblock.saga.flows.spawn.teleport;

import java.util.UUID;

public interface ITeleportToSpawnBungeeHandler
{
    void sendPlayerToServer(UUID playerId, String serverId);
}
