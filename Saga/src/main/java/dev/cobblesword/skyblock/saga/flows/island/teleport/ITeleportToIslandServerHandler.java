package dev.cobblesword.skyblock.saga.flows.island.teleport;

import dev.cobblesword.skyblock.protocol.models.PlayerDataModel;

import java.util.UUID;

public interface ITeleportToIslandServerHandler {
    /// return true = send player to server
    boolean prepareToRecievePlayer(UUID playerId, String serverId, UUID islandId, PlayerDataModel playerData);
}
