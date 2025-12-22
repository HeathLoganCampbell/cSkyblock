package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LoadIslandRequest extends ExchangeMessage
{
    private UUID islandId;

    private UUID requesterPlayerId;
}
