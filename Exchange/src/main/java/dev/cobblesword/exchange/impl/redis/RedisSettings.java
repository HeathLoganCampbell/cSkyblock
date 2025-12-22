package dev.cobblesword.exchange.impl.redis;

import redis.clients.jedis.JedisPool;

public class RedisSettings
{
    private final String address;
    private final int port;
    private final String password;

    public RedisSettings(String address, int port, String password)
    {
        this.address = address;
        this.port = port;
        this.password = password;
    }

    public RedisSettings(String address, String password)
    {
        this(address, 6379, password);
    }

    public RedisSettings(String address)
    {
        this(address, null);
    }

    public boolean hasPassword()
    {
        return this.password != null;
    }

    public JedisPool getRedisPool()
    {
        return new JedisPool(this.address, this.port);
    }
}
