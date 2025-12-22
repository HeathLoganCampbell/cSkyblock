package dev.cobblesword.exchange.impl.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.cobblesword.exchange.api.ExchangeFrame;
import dev.cobblesword.exchange.api.ExchangeListener;
import dev.cobblesword.exchange.api.ExchangeMessage;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class RedisSubscriber extends JedisPubSub
{
    private static final Gson GSON = new GsonBuilder().create();

    private List<ExchangeListener> listeners = new CopyOnWriteArrayList<>();

    public Optional<Class<?>> toClass(String classType)
    {
        try
        {
            return Optional.ofNullable(Class.forName(classType));
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Class not found exception:" +  e);
            return Optional.empty();
        }
    }

    @Override
    public void onMessage(String channel, String message)
    {
        ExchangeFrame exchangeFrame = GSON.fromJson(message, ExchangeFrame.class);

        String classType = exchangeFrame.getClassType();
        String payload = exchangeFrame.getPayload();

        Optional<Class<?>> clazzOptional = toClass(classType);

        if(!clazzOptional.isPresent())
        {
            System.out.println("Failed to handle " + exchangeFrame.getClassType());
            return;
        }

        Class<?> clazz = clazzOptional.get();
        if(!ExchangeMessage.class.isAssignableFrom(clazz))
        {
            System.out.println("Failed to handle " + exchangeFrame.getClassType() + " because it doesn't extend ExchangeMessage");
            return;
        }

        Class<? extends ExchangeMessage> exchangeMessageClazz = (Class<? extends ExchangeMessage>) clazz;

        ExchangeMessage exchangeMessage = GSON.fromJson(payload, exchangeMessageClazz);

        for (ExchangeListener listener : listeners)
        {
            listener.onEvent(exchangeMessage);
        }
    }

    public void registerListener(ExchangeListener exchangeListener)
    {
        listeners.add(exchangeListener);
    }
}
