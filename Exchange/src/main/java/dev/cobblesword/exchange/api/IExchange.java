package dev.cobblesword.exchange.api;

import java.util.List;
import java.util.UUID;

public interface IExchange
{
    void publishMessage(ExchangeMessage exchangeMessage);

   void subscribe(ExchangeListener exchangeListener);

   void stop();

    String get(String key);

    void set(String key, String value);

    boolean delete(String key);

    <T> void set(String key, T value);

    <T> T get(String key, Class<T> clazz);

    void addToList(String listKey, String element);

    void removeFromList(String listKey, String element);

    List<String> getListItems(String listKey);
}
