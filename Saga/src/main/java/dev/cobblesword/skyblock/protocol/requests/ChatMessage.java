package dev.cobblesword.skyblock.protocol.requests;

import dev.cobblesword.skyblock.protocol.ExchangeMessageWithServer;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage extends ExchangeMessageWithServer
{
    private String message;
}
