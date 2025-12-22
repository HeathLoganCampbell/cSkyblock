package dev.cobblesword.exchange.protocols;

import dev.cobblesword.exchange.api.ExchangeMessage;
import lombok.*;

@ToString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class KeepAlive extends ExchangeMessage
{
    private String serverId;

    private String address;

    private int port;

    private long timestamp;

    private int playerCount;
}
