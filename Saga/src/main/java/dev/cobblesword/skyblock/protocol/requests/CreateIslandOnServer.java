package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class CreateIslandOnServer extends ExchangeMessageWithServer
{
    private UUID requesterPlayerId;
}
