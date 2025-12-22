package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import dev.cobblesword.skyblock.protocol.models.PlayerDataModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Setter
@Getter
@ToString
public class SendPlayerDataWithServer extends ExchangeMessageWithServer
{
    private UUID playerId;

    private PlayerDataModel playerData;

    private UUID targetIslandId;
}
