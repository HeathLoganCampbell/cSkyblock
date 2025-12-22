package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class LoadIslandOnServer extends ExchangeMessageWithServer
{
    private UUID playerId;
    
    private UUID requesterPlayerId;
}
