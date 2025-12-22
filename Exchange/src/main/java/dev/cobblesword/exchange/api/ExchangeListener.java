package dev.cobblesword.exchange.api;

public interface ExchangeListener
{
    void onEvent(ExchangeMessage exchangeMessage);
}
