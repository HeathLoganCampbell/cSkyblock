package dev.cobblesword.exchange.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExchangeFrame
{
    private String classType;

    private String payload;
}
