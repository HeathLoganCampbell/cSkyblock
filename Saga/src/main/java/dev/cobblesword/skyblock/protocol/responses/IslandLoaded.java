package dev.cobblesword.skyblock.protocol.responses;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class IslandLoaded extends ExchangeMessageWithServer
{
    private UUID IslandId;

    private UUID requesterPlayerId;

    private String islandName;
}
