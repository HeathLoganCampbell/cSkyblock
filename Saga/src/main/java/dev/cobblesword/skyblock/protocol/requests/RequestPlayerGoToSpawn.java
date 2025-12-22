package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@ToString
public class RequestPlayerGoToSpawn extends ExchangeMessage
{
    public UUID playerId;
}
