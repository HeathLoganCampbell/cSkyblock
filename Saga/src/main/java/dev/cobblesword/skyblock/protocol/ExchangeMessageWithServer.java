package dev.cobblesword.skyblock.protocol;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExchangeMessageWithServer extends ExchangeMessage
{
    private String ServerId;
}
