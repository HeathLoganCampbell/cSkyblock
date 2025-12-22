package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class RequestPlayerGoToIsland extends ExchangeMessage
{
    private UUID playerId;
    private UUID islandId;
    private String targetServerId;  // Which server should load this island
}
