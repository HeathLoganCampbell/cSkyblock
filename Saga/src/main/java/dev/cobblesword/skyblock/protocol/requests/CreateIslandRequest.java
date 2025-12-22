package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateIslandRequest extends ExchangeMessage
{
    private UUID playerId;
}
