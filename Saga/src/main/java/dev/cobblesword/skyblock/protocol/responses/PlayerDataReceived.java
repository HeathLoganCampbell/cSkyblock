package dev.cobblesword.skyblock.protocol.responses;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@ToString
public class PlayerDataReceived extends ExchangeMessageWithServer
{
    private UUID playerId;
}
