package dev.cobblesword.exchange.impl.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import dev.cobblesword.exchange.api.ExchangeFrame;
import dev.cobblesword.exchange.api.ExchangeListener;
import dev.cobblesword.exchange.api.ExchangeMessage;
import dev.cobblesword.exchange.api.IExchange;
import dev.cobblesword.exchange.common.GsonUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RedisExchange implements IExchange
{
    private static final Gson GSON = new GsonBuilder().create();

    private JedisPool generalRedisPool;

    private JedisPool subRedisPool;

    private RedisSubscriber redisSubscriber;

    public static String PRIMARY_EXCHANGE_NAME = "exchange-messaging";

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public RedisExchange(RedisSettings redisSettings)
    {
        this.generalRedisPool = redisSettings.getRedisPool();
        this.subRedisPool = redisSettings.getRedisPool();

        this.redisSubscriber = new RedisSubscriber();
        executorService.submit(() -> {
            try (Jedis jedis = this.subRedisPool.getResource())
            {
                jedis.subscribe(redisSubscriber, PRIMARY_EXCHANGE_NAME);
            }
        });
    }

    @Override
    public void publishMessage(ExchangeMessage exchangeMessage)
    {
        try (Jedis redis = generalRedisPool.getResource())
        {
            String name = exchangeMessage.getClass().getCanonicalName();
            String payload = GSON.toJson(exchangeMessage);

            ExchangeFrame exchangeFrame = new ExchangeFrame(name, payload);
            String message = GSON.toJson(exchangeFrame);

            redis.publish(PRIMARY_EXCHANGE_NAME, message);
        }catch (JedisConnectionException e) {
            System.err.println("Error obtaining Redis connection: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            System.err.println("Error serializing message: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error when sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void subscribe(ExchangeListener exchangeListener)
    {
        redisSubscriber.registerListener(exchangeListener);
    }

    @Override
    public void stop()
    {
        executorService.shutdown();

        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS))
            {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        redisSubscriber.unsubscribe();
        generalRedisPool.close();
        subRedisPool.close();
    }

    @Override
    public String get(String key) {
        try(Jedis resource = this.generalRedisPool.getResource())
        {
            return resource.get(key);
        }
    }

    @Override
    public void set(String key, String value) {
        try(Jedis resource = this.generalRedisPool.getResource())
        {
            resource.set(key, value);
        }
    }

    public boolean delete(String key) {
        try (Jedis jedis = this.generalRedisPool.getResource()) {
            return jedis.del(key) > 0;
        } catch (Exception e) {
            throw new RuntimeException("Error deleting key from Redis", e);
        }
    }

    @Override
    public <T> void set(String key, T value) {
        try(Jedis resource = this.generalRedisPool.getResource())
        {
            String json = GsonUtil.getGson().toJson(value);
            resource.set(key, json);
        }
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        try(Jedis resource = this.generalRedisPool.getResource())
        {
            String value = resource.get(key);
            return GsonUtil.getGson().fromJson(value, clazz);
        }
    }

    public void addToList(String listKey, String element) {
        try (Jedis jedis = this.generalRedisPool.getResource()) {
            jedis.rpush(listKey, element); // Use lpush if you want to add to the start of the list
        } catch (JedisException e) {
            throw new RuntimeException("Error adding to list in Redis", e);
        }
    }

    public void removeFromList(String listKey, String element) {
        try (Jedis jedis = this.generalRedisPool.getResource()) {
            jedis.lrem(listKey, 1, element);
        } catch (JedisException e) {
            throw new RuntimeException("Error removing from list in Redis", e);
        }
    }

    public List<String> getListItems(String listKey) {
        try (Jedis jedis = this.generalRedisPool.getResource()) {
            // Get all elements from the list
            return jedis.lrange(listKey, 0, -1);
        } catch (JedisException e) {
            // Log the exception or handle it as needed
            throw new RuntimeException("Error retrieving items from list in Redis", e);
        }
    }
}
