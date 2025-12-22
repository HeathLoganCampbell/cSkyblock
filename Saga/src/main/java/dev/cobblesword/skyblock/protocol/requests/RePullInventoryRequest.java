package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RePullInventoryRequest extends ExchangeMessageWithServer
{
    private UUID playerId;
}
