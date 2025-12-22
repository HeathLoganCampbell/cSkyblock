package dev.cobblesword.exchange;

import dev.cobblesword.exchange.api.ExchangeListener;
import dev.cobblesword.exchange.api.ExchangeMessage;
import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.exchange.impl.redis.RedisExchange;
import dev.cobblesword.exchange.impl.redis.RedisSettings;
import dev.cobblesword.exchange.protocols.KeepAlive;

public class Main
{
    public static void main(String[] args)
    {
        IExchange exchange = getExchange();

        exchange.subscribe(event ->
        {
            if (event instanceof KeepAlive)
            {
                KeepAlive keepAlive = (KeepAlive) event;
                long duration = System.currentTimeMillis() - keepAlive.getTimestamp();

                System.out.println("Received KeepAlive message: " + event + " Duration: " + duration + "ms");
            }
        });

        exchange.stop();
    }

    private static IExchange getExchange() {
        RedisSettings redisSettings = new RedisSettings("localhost");
        return new RedisExchange(redisSettings);
    }
}
