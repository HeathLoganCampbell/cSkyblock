package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Request for a server to load an island and then route the player
 */
@Setter
@Getter
@ToString
public class RequestLoadIsland extends ExchangeMessage {
    private UUID playerId;
    private UUID islandId;
    private String targetServer;  // Server that should load the island
}

