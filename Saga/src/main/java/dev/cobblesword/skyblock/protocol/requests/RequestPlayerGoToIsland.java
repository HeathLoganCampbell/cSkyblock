package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import dev.cobblesword.skyblock.protocol.models.PlayerModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class RequestPlayerGoToIsland extends ExchangeMessage
{
    public PlayerModel player;

    private UUID playerId;

    public UUID IslandId;
}
