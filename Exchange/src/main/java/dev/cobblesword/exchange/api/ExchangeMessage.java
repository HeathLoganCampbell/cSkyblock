package dev.cobblesword.exchange.api;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ExchangeMessage
{
    private UUID correlationId = UUID.randomUUID();
}
