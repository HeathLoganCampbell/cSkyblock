package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Request to route a player to a specific server
 */
@Setter
@Getter
@ToString
public class RequestPlayerRoute extends ExchangeMessage {
    private UUID playerId;
    private String targetServer;
}

